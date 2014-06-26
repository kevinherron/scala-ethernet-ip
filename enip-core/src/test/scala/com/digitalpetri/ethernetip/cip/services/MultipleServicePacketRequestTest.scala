package com.digitalpetri.ethernetip.cip.services

import com.digitalpetri.ethernetip.cip.services.MultipleServicePacket.MultipleServicePacketRequest
import com.digitalpetri.ethernetip.util.Buffers
import org.scalatest.FunSuite

class MultipleServicePacketRequestTest extends FunSuite {

  test("MultipleServicePacketRequest is round-trip encodable/decodable") {
    val serviceRequests = Seq(
      Buffers.unpooled().writeBytes(Array[Byte](0, 1, 2)),
      Buffers.unpooled().writeBytes(Array[Byte](3, 4, 5, 6)),
      Buffers.unpooled().writeBytes(Array[Byte](7, 8, 9, 10, 11)))

    val request = MultipleServicePacketRequest(serviceRequests)
    val decoded = MultipleServicePacketRequest.decode(MultipleServicePacketRequest.encode(request))

    serviceRequests.foreach(_.readerIndex(0))

    assert (request == decoded)
  }

}
