package com.digitalpetri.ethernetip.client.cip.services

import com.digitalpetri.ethernetip.cip.CipClassCodes
import com.digitalpetri.ethernetip.cip.epath.{ClassId, InstanceId, PaddedEPath}
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.ForwardOpenResponse
import com.digitalpetri.ethernetip.cip.services.LargeForwardOpen
import com.digitalpetri.ethernetip.cip.services.LargeForwardOpen.LargeForwardOpenRequest
import com.digitalpetri.ethernetip.cip.structs.MessageRouterRequest
import io.netty.buffer.ByteBuf

import scala.util.Try

class LargeForwardOpenService(request: LargeForwardOpenRequest) extends AbstractInvokableService[ForwardOpenResponse] {

  private val requestPath = PaddedEPath(
    ClassId(CipClassCodes.ConnectionManagerObject),
    InstanceId(0x01))

  def getRequestData: ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = LargeForwardOpen.ServiceCode,
      requestPath = requestPath,
      requestData = LargeForwardOpenRequest.encode(request))

    MessageRouterRequest.encode(routerRequest)
  }

  /**
   * Decode `responseData` and return a response.
   * @param responseData the [[ByteBuf]] containing the response data.
   * @return a decoded response.
   */
  def decodeResponse(responseData: ByteBuf): Try[ForwardOpenResponse] = {
    ForwardOpenResponse.decode(responseData)
  }

}
