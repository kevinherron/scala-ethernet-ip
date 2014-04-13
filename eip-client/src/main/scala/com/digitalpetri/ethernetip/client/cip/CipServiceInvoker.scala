package com.digitalpetri.ethernetip.client.cip

import com.digitalpetri.ethernetip.cip.CipConnection
import com.digitalpetri.ethernetip.client.cip.services.UnconnectedSend
import com.digitalpetri.ethernetip.client.cip.services.UnconnectedSend.UnconnectedSendRequest
import io.netty.buffer.ByteBuf
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait CipServiceInvoker {
  this: CipClient =>

  def invokeService[T](service: InvokableService[T], connection: Option[CipConnection] = None): Future[T] = {
    connection match {
      case Some(c) =>
        /*
         * Connected Explicit Message
         */

        sendConnectedData(service.getRequestData, c.o2tConnectionId).onComplete {
          case Success(responseData) =>
            service.setResponseData(responseData).map(sendConnectedData(_, c.o2tConnectionId))

          case Failure(ex) =>
            service.setResponseFailure(ex)
        }

      case None =>
        /*
         * Unconnected Explicit Message
         */

        def sendRequestData(requestData: ByteBuf) {
          val request = UnconnectedSendRequest(
            timeout         = config.timeout,
            embeddedRequest = requestData,
            connectionPath  = config.connectionPath)

          val unconnectedService = new UnconnectedSend(request)

          unconnectedService.response.onComplete {
            case Success(responseData) =>
              service.setResponseData(responseData).map(sendRequestData)

            case Failure(ex) =>
              service.setResponseFailure(ex)
          }

          sendUnconnectedData(unconnectedService.getRequestData).onComplete {
            case Success(responseData) => unconnectedService.setResponseData(responseData)
            case Failure(ex) => unconnectedService.setResponseFailure(ex)
          }
        }

        sendRequestData(service.getRequestData)
    }

    service.response
  }

  def invokeMultiple(services: Seq[InvokableService[_]], connection: Option[CipConnection]) {

  }

}


