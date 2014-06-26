/*
 * Copyright 2014 Kevin Herron
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalpetri.ethernetip.cip.services

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import com.digitalpetri.ethernetip.cip.CipClassCodes
import com.digitalpetri.ethernetip.cip.epath.{ClassId, ConnectionPoint, InstanceId, PaddedEPath}
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.NetworkConnectionParameters.ConnectionType._
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.NetworkConnectionParameters.Priority._
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.NetworkConnectionParameters.Size.{FixedSize, Size, VariableSize}
import com.digitalpetri.ethernetip.util.{Buffers, TimeoutCalculator}
import io.netty.buffer.{ByteBuf, Unpooled}

import scala.concurrent.duration.Duration
import scala.util.Try


object ForwardOpen {

  val ServiceCode = 0x54

  val MessageRouterConnectionPoint = PaddedEPath(
    ClassId(CipClassCodes.MessageRouterObject),
    InstanceId(0x01),
    ConnectionPoint(0x01))

  val T2OConnectionIds = new AtomicInteger(0)

  case class ForwardOpenRequest(timeout: Duration,
                                o2tConnectionId: Int = 0,
                                t2oConnectionId: Int = T2OConnectionIds.getAndIncrement,
                                connectionSerialNumber: Int,
                                vendorId: Int,
                                vendorSerialNumber: Long,
                                connectionTimeoutMultiplier: Int = DefaultConnectionTimeoutMultiplier,
                                connectionPath: PaddedEPath,
                                o2tRpi: Duration = DefaultRpi,
                                o2tNetworkConnectionParameters: NetworkConnectionParameters,
                                t2oRpi: Duration = DefaultRpi,
                                t2oNetworkConnectionParameters: NetworkConnectionParameters,
                                transportClassAndTrigger: Int = MessagingConnectionTransportClassAndTrigger)

  case class ForwardOpenResponse(o2tConnectionId: Int,
                                 t2oConnectionId: Int,
                                 connectionSerialNumber: Int,
                                 originatorVendorId: Int,
                                 originatorSerialNumber: Long,
                                 o2tActualPacketInterval: Long,
                                 t2oActualPacketInterval: Long,
                                 applicationReplySize: Short,
                                 reserved: Byte = 0,
                                 applicationReply: ByteBuf)

  object ForwardOpenRequest {

    private val ReservedBytesLength = 3

    def encode(request: ForwardOpenRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      val priorityAndTimeoutBytes = TimeoutCalculator.calculateTimeoutBytes(request.timeout)
      buffer.writeByte(priorityAndTimeoutBytes >> 8 & 0xFF)
      buffer.writeByte(priorityAndTimeoutBytes      & 0xFF)

      buffer.writeInt(0) // TODO chosen by remote and indicated in response?
      buffer.writeInt(request.t2oConnectionId)
      buffer.writeShort(request.connectionSerialNumber)

      buffer.writeShort(request.vendorId)
      buffer.writeInt(request.vendorSerialNumber.toInt)

      buffer.writeByte(request.connectionTimeoutMultiplier)
      buffer.writeZero(ReservedBytesLength)

      buffer.writeInt(request.o2tRpi.toMicros.toInt)
      buffer.writeShort(request.o2tNetworkConnectionParameters)

      buffer.writeInt(request.t2oRpi.toMicros.toInt)
      buffer.writeShort(request.t2oNetworkConnectionParameters)

      buffer.writeByte(request.transportClassAndTrigger)

      PaddedEPath.encode(request.connectionPath, buffer)
    }

    def decode(buffer: ByteBuf): ForwardOpenRequest = {
      val priorityAndTick = buffer.readByte()
      val timeoutTicks = buffer.readUnsignedByte()
      val timeTick = priorityAndTick & 0x0F
      val timePerTick = Math.pow(2, timeTick).toInt
      val timeout = Duration(timePerTick * timeoutTicks, TimeUnit.MILLISECONDS)

      val o2tConnectionId = buffer.readInt()
      val t2oConnectionId = buffer.readInt()
      val serialNumber = buffer.readUnsignedShort()

      val vendorId = buffer.readUnsignedShort()
      val vendorSerialNumber = buffer.readUnsignedInt()

      val connectionTimeoutMultiplier = buffer.readUnsignedByte()
      buffer.skipBytes(ReservedBytesLength)

      val o2tRpi = buffer.readUnsignedInt()
      val o2tNetworkConnectionParameters = buffer.readUnsignedShort()

      val t2oRpi = buffer.readUnsignedInt()
      val t2oNetworkConnectionParameters = buffer.readUnsignedShort()

      val transportClassAndTrigger = buffer.readByte()

      val connectionPath = PaddedEPath.decode(buffer)

      ForwardOpen.ForwardOpenRequest(
        timeout,
        o2tConnectionId,
        t2oConnectionId,
        serialNumber,
        vendorId,
        vendorSerialNumber,
        connectionTimeoutMultiplier,
        connectionPath,
        o2tRpi = Duration(o2tRpi, TimeUnit.MICROSECONDS),
        o2tNetworkConnectionParameters,
        t2oRpi = Duration(t2oRpi, TimeUnit.MICROSECONDS),
        t2oNetworkConnectionParameters,
        transportClassAndTrigger)
    }
  }

  object ForwardOpenResponse {

    def encode(response: ForwardOpenResponse, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer.writeInt(response.o2tConnectionId)
      buffer.writeInt(response.t2oConnectionId)
      buffer.writeShort(response.connectionSerialNumber)
      buffer.writeShort(response.originatorVendorId)
      buffer.writeInt(response.originatorSerialNumber.toInt)
      buffer.writeInt(response.o2tActualPacketInterval.toInt)
      buffer.writeInt(response.t2oActualPacketInterval.toInt)
      buffer.writeByte(response.applicationReplySize)
      buffer.writeByte(response.reserved)
      buffer.writeBytes(response.applicationReply, response.applicationReplySize)
    }

    def decode(buffer: ByteBuf): Try[ForwardOpenResponse] = Try {
      val o2tConnectionId         = buffer.readInt()
      val t2oConnectionId         = buffer.readInt()
      val connectionSerialNumber  = buffer.readShort()
      val originatorVendorId      = buffer.readShort()
      val originatorSerialNumber  = buffer.readInt()
      val o2tActualPacketInterval = buffer.readUnsignedInt()
      val t2oActualPacketInterval = buffer.readUnsignedInt()
      val applicationReplySize    = buffer.readUnsignedByte()
      val reserved                = buffer.readByte()

      val applicationReply: ByteBuf = {
        if (applicationReplySize > 0) buffer.readSlice(applicationReplySize)
        else Unpooled.EMPTY_BUFFER
      }

      ForwardOpenResponse(
        o2tConnectionId,
        t2oConnectionId,
        connectionSerialNumber,
        originatorVendorId,
        originatorSerialNumber,
        o2tActualPacketInterval,
        t2oActualPacketInterval,
        applicationReplySize,
        reserved,
        applicationReply)
    }

  }

  /**
   * Bit 7: 		1		0=client, 1=server
   * Bit 6-4:	 	2		0=cyclic, 1=change of state, 2=application object
   * Bit 3-0: 	3		0=class 0, 1=class 1, 2=class 2, 3=class 3
   * <p>
   * Default to asking endpoint to behave as a server, using application object production trigger (which gets ignored
   * when asking endpoint to act as a server...), and class 3 connection. Some day we may need to expose these options.
   */
  val MessagingConnectionTransportClassAndTrigger = 0xA3

  /**
   * Connection Timeout Multiplier, used in conjunction with the RPI, indicates the inactivity timeout for this
   * connection.
   * <p>
   * 0 = x4, 1 = x8, 2 = x16, 3 = x32, 4 = x128, 5 = x256, 6 = x512
   */
  val DefaultConnectionTimeoutMultiplier = 0x01

  /**
   * 2 seconds. When connecting to a server endpoint, it is used with the Connection Timeout Multiplier parameter to
   * derive an inactivity timeout for the connection.
   */
  val DefaultRpi = Duration(2, TimeUnit.SECONDS)

  val DefaultExplicitConnectionParameters = NetworkConnectionParameters(
    connectionSize  = 500,
    sizeType        = VariableSize,
    priority        = Low,
    connectionType  = PointToPoint,
    redundantOwner  = false)

  val DefaultO2tIoConnectionParameters = NetworkConnectionParameters(
    connectionSize  = 2,
    sizeType        = FixedSize,
    priority        = Low,
    connectionType  = PointToPoint,
    redundantOwner  = false)

  val DefaultT2oIoConnectionParameters = NetworkConnectionParameters(
    _: Int,
    sizeType        = FixedSize,
    priority        = High,
    connectionType  = Multicast,
    redundantOwner  = false)

  case class NetworkConnectionParameters(connectionSize: Int = 500,
                                         sizeType: Size = FixedSize,
                                         priority: Priority = Low,
                                         connectionType: ConnectionType = PointToPoint,
                                         redundantOwner: Boolean = false)

  object NetworkConnectionParameters {

    import scala.language.implicitConversions

    object Size {
      sealed abstract class Size(val bit: Int)
      case object FixedSize extends Size(0)
      case object VariableSize extends Size(1)
    }

    object Priority {
      sealed abstract class Priority(val bits: Int)
      case object Low extends Priority(0)
      case object High extends Priority(1)
      case object Scheduled extends Priority(2)
      case object Urgent extends Priority(3)
    }

    object ConnectionType {
      sealed abstract class ConnectionType(val bits: Int)
      case object Null extends ConnectionType(0)
      case object Multicast extends ConnectionType(1)
      case object PointToPoint extends ConnectionType(2)
      case object Reserved extends ConnectionType(3)
    }

    implicit def intToParameters(parameters: Int): NetworkConnectionParameters = {
      val connectionSize = parameters & 0x1FF

      val sizeType = {
        val bit = ((parameters >> 9) & 1) == 1
        if (bit) Size.VariableSize else Size.FixedSize
      }

      val priority = {
        val bits = (parameters >> 10) & 3

        bits match {
          case 0 => Low
          case 1 => High
          case 2 => Scheduled
          case _ => Urgent
        }
      }

      val connectionType = {
        val bits = (parameters >> 13) & 3

        bits match {
          case 0 => Null
          case 1 => Multicast
          case 2 => PointToPoint
          case _ => Reserved
        }
      }

      val redundantOwner = ((parameters >> 15) & 1) == 1

      NetworkConnectionParameters(connectionSize, sizeType, priority, connectionType, redundantOwner)
    }

    implicit def parametersToInt(parameters: NetworkConnectionParameters): Int = {
      var parametersInt = parameters.connectionSize & 0x1FF

      parametersInt |= (parameters.sizeType.bit << 9)
      parametersInt |= (parameters.priority.bits << 10)
      parametersInt |= (parameters.connectionType.bits << 13)
      if (parameters.redundantOwner) parametersInt |= (1 << 15)

      parametersInt
    }

  }

}
