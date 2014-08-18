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

package com.digitalpetri.ethernetip.cip.logix

import org.scalatest.FunSuite

class SymbolTypeTest extends FunSuite {

  test("AtomicSymbolType is round-trip encodable/decodable") {
    for {
      tagType     <- Seq(CipBool(0), CipBool(1), CipSInt, CipInt, CipDInt, CipLInt, CipDWord, CipReal)
      dimensions  <- 1 to 3
      reserved    <- Seq(false, true)
    } yield {
      assertRoundTrip(AtomicSymbolType(tagType, dimensions, reserved))
    }
  }

  test("StructuredSymbolType is round-trip encodable/decodable") {
    for {
      instanceId  <- 0 to 0xFFF
      dimensions  <- 1 to 3
      reserved    <- Seq(false, true)
    } yield {
      assertRoundTrip(StructuredSymbolType(instanceId, dimensions, reserved))
    }
  }

  private def assertRoundTrip(symbolType: SymbolType) {
    val decoded = SymbolType.decode(SymbolType.encode(symbolType))

    assert(symbolType == decoded)
  }

}
