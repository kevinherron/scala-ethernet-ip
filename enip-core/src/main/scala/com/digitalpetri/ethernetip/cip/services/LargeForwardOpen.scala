package com.digitalpetri.ethernetip.cip.services

import java.util.concurrent.TimeUnit

import com.digitalpetri.ethernetip.cip.epath.PaddedEPath
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.NetworkConnectionParameters
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.NetworkConnectionParameters.ConnectionType._
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.NetworkConnectionParameters.Priority._
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.NetworkConnectionParameters.Size
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.NetworkConnectionParameters.Size.{FixedSize, VariableSize}
import com.digitalpetri.ethernetip.util.{Buffers, TimeoutCalculator}
import io.netty.buffer.ByteBuf

import scala.concurrent.duration.Duration

object LargeForwardOpen {

  val ServiceCode = 0x5B

  case class LargeForwardOpenRequest(timeout: Duration,
                                     o2tConnectionId: Int = 0,
                                     t2oConnectionId: Int = ForwardOpen.T2OConnectionIds.getAndIncrement,
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


  def intToParameters(parameters: Int): NetworkConnectionParameters = {
    val connectionSize = parameters & 0xFFFF

    val sizeType = {
      val bit = ((parameters >> 25) & 1) == 1
      if (bit) Size.VariableSize else Size.FixedSize
    }

    val priority = {
      val bits = (parameters >> 26) & 3

      bits match {
        case 0 => Low
        case 1 => High
        case 2 => Scheduled
        case _ => Urgent
      }
    }

    val connectionType = {
      val bits = (parameters >> 29) & 3

      bits match {
        case 0 => Null
        case 1 => Multicast
        case 2 => PointToPoint
        case _ => Reserved
      }
    }

    val redundantOwner = ((parameters >> 31) & 1) == 1

    NetworkConnectionParameters(connectionSize, sizeType, priority, connectionType, redundantOwner)
  }

  def parametersToInt(parameters: NetworkConnectionParameters): Int = {
    var parametersInt = parameters.connectionSize & 0xFFFF

    parametersInt |= (parameters.sizeType.bit << 25)
    parametersInt |= (parameters.priority.bits << 26)
    parametersInt |= (parameters.connectionType.bits << 29)
    if (parameters.redundantOwner) parametersInt |= (1 << 31)

    parametersInt
  }

  object LargeForwardOpenRequest {
    private val ReservedBytesLength = 3

    def encode(request: LargeForwardOpenRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
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
      buffer.writeInt(LargeForwardOpen.parametersToInt(request.o2tNetworkConnectionParameters))

      buffer.writeInt(request.t2oRpi.toMicros.toInt)
      buffer.writeInt(LargeForwardOpen.parametersToInt(request.t2oNetworkConnectionParameters))

      buffer.writeByte(request.transportClassAndTrigger)

      PaddedEPath.encode(request.connectionPath, buffer)
    }

    def decode(buffer: ByteBuf): LargeForwardOpenRequest = {
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
      val o2tNetworkConnectionParameters = buffer.readInt()

      val t2oRpi = buffer.readUnsignedInt()
      val t2oNetworkConnectionParameters = buffer.readInt()

      val transportClassAndTrigger = buffer.readByte()

      val connectionPath = PaddedEPath.decode(buffer)

      LargeForwardOpenRequest(
        timeout,
        o2tConnectionId,
        t2oConnectionId,
        serialNumber,
        vendorId,
        vendorSerialNumber,
        connectionTimeoutMultiplier,
        connectionPath,
        o2tRpi = Duration(o2tRpi, TimeUnit.MICROSECONDS),
        LargeForwardOpen.intToParameters(o2tNetworkConnectionParameters),
        t2oRpi = Duration(t2oRpi, TimeUnit.MICROSECONDS),
        LargeForwardOpen.intToParameters(t2oNetworkConnectionParameters),
        transportClassAndTrigger)
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

}
