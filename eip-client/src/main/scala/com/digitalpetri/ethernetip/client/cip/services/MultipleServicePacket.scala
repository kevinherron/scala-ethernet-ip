package com.digitalpetri.ethernetip.client.cip.services

import com.digitalpetri.ethernetip.cip.{MessageRouterResponse, MessageRouterRequest, CipConnection, EPath}
import com.digitalpetri.ethernetip.client.cip.{InvokableService, CipClient}
import com.digitalpetri.ethernetip.client.cip.services.MultipleServicePacket.{MultipleServicePacketResponse, MultipleServicePacketRequest}
import scala.concurrent.{Promise, Future}
import io.netty.buffer.ByteBuf
import scala.util.Try

class MultipleServicePacket(request: MultipleServicePacketRequest, requestPath: EPath)
  extends InvokableService[MultipleServicePacketResponse] {

  private val promise = Promise[MultipleServicePacketResponse]()

  def response: Future[MultipleServicePacketResponse] = promise.future

  def getRequestData: ByteBuf = ???

  def setResponseData(data: ByteBuf): Option[ByteBuf] = ???

  def setResponseFailure(ex: Throwable): Unit = ???

}

object MultipleServicePacket {

  val ServiceCode = 0x0A

  case class MultipleServicePacketRequest(requests: Seq[MessageRouterRequest])
  case class MultipleServicePacketResponse(responses: Seq[MessageRouterResponse])

}
