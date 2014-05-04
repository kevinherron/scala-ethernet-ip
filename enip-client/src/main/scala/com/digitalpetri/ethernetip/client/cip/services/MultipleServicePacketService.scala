package com.digitalpetri.ethernetip.client.cip.services

import com.digitalpetri.ethernetip.cip.CipServiceCodes
import com.digitalpetri.ethernetip.cip.services.MultipleServicePacket
import com.digitalpetri.ethernetip.cip.services.MultipleServicePacket.{MultipleServicePacketResponse, MultipleServicePacketRequest}
import com.digitalpetri.ethernetip.cip.structs.{MessageRouterResponse, MessageRouterRequest}
import io.netty.buffer.{Unpooled, ByteBuf}
import scala.util.{Failure, Success, Try}
import com.digitalpetri.ethernetip.client.cip.CipResponseException

class MultipleServicePacketService(request: MultipleServicePacketRequest)
  extends AbstractInvokableService[MultipleServicePacketResponse] {

  def getRequestData: ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = CipServiceCodes.MultipleServicePacket,
      requestPath = MultipleServicePacket.RequestPath,
      requestData = MultipleServicePacketRequest.encode(request))

    MessageRouterRequest.encode(routerRequest)
  }


  override def decodeMessageRouterResponse(buffer: ByteBuf): Try[ByteBuf] = Try {
    MessageRouterResponse.decode(buffer) match {
      case Success(response) =>
        if (response.generalStatus == 0x00 || response.generalStatus == 0x1E) {
          response.data.getOrElse(Unpooled.EMPTY_BUFFER)
        } else {
          throw new CipResponseException(response.generalStatus, response.additionalStatus)
        }
      case Failure(ex) => throw ex
    }
  }

  /**
   * Decode `responseData` and return a response.
   * @param responseData the [[ByteBuf]] containing the response data.
   * @return a decoded response.
   */
  override def decodeResponse(responseData: ByteBuf): Try[MultipleServicePacketResponse] = Try {
    MultipleServicePacketResponse.decode(responseData)
  }

}


