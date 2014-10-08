package com.digitalpetri.ethernetip.cip.services

import java.util.concurrent.TimeUnit

import com.digitalpetri.ethernetip.cip.epath._
import com.digitalpetri.ethernetip.util.{Buffers, TimeoutCalculator}
import io.netty.buffer.ByteBuf

import scala.concurrent.duration.Duration

object ForwardClose {

  val ServiceCode = 0x4E

  case class ForwardCloseRequest(connectionTimeout: Duration,
                                 connectionSerialNumber: Int,
                                 originatorVendorId: Int,
                                 originatorSerialNumber: Long,
                                 connectionPath: PaddedEPath)

  case class ForwardCloseResponse()

  object ForwardCloseRequest {

    def encode(request: ForwardCloseRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      val priorityAndTimeoutBytes = TimeoutCalculator.calculateTimeoutBytes(request.connectionTimeout)
      buffer.writeByte(priorityAndTimeoutBytes >> 8 & 0xFF)
      buffer.writeByte(priorityAndTimeoutBytes      & 0xFF)

      buffer.writeShort(request.connectionSerialNumber)
      buffer.writeShort(request.originatorVendorId)
      buffer.writeInt(request.originatorSerialNumber.toInt)

      // length placeholder...
      val lengthStartIndex = buffer.writerIndex()
      buffer.writeByte(0)

      // reserved
      buffer.writeByte(0)

      // encode the path segments...
      val dataStartIndex = buffer.writerIndex()

      request.connectionPath.segments.foreach {
        case s: LogicalSegment[_] => LogicalSegment.encode(s, padded = true, buffer)
        case s: PortSegment       => PortSegment.encode(s, buffer)
        case s: AnsiDataSegment   => AnsiDataSegment.encode(s, buffer)
        case s: SimpleDataSegment => SimpleDataSegment.encode(s, buffer)
      }

      // go back and update the length
      val bytesWritten = buffer.writerIndex() - dataStartIndex
      val wordsWritten = bytesWritten / 2
      buffer.markWriterIndex()
      buffer.writerIndex(lengthStartIndex)
      buffer.writeByte(wordsWritten.asInstanceOf[Short])
      buffer.resetWriterIndex()
    }

    def decode(buffer: ByteBuf): ForwardCloseRequest = {
      val priorityAndTick   = buffer.readByte()
      val timeoutTicks      = buffer.readUnsignedByte()
      val timeTick          = priorityAndTick & 0x0F
      val timePerTick       = Math.pow(2, timeTick).toInt
      val connectionTimeout = Duration(timePerTick * timeoutTicks, TimeUnit.MILLISECONDS)

      val connectionSerialNumber  = buffer.readUnsignedShort()
      val originatorVendorId      = buffer.readUnsignedShort()
      val originatorSerialNumber  = buffer.readUnsignedInt()

      val wordCount = buffer.readUnsignedByte()
      val byteCount = wordCount * 2

      buffer.skipBytes(1) // reserved

      val dataStartIndex = buffer.readerIndex()
      def decodeSegments(segments: Seq[EPathSegment] = Seq.empty): Seq[EPathSegment] = {
        if (buffer.readerIndex() >= dataStartIndex + byteCount) segments
        else decodeSegments(segments :+ EPathSegment.decode(buffer))
      }

      val connectionPath = PaddedEPath(decodeSegments(): _*)

      ForwardCloseRequest(
        connectionTimeout,
        connectionSerialNumber,
        originatorVendorId,
        originatorSerialNumber,
        connectionPath)
    }

  }

}
