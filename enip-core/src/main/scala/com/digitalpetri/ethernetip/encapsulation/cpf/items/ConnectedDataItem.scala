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

import com.digitalpetri.ethernetip.encapsulation.cpf.CpfItem
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

/**
 * A data item that encapsulates a connected transport packet.
 *
 * The format of the “data” field is dependent on the encapsulated protocol. When used to encapsulate CIP, the format of
 * the “data” field is that of connected packet.
 *
 * @param data The transport packet.
 */
case class ConnectedDataItem(data: ByteBuf) extends CpfItem(ConnectedDataItem.TypeId)

object ConnectedDataItem {

  val TypeId = 0xB1

  def encode(item: ConnectedDataItem, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(item.typeId)

    // Length placeholder...
    val lengthStartIndex = buffer.writerIndex
    buffer.writeShort(0)

    // Encode the encapsulated data...
    val dataStartIndex = buffer.writerIndex
    buffer.writeBytes(item.data)

    // Go back and update the length.
    val bytesWritten = buffer.writerIndex - dataStartIndex
    buffer.markWriterIndex()
    buffer.writerIndex(lengthStartIndex)
    buffer.writeShort(bytesWritten)
    buffer.resetWriterIndex()

    buffer
  }

  def decode(buffer: ByteBuf): ConnectedDataItem = {
    val typeId = buffer.readUnsignedShort()
    val length = buffer.readUnsignedShort()

    assert(typeId == TypeId)

    // ConnectedDataItem and UnconnectedDataItem are a special case; they write the item data into an new, unpooled
    // buffer so the source buffer can be (automatically) returned to the pool after leaving the Netty IO thread.
    val index = buffer.readerIndex()
    val data  = Buffers.unpooled(length).writeBytes(buffer, index, length)
    buffer.readerIndex(index + length)

    ConnectedDataItem(data)
  }

}
