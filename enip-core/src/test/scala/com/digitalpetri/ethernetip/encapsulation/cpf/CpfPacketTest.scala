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

package com.digitalpetri.ethernetip.encapsulation.cpf

import com.digitalpetri.ethernetip.encapsulation.cpf.items.{NullAddressItem, UnconnectedDataItem}
import com.digitalpetri.ethernetip.util.Buffers
import org.scalatest.FunSuite

class CpfPacketTest extends FunSuite {

  test("CpfPacket is round-trip encodable/decodable") {
    val buffer = Buffers.unpooled().writeByte(42)
    val packet = CpfPacket(Seq(NullAddressItem(), UnconnectedDataItem(buffer)))
    val decoded = CpfPacket.decode(CpfPacket.encode(packet))
    buffer.readerIndex(0)

    assert (packet == decoded)
  }

  test("Decoding CpfPacket containing an unrecognized item throws exception") {
    val packet = CpfPacket(Seq(NullAddressItem()))
    val buffer = CpfPacket.encode(packet)

    // Judo chop the typeId bytes.
    buffer.setShort(2, 42)

    intercept[Exception] {
      CpfPacket.decode(buffer)
    }
  }

}
