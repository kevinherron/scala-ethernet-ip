package com.digitalpetri.ethernetip.client.cip.services

import com.digitalpetri.ethernetip.cip.epath.PaddedEPath
import com.digitalpetri.ethernetip.cip.{CipServiceCodes, MessageRouterResponse, MessageRouterRequest}
import com.digitalpetri.ethernetip.client.cip.InvokableService
import com.digitalpetri.ethernetip.client.cip.services.GetAttributeList.{GetAttributeListResponse, GetAttributeListRequest, AttributeRequest, AttributeResponse}
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.{Unpooled, ByteBuf}
import scala.concurrent.{Promise, Future}
import scala.util.{Success, Failure, Try}

class GetAttributeList(request: GetAttributeListRequest,
                       requestPath: PaddedEPath) extends InvokableService[GetAttributeListResponse] {

  private val promise = Promise[GetAttributeListResponse]()

  def response: Future[GetAttributeListResponse] = promise.future

  def getRequestData: ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = CipServiceCodes.GetAttributeList,
      requestPath = requestPath,
      requestData = encode(request))

    MessageRouterRequest.encode(routerRequest)
  }

  def setResponseData(data: ByteBuf): Option[ByteBuf] = {
    val responseTry = for {
      routerData  <- decodeMessageRouterResponse(data)
      response    <- decode(request, routerData)
    } yield response

    responseTry match {
      case Success(response) => promise.success(response)
      case Failure(ex) => promise.failure(ex)
    }

    None
  }

  def setResponseFailure(ex: Throwable): Unit = promise.failure(ex)

  private def encode(request: GetAttributeListRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(request.attributes.size)
    request.attributes.foreach(a => buffer.writeShort(a.id))

    buffer
  }

  private def decode(request: GetAttributeListRequest, buffer: ByteBuf): Try[GetAttributeListResponse] = Try {
    val count = buffer.readUnsignedShort()
    assert(count == request.attributes.size)

    val attributes = request.attributes.map(ar => readAttributeResponse(ar, buffer))

    GetAttributeListResponse(attributes)
  }

  private def readAttributeResponse(request: AttributeRequest, buffer: ByteBuf): AttributeResponse = {
    val id = buffer.readUnsignedShort()
    val status = buffer.readUnsignedShort()

    val data: Option[ByteBuf] = {
      if (status != 0x00) None
      else Some(buffer.readBytes(request.size))
    }

    AttributeResponse(id, status, data)
  }

  private def decodeMessageRouterResponse(buffer: ByteBuf): Try[ByteBuf] = Try {
    MessageRouterResponse.decode(buffer) match {
      case Success(response) =>
        if (response.generalStatus == 0x00) {
          response.data.getOrElse(Unpooled.EMPTY_BUFFER)
        } else {
          throw new Exception(s"status=${response.generalStatus} additional=${response.additionalStatus}")
        }
      case Failure(ex) => throw ex
    }
  }

}

object GetAttributeList {

  case class GetAttributeListRequest(attributes: Seq[AttributeRequest])
  case class GetAttributeListResponse(attributes: Seq[AttributeResponse])

  /**
   * @param id Attribute identifier.
   * @param size Size (in bytes) of the attribute response data.
   */
  case class AttributeRequest(id: Int, size: Int)

  /**
   * @param id Attribute identifier.
   * @param status Status of the attribute response. 0x00 == Success.
   * @param data Attribute response. Only exists when status == 0x00.
   */
  case class AttributeResponse(id: Int, status: Int, data: Option[ByteBuf])

}



