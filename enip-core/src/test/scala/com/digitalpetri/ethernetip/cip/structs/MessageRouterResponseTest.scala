package com.digitalpetri.ethernetip.cip.structs

import com.digitalpetri.ethernetip.util.Buffers
import org.scalatest.FunSuite

class MessageRouterResponseTest extends FunSuite {

  test("MessageRouterResponse is round-trip encodable/decodable... generalStatus == 0x00") {
    val data = Buffers.unpooled().writeBytes(Array[Byte](1, 2, 3)).markReaderIndex()

    val response = MessageRouterResponse(
      serviceCode = 0x42,
      generalStatus = 0x00,
      additionalStatus = Seq.empty[Short],
      data = Some(data))

    val decoded = MessageRouterResponse.decode(MessageRouterResponse.encode(response))

    data.resetReaderIndex()
    assert(response == decoded.getOrElse(null))
  }

  test("MessageRouterResponse is round-trip encodable/decodable... generalStatus != 0x00, additionalStatus == []") {
    val data = Buffers.unpooled().writeBytes(Array[Byte](1, 2, 3)).markReaderIndex()

    val response = MessageRouterResponse(
      serviceCode = 0x42,
      generalStatus = 0x01,
      additionalStatus = Seq.empty[Short],
      data = Some(data))

    val decoded = MessageRouterResponse.decode(MessageRouterResponse.encode(response))

    data.resetReaderIndex()
    assert(response == decoded.getOrElse(null))
  }

  test("MessageRouterResponse is round-trip encodable/decodable... generalStatus != 0x00, additionalStatus == [0x0001, 0x0002]") {
    val data = Buffers.unpooled().writeBytes(Array[Byte](1, 2, 3)).markReaderIndex()

    val response = MessageRouterResponse(
      serviceCode = 0x42,
      generalStatus = 0x01,
      additionalStatus = Seq(0x01, 0x02),
      data = Some(data))

    val decoded = MessageRouterResponse.decode(MessageRouterResponse.encode(response))

    data.resetReaderIndex()
    assert(response == decoded.getOrElse(null))
  }

}
