package com.digitalpetri.ethernetip.cip.services

import org.scalatest.FunSuite
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.ForwardOpenRequest
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import com.digitalpetri.ethernetip.cip.epath.{PortSegment, PaddedEPath}

class ForwardOpenRequestTest extends FunSuite {

  test("ForwardOpenRequest is round-trip encodable/decodable") {
    val request = ForwardOpenRequest(
      timeout = Duration(30, TimeUnit.SECONDS),
      connectionSerialNumber = 1,
      vendorId = 2,
      vendorSerialNumber = 3,
      connectionPath = PaddedEPath(PortSegment(1, Array[Byte](3))),
      o2tNetworkConnectionParameters = ForwardOpen.DefaultExplicitConnectionParameters,
      t2oNetworkConnectionParameters = ForwardOpen.DefaultExplicitConnectionParameters)

    val decoded = ForwardOpenRequest.decode(ForwardOpenRequest.encode(request))

    assert(request == decoded)
  }

}
