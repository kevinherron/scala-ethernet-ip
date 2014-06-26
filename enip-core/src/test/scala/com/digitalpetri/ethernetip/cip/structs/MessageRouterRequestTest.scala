package com.digitalpetri.ethernetip.cip.structs

import org.scalatest.FunSuite

class MessageRouterRequestTest extends FunSuite {

  // TODO Fix this
//  test("MessageRouterRequest is round-trip encodable/decodable") {
//    val data = Buffers.unpooled().writeBytes(Array[Byte](1, 2, 3)).markReaderIndex()
//
//    val request = MessageRouterRequest(
//      serviceCode = 0x42,
//      requestPath = PaddedEPath(ClassId(1), InstanceId(1)),
//      requestData = data)
//
//    val decoded = MessageRouterRequest.decode(MessageRouterRequest.encode(request))
//
//    data.resetReaderIndex()
//    assert(request == decoded)
//  }

}
