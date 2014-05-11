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

package com.digitalpetri.ethernetip.encapsulation.commands

import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

/**
 * The optional List Interfaces command shall be used by a connection originator to identify non-CIP communication
 * interfaces associated with the target. A session need not be established to send this command.
 */
case class ListInterfaces(interfaces: Seq[InterfaceInformation] = List.empty[InterfaceInformation])
  extends Command(ListInterfacesCode)

case class InterfaceInformation(itemId: Int)(val data: Array[Byte])

object ListInterfaces {

  def encode(command: ListInterfaces, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    if (command.interfaces.nonEmpty) {
      buffer.writeShort(command.interfaces.size)
      command.interfaces.foreach(encodeInterfaceInformation(_, buffer))
    }

    buffer
  }

  def decode(buffer: ByteBuf): ListInterfaces = {
    val itemCount = {
      if (buffer.readableBytes() >= 2) buffer.readUnsignedShort()
      else 0
    }

    def decodeItems(items: List[InterfaceInformation], itemCount: Int): List[InterfaceInformation] = {
      if (itemCount == 0) items
      else decodeItems(items :+ decodeInterfaceInformation(buffer), itemCount - 1)
    }

    ListInterfaces(decodeItems(List(), itemCount))
  }

  private def encodeInterfaceInformation(interface: InterfaceInformation, buffer: ByteBuf) {
    buffer.writeShort(interface.itemId)
    buffer.writeShort(interface.data.length)
    buffer.writeBytes(interface.data)
  }

  private def decodeInterfaceInformation(buffer: ByteBuf): InterfaceInformation = {
    val itemId = buffer.readUnsignedShort()
    val itemLength = buffer.readUnsignedShort()
    val data = buffer.readBytes(itemLength).array()

    InterfaceInformation(itemId)(data)
  }

}
