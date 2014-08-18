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

class TagTypeTest extends FunSuite {

  test("CipBool is round-trip encodable/decodable") {
    for (i <- 0 to 7) {
      assertRoundTrip(CipBool(i))
    }
  }

  test("CipSInt is round-trip encodable/decodable") {
    assertRoundTrip(CipSInt)
  }

  test("CipInt is round-trip encodable/decodable") {
    assertRoundTrip(CipInt)
  }

  test("CipDInt is round-trip encodable/decodable") {
    assertRoundTrip(CipDInt)
  }

  test("CipLInt is round-trip encodable/decodable") {
    assertRoundTrip(CipLInt)
  }

  test("CipDWord is round-trip encodable/decodable") {
    assertRoundTrip(CipDWord)
  }

  test("CipReal is round-trip encodable/decodable") {
    assertRoundTrip(CipReal)
  }

  test("LogixProgram is round-trip encodable/decodable") {
    assertRoundTrip(LogixProgram)
  }

  private def assertRoundTrip(tagType: TagType) {
    val decoded = TagType.decode(TagType.encode(tagType))
    assert(tagType == decoded)
  }

}
