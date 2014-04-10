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
 * This address item shall be used when the encapsulated protocol is connection-oriented. The data shall contain a
 * connection identifier.
 *
 * @param connectionId The connection identifier, exchanged in the Forward Open service of the Connection Manager.
 */
case class ConnectedAddressItem(connectionId: Long) extends CpfItem(ConnectedAddressItem.TypeId)

object ConnectedAddressItem {

  val TypeId = 0xA1
  val Length = 4

  def encode(item: ConnectedAddressItem, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(item.typeId)
    buffer.writeShort(Length)
    buffer.writeInt(item.connectionId.toInt)

    buffer
  }

  def decode(buffer: ByteBuf): ConnectedAddressItem = {
    val typeId        = buffer.readUnsignedShort()
    val length        = buffer.readUnsignedShort()
    val connectionId  = buffer.readUnsignedInt()

    assert(typeId == TypeId)
    assert(length == Length)

    ConnectedAddressItem(connectionId)
  }

}
