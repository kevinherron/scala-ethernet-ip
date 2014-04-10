package com.digitalpetri.ethernetip.client.cip.services

import com.digitalpetri.ethernetip.cip.{CipConnection, EPath}
import com.digitalpetri.ethernetip.client.cip.CipClient
import scala.concurrent.Future

trait CipService[RequestType, ResponseType] {

  def serviceCode: Int

  def invoke(request: RequestType, requestPath: EPath, connection: CipConnection)
            (implicit client: CipClient): Future[ResponseType]

  def invoke(request: RequestType, requestPath: EPath, connectionPath: EPath)
            (implicit client: CipClient): Future[ResponseType]

}
