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

import com.digitalpetri.ethernetip.encapsulation.cpf.CpfItem
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf


/**
 * The Sockaddr Info items shall be used to communicate IP address or port information necessary to create Class 0 or
 * Class 1 connections. There are separate items for originator-to- target and target-to-originator socket information.
 *
 * The items are present as additional data in Forward_Open / Large_Forward_Open request and reply services encapsulated
 * in a SendRRData message.
 */
sealed trait SockaddrItem extends CpfItem {
  val sockaddr: Sockaddr
}

case class SockaddrItemO2t(sockaddr: Sockaddr) extends CpfItem(SockaddrItem.TypeIdO2t) with SockaddrItem
case class SockaddrItemT2o(sockaddr: Sockaddr) extends CpfItem(SockaddrItem.TypeIdT2o) with SockaddrItem

object SockaddrItem {

  val TypeIdO2t = 0x8000
  val TypeIdT2o = 0x8001
  val Length = 16

  def encode(item: SockaddrItem, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(item.typeId)
    buffer.writeShort(Length)

    Sockaddr.encode(item.sockaddr, buffer)
  }

  def decode(buffer: ByteBuf): SockaddrItem = {
    val typeId    = buffer.readUnsignedShort()
    val length    = buffer.readUnsignedShort()

    assert(typeId == TypeIdO2t || typeId == TypeIdT2o)
    assert(length == Length)

    val sockaddr = Sockaddr.decode(buffer)

    typeId match {
      case TypeIdO2t => SockaddrItemO2t(sockaddr)
      case TypeIdT2o => SockaddrItemT2o(sockaddr)
      case _ => throw new Exception(s"invalid SockAddrItem type: $typeId")
    }
  }

}
