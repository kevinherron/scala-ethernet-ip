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
