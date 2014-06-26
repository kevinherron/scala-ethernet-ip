package com.digitalpetri.ethernetip.client.cip.services

import com.digitalpetri.ethernetip.cip.structs.MessageRouterResponse
import com.digitalpetri.ethernetip.client.cip.CipResponseException
import com.digitalpetri.ethernetip.util.Implicits.KestrelCombinator
import io.netty.buffer.ByteBuf

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

abstract class AbstractMultiInvokableService[T] extends InvokableService[T] {

  protected val promise = Promise[T]()

  def response: Future[T] = promise.future

  def setResponseData(data: ByteBuf): Option[ByteBuf] = {
    val responseTry = for {
      routerResponse  <- Try(decodeMessageRouterResponse(data))
      serviceResponse <- Try(decodeResponse(routerResponse))
    } yield serviceResponse

    responseTry match {
      case Success(response) =>
        response.tap(d => d.right.foreach(promise.success)).left.toOption
      case Failure(ex) =>
        None.tap(_ => promise.failure(ex))
    }
  }

  def setResponseFailure(ex: Throwable): Unit = promise.failure(ex)

  def decodeMessageRouterResponse(buffer: ByteBuf): MessageRouterResponse = {
    MessageRouterResponse.decode(buffer) match {
      case Success(response) =>
        response.generalStatus match {
          case 0x00 => response
          case 0x06 => response
          case _ => throw new CipResponseException(response.generalStatus, response.additionalStatus)
        }
      case Failure(ex) => throw ex
    }
  }

  /**
   * A [[MessageRouterResponse]] containing response data was received with a general status of either 0x00 (Success)
   * or 0x06 (Partial Data). Decode the data contained in the response and return either the next request data or a
   * decoded service response.
   * @param routerResponse The [[MessageRouterResponse]] containing the service response data.
   * @return Either the next request data or the decoded response.
   */
  def decodeResponse(routerResponse: MessageRouterResponse): Either[ByteBuf, T]

}
