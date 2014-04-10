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
 * The null address item shall contain only the type id and the length. The length shall be zero. No data shall follow
 * the length. Since the null address item contains no routing information, it shall be used when the protocol packet
 * itself contains any necessary routing information. The null address item shall be used for Unconnected Messages.
 */
case class NullAddressItem() extends CpfItem(NullAddressItem.TypeId)

object NullAddressItem {

  val TypeId = 0x00
  val Length = 0

  def encode(item: NullAddressItem, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(item.typeId)
    buffer.writeShort(Length)

    buffer
  }

  def decode(buffer: ByteBuf): NullAddressItem = {
    val typeId = buffer.readUnsignedShort()
    val length = buffer.readUnsignedShort()

    assert(typeId == TypeId)
    assert(length == Length)

    NullAddressItem()
  }

}
