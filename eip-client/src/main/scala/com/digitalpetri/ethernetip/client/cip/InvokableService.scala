package com.digitalpetri.ethernetip.client.cip

import io.netty.buffer.ByteBuf
import scala.concurrent.Future

trait InvokableService[T] {

  def response: Future[T]

  def getRequestData: ByteBuf
  def setResponseData(data: ByteBuf): Option[ByteBuf]
  def setResponseFailure(ex: Throwable)

}
