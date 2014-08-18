/*
 * Copyright 2014 Kevin Herron
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalpetri.ethernetip.cip.logix.services

import com.digitalpetri.ethernetip.cip.logix.CipDInt
import com.digitalpetri.ethernetip.cip.logix.services.WriteTagService.WriteTagRequest
import com.digitalpetri.ethernetip.util.Buffers
import org.scalatest.FunSuite

class WriteTagRequestTest extends FunSuite {

  test("WriteTagRequest is round-tripe encodable/decodable") {
    val requestData = Buffers.unpooled().writeInt(42)

    val request = WriteTagRequest(
      tagType = CipDInt,
      elements = 1,
      data = requestData)

    val decoded = WriteTagRequest.decode(WriteTagRequest.encode(request))

    requestData.resetReaderIndex()
    assert(request == decoded)
  }

}
