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
    buffer.writeShort(command.interfaces.size)
    command.interfaces.foreach(encodeInterfaceInformation(_, buffer))

    buffer
  }

  def decode(buffer: ByteBuf): ListInterfaces = {
    val itemCount = buffer.readUnsignedShort()

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
