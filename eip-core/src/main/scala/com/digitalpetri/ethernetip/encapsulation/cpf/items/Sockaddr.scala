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

import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf
import java.net.InetAddress
import java.nio.ByteOrder

case class Sockaddr(sinFamily: Int, sinPort: Int, sinAddr: InetAddress, sinZero: Long = 0)

object Sockaddr {

  def encode(sockaddr: Sockaddr, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.order(ByteOrder.BIG_ENDIAN).writeShort(sockaddr.sinFamily)
    buffer.order(ByteOrder.BIG_ENDIAN).writeShort(sockaddr.sinPort)
    buffer.order(ByteOrder.BIG_ENDIAN).writeBytes(sockaddr.sinAddr.getAddress)
    buffer.order(ByteOrder.BIG_ENDIAN).writeLong(sockaddr.sinZero)

    buffer
  }

  def decode(buffer: ByteBuf): Sockaddr = {
    Sockaddr(
      sinFamily = buffer.order(ByteOrder.BIG_ENDIAN).readUnsignedShort(),
      sinPort   = buffer.order(ByteOrder.BIG_ENDIAN).readUnsignedShort(),
      sinAddr   = InetAddress.getByAddress(buffer.order(ByteOrder.BIG_ENDIAN).readBytes(4).array()),
      sinZero   = buffer.order(ByteOrder.BIG_ENDIAN).readLong())
  }

}
