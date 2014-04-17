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

package com.digitalpetri.ethernetip.client.cip

import com.digitalpetri.ethernetip.cip.CipStatusCodes

class CipResponseException(val status: Short, val additionalStatus: Seq[Short]) extends Exception {

  override def getMessage: String = {
    val sb = new StringBuilder

    sb.append(f"status=0x$status%02X")

    CipStatusCodes.getName(status).foreach(s => sb.append(s" ($s)"))

    val additional = additionalStatus.map(status => f"0x$status%04X").mkString("[", ",", "]")
    sb.append(s", additional=$additional")

    sb.toString()
  }

}
