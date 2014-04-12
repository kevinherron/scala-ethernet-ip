package com.digitalpetri.ethernetip.client.cip.services

import com.digitalpetri.ethernetip.cip.epath.PaddedEPath
import com.digitalpetri.ethernetip.cip.services.GetAttributeListService.{AttributeRequest, GetAttributeListRequest, GetAttributeListResponse}
import com.digitalpetri.ethernetip.cip.{CipServiceCodes, MessageRouterResponse, MessageRouterRequest}
import com.digitalpetri.ethernetip.client.cip.InvokableService
import io.netty.buffer.{Unpooled, ByteBuf}
import scala.concurrent.{Promise, Future}
import scala.util.{Success, Failure, Try}

class GetAttributeList(attributes: Seq[AttributeRequest],
                       attributeSizes: Seq[Int],
                       requestPath: PaddedEPath) extends InvokableService[GetAttributeListResponse] {

  assert(attributes.size == attributeSizes.size)

  private val promise = Promise[GetAttributeListResponse]()

  def response: Future[GetAttributeListResponse] = promise.future

  def getRequestData: ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = CipServiceCodes.GetAttributeList,
      requestPath = requestPath,
      requestData = GetAttributeListRequest.encode(GetAttributeListRequest(attributes)))

    MessageRouterRequest.encode(routerRequest)
  }

  def setResponseData(data: ByteBuf): Option[ByteBuf] = {
    val responseTry = for {
      routerData  <- decodeMessageRouterResponse(data)
      response    <- GetAttributeListResponse.decode(attributeSizes, routerData)
    } yield response

    responseTry match {
      case Success(response) => promise.success(response)
      case Failure(ex) => promise.failure(ex)
    }

    None
  }

  def setResponseFailure(ex: Throwable): Unit = promise.failure(ex)

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



