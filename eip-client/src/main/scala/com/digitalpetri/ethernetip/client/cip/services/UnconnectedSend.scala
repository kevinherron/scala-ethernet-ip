package com.digitalpetri.ethernetip.client.cip.services

import com.digitalpetri.ethernetip.cip.{EPath, MessageRouterRequest}
import com.digitalpetri.ethernetip.client.cip.InvokableService
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf
import scala.concurrent.duration.Duration
import scala.concurrent.{Promise, Future}
import scala.util.{Try, Failure, Success}

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

  def serviceCode: Int = UnconnectedSend.ServiceCode

  def response: Future[ByteBuf] = promise.future

  def getRequestData: ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = serviceCode,
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
    // TODO
    buffer
  }

  private def decode(buffer: ByteBuf): Try[ByteBuf] = {
    Success(buffer)
  }


}

case class UnconnectedSendRequest(desiredTimeout: Duration,
                                  embeddedRequest: ByteBuf,
                                  connectionPath: EPath)

object UnconnectedSend {

  val ServiceCode = 0x52
  val ConnectionManagerPath = EPath() // TODO

}
