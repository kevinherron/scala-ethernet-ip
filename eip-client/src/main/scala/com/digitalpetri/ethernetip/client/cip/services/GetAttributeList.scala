package com.digitalpetri.ethernetip.client.cip.services

import com.digitalpetri.ethernetip.cip.{UnconnectedSendRequest, MessageRouterRequest, CipConnection, EPath}
import com.digitalpetri.ethernetip.client.cip.CipClient
import com.digitalpetri.ethernetip.client.cip.services.GetAttributeList.AttributeResponse
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf
import scala.concurrent.{Promise, Future}
import scala.util.{Try, Failure, Success}

class GetAttributeList(attributes: Seq[Int]) extends CipService[Seq[Int], Seq[AttributeResponse]] {

  def serviceCode: Int = 0x03

  def invoke(request: Seq[Int], requestPath: EPath, connection: CipConnection)
            (implicit client: CipClient): Future[Seq[AttributeResponse]] = {

    ???
  }

  def invoke(request: Seq[Int], requestPath: EPath, connectionPath: EPath)
            (implicit client: CipClient): Future[Seq[AttributeResponse]] = {

    implicit val ec = client.config.executionContext

    val promise = Promise[Seq[AttributeResponse]]()

    val data: ByteBuf = {
      val requestData = encode(request, Buffers.unpooled())
      val routerRequest = MessageRouterRequest(serviceCode, requestPath, requestData)
      val unconnectedRequest = UnconnectedSendRequest(client.config.timeout, routerRequest, connectionPath)

      UnconnectedSendRequest.encode(unconnectedRequest, Buffers.unpooled())
    }

    client.sendUnconnectedData(data).onComplete {
      case Success(buffer) =>
        decode(request, buffer) match {
          case Success(response) => promise.success(response)
          case Failure(ex) => promise.failure(ex)
        }

      case Failure(ex) => promise.failure(ex)
    }

    promise.future
  }

  private def encode(request: Seq[Int], buffer: ByteBuf): ByteBuf = {
    // TODO
    buffer
  }

  private def decode(request: Seq[Int], buffer: ByteBuf): Try[Seq[AttributeResponse]] = {
    ??? // TODO
  }

}

object GetAttributeList {

  /**
   * @param id Attribute identifier.
   * @param status Status of the attribute response. 0x00 == Success.
   * @param data Attribute response. Only exists when status == 0x00.
   */
  case class AttributeResponse(id: Int, status: Int, data: Option[ByteBuf])

}



