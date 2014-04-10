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

import org.scalatest.FunSuite

class ListInterfacesTest extends FunSuite {

  test("ListInterfaces request is round-trip encodable/decodable") {
    val command = ListInterfaces()
    val decoded = ListInterfaces.decode(ListInterfaces.encode(command))

    assert(command == decoded)
  }

  test("ListInterfaces response is round-trip encodable/decodable") {
    val command = ListInterfaces(List(InterfaceInformation(0)(Array[Byte](0))))
    val decoded = ListInterfaces.decode(ListInterfaces.encode(command))

    assert(command == decoded)
  }

}
