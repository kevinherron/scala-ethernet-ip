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

package com.digitalpetri.ethernetip.cip.services

import org.scalatest.FunSuite
import com.digitalpetri.ethernetip.cip.services.ForwardOpen.NetworkConnectionParameters

class NetworkConnectionParametersTest extends FunSuite {

  test("NetworkConnectionParameters implicitly converts to and from Int") {
    val parameters = ForwardOpen.DefaultExplicitConnectionParameters
    assert(parameters == params2int2params(parameters))

    val parameters2 = ForwardOpen.DefaultO2tIoConnectionParameters
    assert(parameters2 == params2int2params(parameters2))

    val parameters3 = ForwardOpen.DefaultT2oIoConnectionParameters
    assert(parameters3 == parameters3)
  }

  private def params2int2params(parameters: NetworkConnectionParameters): NetworkConnectionParameters = {
    val i: Int = parameters
    val p: NetworkConnectionParameters = i
    p
  }

}
