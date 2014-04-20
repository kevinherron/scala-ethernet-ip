/*
 * EtherNet/IP
 * Copyright (C) 2014 Kevin Herron
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.digitalpetri.ethernetip.cip.epath

import org.scalatest.FunSuite

class PortSegmentTest extends FunSuite {

  test("PortSegment is round-trip encodable/decodable") {
    val segment = PortSegment(0, Array[Byte](0x01))
    val decoded = PortSegment.decode(PortSegment.encode(segment))

    assert(segment.portId == decoded.portId)
    assert(segment.linkAddress(0) == decoded.linkAddress(0))
  }

  test("PortSegment is round-trip encodable/decodable... extended port") {
    val segment = PortSegment(42, Array[Byte](0x01))
    val decoded = PortSegment.decode(PortSegment.encode(segment))

    assert(segment.portId == decoded.portId)
    assert(segment.linkAddress(0) == decoded.linkAddress(0))
  }

  test("PortSegment is round-trip encodable/decodable... extended link") {
    val segment = PortSegment(0, Array[Byte](0x01, 0x02))
    val decoded = PortSegment.decode(PortSegment.encode(segment))

    assert(segment.portId == decoded.portId)
    assert(segment.linkAddress(0) == decoded.linkAddress(0))
    assert(segment.linkAddress(1) == decoded.linkAddress(1))
  }

  test("PortSegment is round-trip encodable/decodable... extended port and link") {
    val segment = PortSegment(42, Array[Byte](0x01, 0x02))
    val decoded = PortSegment.decode(PortSegment.encode(segment))

    assert(segment.portId == decoded.portId)
    assert(segment.linkAddress(0) == decoded.linkAddress(0))
    assert(segment.linkAddress(1) == decoded.linkAddress(1))
  }

}
