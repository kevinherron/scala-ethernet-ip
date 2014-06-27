package com.digitalpetri.ethernetip.client.cip.services

import com.digitalpetri.ethernetip.cip.CipClassCodes
import com.digitalpetri.ethernetip.cip.epath.{ClassId, InstanceId, PaddedEPath}
import com.digitalpetri.ethernetip.cip.services.ForwardClose
import com.digitalpetri.ethernetip.cip.services.ForwardClose.{ForwardCloseRequest, ForwardCloseResponse}
import com.digitalpetri.ethernetip.cip.structs.MessageRouterRequest
import io.netty.buffer.ByteBuf

import scala.util.Try

class ForwardCloseService(request: ForwardCloseRequest) extends AbstractInvokableService[ForwardCloseResponse] {

  private val requestPath = PaddedEPath(
    ClassId(CipClassCodes.ConnectionManagerObject),
    InstanceId(0x01))

  def getRequestData: ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = ForwardClose.ServiceCode,
      requestPath = requestPath,
      requestData = ForwardCloseRequest.encode(request))

    MessageRouterRequest.encode(routerRequest)
  }

  /**
   * Decode `responseData` and return a response.
   * @param responseData the [[ByteBuf]] containing the response data.
   * @return a decoded response.
   */
  def decodeResponse(responseData: ByteBuf): Try[ForwardCloseResponse] = {
    // TODO Decode this once implemented
    Try(ForwardCloseResponse())
  }

}
