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

class SimpleDataSegmentTest extends FunSuite {

  test("SimpleDataSegment is round-trip encodable/decodable") {
    val segment = SimpleDataSegment(Seq(Short.MinValue, 0, 1, 2, Short.MaxValue))
    val decoded = SimpleDataSegment.decode(SimpleDataSegment.encode(segment))

    assert(segment == decoded)
  }

}
