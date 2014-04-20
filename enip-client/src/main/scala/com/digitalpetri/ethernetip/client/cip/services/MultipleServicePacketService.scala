package com.digitalpetri.ethernetip.client.cip.services

import com.digitalpetri.ethernetip.cip.CipServiceCodes
import com.digitalpetri.ethernetip.cip.services.MultipleServicePacket
import com.digitalpetri.ethernetip.cip.services.MultipleServicePacket.{MultipleServicePacketResponse, MultipleServicePacketRequest}
import com.digitalpetri.ethernetip.cip.structs.MessageRouterRequest
import io.netty.buffer.ByteBuf
import scala.concurrent.{Promise, Future}

class MultipleServicePacketService(request: MultipleServicePacketRequest)
  extends InvokableService[MultipleServicePacketResponse] {

  private val promise = Promise[MultipleServicePacketResponse]()

  def response: Future[MultipleServicePacketResponse] = promise.future

  def getRequestData: ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = CipServiceCodes.MultipleServicePacket,
      requestPath = MultipleServicePacket.RequestPath,
      requestData = MultipleServicePacketRequest.encode(request))

    MessageRouterRequest.encode(routerRequest)
  }

  def setResponseData(data: ByteBuf): Option[ByteBuf] = ???

  def setResponseFailure(ex: Throwable): Unit = ???

}


