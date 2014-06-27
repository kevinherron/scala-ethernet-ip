package com.digitalpetri.ethernetip.cip.services

import java.util.concurrent.TimeUnit

import com.digitalpetri.ethernetip.cip.epath.{PaddedEPath, PortSegment}
import com.digitalpetri.ethernetip.cip.services.ForwardClose.ForwardCloseRequest
import org.scalatest.FunSuite

import scala.concurrent.duration.Duration

class ForwardCloseRequestTest extends FunSuite {

  test("ForwardCloseRequest is round-trip encodable/decodable") {
    val request = ForwardCloseRequest(
      connectionTimeout                 = Duration(30, TimeUnit.SECONDS),
      connectionSerialNumber  = 1,
      originatorVendorId      = 2,
      originatorSerialNumber  = 3,
      connectionPath          = PaddedEPath(PortSegment(1, Array[Byte](3))))

    val decoded = ForwardCloseRequest.decode(ForwardCloseRequest.encode(request))

    assert(request === decoded)
  }

}
