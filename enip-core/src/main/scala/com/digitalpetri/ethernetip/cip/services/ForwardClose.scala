package com.digitalpetri.ethernetip.cip.services

import java.util.concurrent.TimeUnit

import com.digitalpetri.ethernetip.cip.epath.PaddedEPath
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

      PaddedEPath.encode(request.connectionPath, buffer)
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
      val connectionPath          = PaddedEPath.decode(buffer)

      ForwardCloseRequest(
        connectionTimeout,
        connectionSerialNumber,
        originatorVendorId,
        originatorSerialNumber,
        connectionPath)
    }

  }

}
