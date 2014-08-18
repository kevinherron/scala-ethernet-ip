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

import com.digitalpetri.ethernetip.cip.logix.services.GetInstanceAttributeListService.GetInstanceAttributeListResponse
import com.digitalpetri.ethernetip.cip.logix.{SymbolInstance, StructuredSymbolType, AtomicSymbolType, CipDInt}
import org.scalatest.FunSuite

class GetInstanceAttributeListResponseTest extends FunSuite {

  test("GetInstanceAttributeListResponse is round-trip encodable/decodable") {
    val program = Some("Program:Main")

    val symbols = Seq(
      SymbolInstance(1, "a", AtomicSymbolType(CipDInt), 0, 0, 0, program),
      SymbolInstance(2, "b", StructuredSymbolType(42), 3, 5, 7, program))

    val response = GetInstanceAttributeListResponse(symbols)
    val decoded = GetInstanceAttributeListResponse.decode(GetInstanceAttributeListResponse.encode(response), program)

    assert(response == decoded)
  }

}
