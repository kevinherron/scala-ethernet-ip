/*
 * Copyright 2014 Kevin Herron
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
