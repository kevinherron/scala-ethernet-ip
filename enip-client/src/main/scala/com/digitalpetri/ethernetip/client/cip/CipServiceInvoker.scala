package com.digitalpetri.ethernetip.client.cip

import com.digitalpetri.ethernetip.cip.services.MultipleServicePacket.MultipleServicePacketRequest
import com.digitalpetri.ethernetip.cip.services.UnconnectedSend.UnconnectedSendRequest
import com.digitalpetri.ethernetip.client.cip.services.{InvokableService, MultipleServicePacketService, UnconnectedSendService}
import io.netty.buffer.ByteBuf
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait CipServiceInvoker extends CipConnectionManager {
  this: CipClient =>

  private implicit val executionContext = config.executionContext

  def invokeService[T](service: InvokableService[T], connected: Boolean = false): Future[T] = {
    if (connected) {
      /*
       * Connected Explicit Message
       */
      reserveConnection().onComplete {
        case Success(connection) =>
          def _send(requestData: ByteBuf, o2tConnectionId: Int) {
            sendConnectedData(requestData, connection.o2tConnectionId).onComplete {
              case Success(responseData) =>
                service.setResponseData(responseData) match {
                  case Some(d) => _send(d, connection.o2tConnectionId)
                  case None => releaseConnection(connection)
                }

              case Failure(ex) =>
                service.setResponseFailure(ex)
            }
          }

          _send(service.getRequestData, connection.o2tConnectionId)

        case Failure(ex) => service.setResponseFailure(ex)
      }

    } else {
      /*
       * Unconnected Explicit Message
       */
      def _send(requestData: ByteBuf) {
        val request = UnconnectedSendRequest(
          timeout         = config.timeout,
          embeddedRequest = requestData,
          connectionPath  = config.connectionPath)

        val unconnectedService = new UnconnectedSendService(request)

        unconnectedService.response.onComplete {
          case Success(responseData) =>
            service.setResponseData(responseData).map(_send)

          case Failure(ex) =>
            service.setResponseFailure(ex)
        }

        sendUnconnectedData(unconnectedService.getRequestData).onComplete {
          case Success(responseData) => unconnectedService.setResponseData(responseData)
          case Failure(ex) => unconnectedService.setResponseFailure(ex)
        }
      }

      _send(service.getRequestData)
    }

    service.response
  }

  def invokeMultiple(services: Seq[InvokableService[_]], connected: Boolean) {
    def invoke(services: Seq[InvokableService[_]], requests: Seq[ByteBuf]) {
      assert(services.size == requests.size)

      if (services.isEmpty || requests.isEmpty) return

      val service = new MultipleServicePacketService(MultipleServicePacketRequest(requests))

      service.response.onComplete {
        case Success(response) =>
          val incomplete = services.zip(response.responses).flatMap {
            case (s, d) => s.setResponseData(d).map(next => (s, next))
          }

          /*
           * If any of the services need more data sent pack them together into another MultipleServicePacket and
           * keep sending again until no services need more data.
           */
          if (incomplete.nonEmpty) {
            val nextServices = incomplete.map(_._1)
            val nextRequests = incomplete.map(_._2)

            invoke(nextServices, nextRequests)
          }

        case Failure(ex) => services.foreach(_.setResponseFailure(ex))
      }

      invokeService(service, connected)
    }

    invoke(services, services.map(_.getRequestData))
  }

}


