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

import java.net.InetAddress

import org.scalatest.FunSuite

class CipIdentityItemTest extends FunSuite {

  val item = CipIdentityItem(
    protocolVersion = 1,
    socketAddress = Sockaddr(0, 44818, InetAddress.getLocalHost, 0),
    vendorId = 2,
    deviceType = 3,
    productCode = 4,
    revisionMajor = 5,
    revisionMinor = 6,
    status = 7,
    serialNumber = 8,
    productName = "digitalpetri EtherNet/IP",
    state = 9)

  test("CipIdentityItem typeId == 0x0C") {
    assert(item.typeId == 0x0C)
  }

  test("CipIdentityItem is round-trip encodable/decodable") {
    val decoded = CipIdentityItem.decode(CipIdentityItem.encode(item))

    assert(item == decoded)
  }

}
