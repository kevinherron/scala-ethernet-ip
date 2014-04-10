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

package com.digitalpetri.ethernetip.encapsulation.commands

import com.digitalpetri.ethernetip.encapsulation.cpf.CpfPacket
import com.digitalpetri.ethernetip.encapsulation.cpf.items.ConnectedAddressItem
import org.scalatest.FunSuite

class SendUnitDataTest extends FunSuite {

  test("SendUnitData should use interfaceHandle=0, timeout=0 by default") {
    val command = SendUnitData(packet = CpfPacket(List.empty))

    assert(command.interfaceHandle == 0)
    assert(command.timeout == 0)
  }

  test("SendUnitData is round-trip encodable/decodable") {
    val command = SendUnitData(packet = CpfPacket(List(ConnectedAddressItem(42))))
    val decoded = SendUnitData.decode(SendUnitData.encode(command))

    assert(command == decoded)
  }

}
