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
 * This address item shall be used when the encapsulated protocol is connection-oriented. The data shall contain a
 * connection identifier.
 *
 * @param connectionId The connection identifier, exchanged in the Forward Open service of the Connection Manager.
 */
case class ConnectedAddressItem(connectionId: Int) extends CpfItem(ConnectedAddressItem.TypeId)

object ConnectedAddressItem {

  val TypeId = 0xA1
  val Length = 4

  def encode(item: ConnectedAddressItem, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(item.typeId)
    buffer.writeShort(Length)
    buffer.writeInt(item.connectionId)

    buffer
  }

  def decode(buffer: ByteBuf): ConnectedAddressItem = {
    val typeId        = buffer.readUnsignedShort()
    val length        = buffer.readUnsignedShort()
    val connectionId  = buffer.readInt()

    assert(typeId == TypeId)
    assert(length == Length)

    ConnectedAddressItem(connectionId)
  }

}
