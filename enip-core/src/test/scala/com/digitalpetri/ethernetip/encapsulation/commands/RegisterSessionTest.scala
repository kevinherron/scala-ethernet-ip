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

class RegisterSessionTest extends FunSuite {

  test("RegisterSession should use protocolVersion=1, optionFlags=0 by default") {
    val command = RegisterSession()
    assert(command.protocolVersion == 1)
    assert(command.optionFlags == 0)
  }

  test("RegisterSession is round-trip encodable/decodable") {
    val command = RegisterSession()
    val decoded = RegisterSession.decode(RegisterSession.encode(command))

    assert(command == decoded)
  }

}
