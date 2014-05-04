package com.digitalpetri.ethernetip.cip.services

import com.digitalpetri.ethernetip.cip.services.MultipleServicePacket.MultipleServicePacketResponse
import com.digitalpetri.ethernetip.util.Buffers
import org.scalatest.FunSuite

class MultipleServicePacketResponseTest extends FunSuite {

  test("MultipleServicePacketResponse is round-trip encodable/decodable") {
    val serviceResponses = Seq(
      Buffers.unpooled().writeBytes(Array[Byte](0, 1, 2)),
      Buffers.unpooled().writeBytes(Array[Byte](3, 4, 5, 6)),
      Buffers.unpooled().writeBytes(Array[Byte](7, 8, 9, 10, 11)))

    val response = MultipleServicePacketResponse(serviceResponses)
    val decoded = MultipleServicePacketResponse.decode(MultipleServicePacketResponse.encode(response))

    serviceResponses.foreach(_.readerIndex(0))

    assert(response == decoded)
  }

}
