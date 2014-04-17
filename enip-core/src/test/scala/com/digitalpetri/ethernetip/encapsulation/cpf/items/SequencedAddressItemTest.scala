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

package com.digitalpetri.ethernetip.encapsulation.cpf.items

import org.scalatest.FunSuite

class SequencedAddressItemTest extends FunSuite {

  val item = SequencedAddressItem(1, 1234)

  test("SequencedAddressItem typeId == 0x8002") {
    assert(item.typeId == 0x8002)
  }

  test("SequencedAddressItem is round-trip encodable/decodable") {
    val decoded = SequencedAddressItem.decode(SequencedAddressItem.encode(item))

    assert(item == decoded)
  }

}
