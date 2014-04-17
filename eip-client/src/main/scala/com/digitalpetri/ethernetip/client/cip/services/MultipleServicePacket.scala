package com.digitalpetri.ethernetip.client.cip.services

import com.digitalpetri.ethernetip.cip.epath.{InstanceId, ClassId, PaddedEPath}
import com.digitalpetri.ethernetip.cip.structs.MessageRouterRequest
import com.digitalpetri.ethernetip.cip.{CipClassCodes, CipServiceCodes}
import com.digitalpetri.ethernetip.client.cip.services.MultipleServicePacket.{MultipleServicePacketResponse, MultipleServicePacketRequest}
import io.netty.buffer.ByteBuf
import scala.concurrent.{Promise, Future}

class MultipleServicePacket(request: MultipleServicePacketRequest)
  extends InvokableService[MultipleServicePacketResponse] {

  private val promise = Promise[MultipleServicePacketResponse]()

  def response: Future[MultipleServicePacketResponse] = promise.future

  def getRequestData: ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = CipServiceCodes.MultipleServicePacket,
      requestPath = MultipleServicePacket.RequestPath,
      requestData = ???)

    MessageRouterRequest.encode(routerRequest)
  }

  def setResponseData(data: ByteBuf): Option[ByteBuf] = ???

  def setResponseFailure(ex: Throwable): Unit = ???

}

object MultipleServicePacket {

  val RequestPath = PaddedEPath(
    ClassId(CipClassCodes.MessageRouterObject),
    InstanceId(0x01))

  case class MultipleServicePacketRequest(requests: Seq[ByteBuf])
  case class MultipleServicePacketResponse(responses: Seq[ByteBuf])

}
