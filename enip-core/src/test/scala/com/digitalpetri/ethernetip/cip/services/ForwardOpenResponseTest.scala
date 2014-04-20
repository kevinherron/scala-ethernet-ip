package com.digitalpetri.ethernetip.cip.services

import com.digitalpetri.ethernetip.cip.services.ForwardOpen.ForwardOpenResponse
import io.netty.buffer.Unpooled
import org.scalatest.FunSuite

class ForwardOpenResponseTest extends FunSuite {

  test("ForwardOpenResponse is round-trip encodable/decodable") {
    val response = ForwardOpenResponse(
      o2tConnectionId = 0,
      t2oConnectionId = 1,
      connectionSerialNumber = 2,
      originatorVendorId = 3,
      originatorSerialNumber = 4,
      o2tActualPacketInterval = 5,
      t2oActualPacketInterval = 6,
      applicationReplySize = 0,
      reserved = 7,
      applicationReply = Unpooled.EMPTY_BUFFER)

    val decoded = ForwardOpenResponse.decode(ForwardOpenResponse.encode(response)).getOrElse(null)

    assert(response == decoded)
  }

}
