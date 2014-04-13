package com.digitalpetri.ethernetip.client.cip.services

import com.digitalpetri.ethernetip.cip.epath._
import com.digitalpetri.ethernetip.cip.{CipClassCodes, MessageRouterRequest}
import com.digitalpetri.ethernetip.client.cip.InvokableService
import com.digitalpetri.ethernetip.client.cip.services.UnconnectedSend.UnconnectedSendRequest
import com.digitalpetri.ethernetip.client.util.TimeoutCalculator
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf
import scala.concurrent.duration.Duration
import scala.concurrent.{Promise, Future}
import scala.util.Failure
import scala.util.Success
import scala.util.Try

/**
 * The Unconnected_Send service shall allow an application to send a message to a device without first setting up a
 * connection.
 *
 * The Unconnected_Send service shall use the Connection Manager object in each intermediate node to forward
 * the message and to remember the return path. The UCMM of each link shall be used to forward the request from
 * Connection Manager to Connection Manager just as it is for the Forward_Open service; however, no connection shall be
 * built. The Unconnected_Send service shall be sent to the local Connection Manager and shall be sent between
 * intermediate nodes. When an intermediate node removes the last port segment, the embedded Message Request shall be
 * formatted as a Message Router Request message and sent to the port and link address of the last port segment using
 * the UCMM for that link type.
 *
 * The target node never sees the Unconnected_Send service but only the embedded Message Request arriving via the UCMM.
 *
 * @param request a [[UnconnectedSendRequest]]
 */
class UnconnectedSend(request: UnconnectedSendRequest) extends InvokableService[ByteBuf] {

  private val promise = Promise[ByteBuf]()

  def response: Future[ByteBuf] = promise.future

  def getRequestData: ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = UnconnectedSend.ServiceCode,
      requestPath = UnconnectedSend.ConnectionManagerPath,
      requestData = encode(request))

    MessageRouterRequest.encode(routerRequest)
  }

  def setResponseData(data: ByteBuf): Option[ByteBuf] = {
    decode(data) match {
      case Success(response) => promise.success(response)
      case Failure(ex) => promise.failure(ex)
    }

    None
  }

  def setResponseFailure(ex: Throwable): Unit = promise.failure(ex)

  private def encode(request: UnconnectedSendRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    val priorityAndTimeoutBytes = TimeoutCalculator.calculateTimeoutBytes(request.timeout)

    // priority/timeTick & timeoutTicks
    buffer.writeByte(priorityAndTimeoutBytes >> 8 & 0xFF)
    buffer.writeByte(priorityAndTimeoutBytes >> 0 & 0xFF)

    // message length + message
    val bytesWritten = encodeEmbeddedMessage(request, buffer)

    // pad byte if length was odd
    if (bytesWritten % 2 != 0) buffer.writeByte(0x00)

    // path length + reserved + path
    encodeConnectionPath(request, buffer)

    buffer
  }

  private def encodeEmbeddedMessage(request: UnconnectedSendRequest, buffer: ByteBuf): Int = {
    // length of embedded message
    val lengthStartIndex = buffer.writerIndex()
    buffer.writeShort(0)

    // embedded message
    val messageStartIndex = buffer.writerIndex()
    buffer.writeBytes(request.embeddedRequest)

    // go back and update length
    val bytesWritten = buffer.writerIndex() - messageStartIndex
    buffer.markWriterIndex()
    buffer.writerIndex(lengthStartIndex)
    buffer.writeShort(bytesWritten)
    buffer.resetWriterIndex()

    bytesWritten
  }

  private def encodeConnectionPath(request: UnconnectedSendRequest, buffer: ByteBuf) {
    // connectionPath length
    val pathLengthStartIndex = buffer.writerIndex()
    buffer.writeByte(0)

    // reserved byte
    buffer.writeByte(0x00)

    // encode the path segments...
    val pathDataStartIndex = buffer.writerIndex()

    request.connectionPath.segments.foreach {
      case s: LogicalSegment[_] => LogicalSegment.encode(s, padded = true, buffer)
      case s: PortSegment       => PortSegment.encode(s, buffer)
    }

    // go back and update the length.
    val pathBytesWritten = buffer.writerIndex() - pathDataStartIndex
    val wordsWritten = pathBytesWritten / 2
    buffer.markWriterIndex()
    buffer.writerIndex(pathLengthStartIndex)
    buffer.writeByte(wordsWritten.asInstanceOf[Byte])
    buffer.resetWriterIndex()
  }

  private def decode(buffer: ByteBuf): Try[ByteBuf] = {
    Success(buffer) // TODO
  }

}

object UnconnectedSend {

  val ServiceCode = 0x52

  val ConnectionManagerPath = PaddedEPath(
    ClassId(CipClassCodes.ConnectionManagerObject),
    InstanceId(0x01))

  case class UnconnectedSendRequest(timeout: Duration, embeddedRequest: ByteBuf, connectionPath: PaddedEPath)
  case class UnconnectedSendResponse(data: ByteBuf)

}
