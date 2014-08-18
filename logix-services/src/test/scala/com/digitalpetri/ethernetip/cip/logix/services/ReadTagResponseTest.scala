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

import com.digitalpetri.ethernetip.cip.logix.services.ReadTagService.ReadTagResponse
import com.digitalpetri.ethernetip.cip.logix.{CipInt, CipBool, CipStructure}
import com.digitalpetri.ethernetip.util.Buffers
import org.scalatest.FunSuite
import scala.util.{Failure, Success}

class ReadTagResponseTest extends FunSuite {

  test("ReadTagResponse is round-trip encodable/decodable... CipBool") {
    for (i <- 0 to 7) {
      val tagData = Buffers.unpooled().writeByte(0x00).markReaderIndex()
      val response = ReadTagResponse(CipBool(0), tagData)

      ReadTagResponse.decode(ReadTagResponse.encode(response)) match {
        case Success(decoded) =>
          tagData.resetReaderIndex()
          assert(response == decoded)

        case Failure(ex) => throw ex
      }
    }
  }

  test("ReadTagResponse is round-trip encodable/decodable... CipInt") {
    for (i <- Short.MinValue to Short.MaxValue) {
      val tagData = Buffers.unpooled().writeShort(i).markReaderIndex()
      val response = ReadTagResponse(CipInt, tagData)

      ReadTagResponse.decode(ReadTagResponse.encode(response)) match {
        case Success(decoded) =>
          tagData.resetReaderIndex()
          assert(response == decoded)

        case Failure(ex) => throw ex
      }
    }
  }

  test("ReadTagResponse is round-trip encodable/decodable... CipStructure") {
    for (handle <- Short.MinValue to Short.MaxValue) {
      val tagData = Buffers.unpooled().writeShort(handle).markReaderIndex()
      val response = ReadTagResponse(CipStructure(handle), tagData)

      ReadTagResponse.decode(ReadTagResponse.encode(response)) match {
        case Success(decoded) =>
          tagData.resetReaderIndex()
          assert(response == decoded)

        case Failure(ex) => throw ex
      }
    }
  }

}
