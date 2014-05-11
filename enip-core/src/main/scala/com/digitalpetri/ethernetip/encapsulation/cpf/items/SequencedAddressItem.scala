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
 * This address item shall be used for CIP transport class 0 and class 1 connected data. The data shall contain a
 * connection identifier and a sequence number.
 * 
 * @param connectionId Connection identifier.
 * @param sequenceNumber Sequence number.
 */
case class SequencedAddressItem(connectionId: Long, sequenceNumber: Long) extends CpfItem(SequencedAddressItem.TypeId)

object SequencedAddressItem {

  val TypeId = 0x8002
  val Length = 8

  def encode(item: SequencedAddressItem, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(item.typeId)
    buffer.writeShort(Length)
    buffer.writeInt(item.connectionId.toInt)
    buffer.writeInt(item.sequenceNumber.toInt)
  }

  def decode(buffer: ByteBuf): SequencedAddressItem = {
    val typeId          = buffer.readUnsignedShort()
    val length          = buffer.readUnsignedShort()
    val connectionId    = buffer.readUnsignedInt()
    val sequenceNumber  = buffer.readUnsignedInt()

    assert(typeId == TypeId)
    assert(length == Length)

    SequencedAddressItem(connectionId, sequenceNumber)
  }

}
