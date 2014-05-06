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
import io.netty.util.ReferenceCountUtil

/**
 * A data item that encapsulates an unconnected message.
 *
 * The format of the “data” field is dependent on the encapsulated protocol. When used to encapsulate CIP, the format of
 * the “data” field is that of a Message Router request or Message Router reply.
 *
 * The context field in the encapsulation header shall be used for unconnected request/reply matching.
 *
 * @param data The unconnected message.
 */
case class UnconnectedDataItem(data: ByteBuf) extends CpfItem(UnconnectedDataItem.TypeId)

object UnconnectedDataItem {

  val TypeId = 0xB2

  def encode(item: UnconnectedDataItem, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(item.typeId)

    // Length placeholder...
    val lengthStartIndex = buffer.writerIndex
    buffer.writeShort(0)

    // Encode the encapsulated data...
    val dataStartIndex = buffer.writerIndex
    buffer.writeBytes(item.data)
    ReferenceCountUtil.release(item.data)

    // Go back and update the length.
    val bytesWritten = buffer.writerIndex - dataStartIndex
    buffer.markWriterIndex()
    buffer.writerIndex(lengthStartIndex)
    buffer.writeShort(bytesWritten)
    buffer.resetWriterIndex()
  }

  def decode(buffer: ByteBuf): UnconnectedDataItem = {
    val typeId = buffer.readUnsignedShort()
    val length = buffer.readUnsignedShort()

    assert(typeId == TypeId)

    // ConnectedDataItem and UnconnectedDataItem are a special case; they write the item data into an new, unpooled
    // buffer so the source buffer can be (automatically) returned to the pool after leaving the Netty IO thread.
    val index = buffer.readerIndex()
    val data  = Buffers.unpooled(length).writeBytes(buffer, index, length)
    buffer.readerIndex(index + length)

    UnconnectedDataItem(data)
  }

}
