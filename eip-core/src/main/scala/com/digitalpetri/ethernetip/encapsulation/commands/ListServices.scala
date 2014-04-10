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
 * The ListServices command shall determine which encapsulation service classes the target device supports. The
 * ListServices command does not require that a session be established.
 *
 * Each service class has a unique type code, and an optional ASCII name.
 */
case class ListServices(services: Seq[ServiceInformation] = Seq.empty) extends Command(ListServicesCode)

case class ServiceInformation(itemId: Int, version: Int, capabilities: Int, name: String)

object ListServices {

  def encode(command: ListServices, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(command.services.size)
    command.services.foreach(encodeServiceInformation(_, buffer))

    buffer
  }

  def decode(buffer: ByteBuf): ListServices = {
    val itemCount = buffer.readUnsignedShort()

    def decodeItems(items: List[ServiceInformation], itemCount: Int): List[ServiceInformation] = {
      if (itemCount == 0) items
      else decodeItems(items :+ decodeServiceInformation(buffer), itemCount - 1)
    }

    ListServices(decodeItems(List(), itemCount))
  }

  private def encodeServiceInformation(service: ServiceInformation, buffer: ByteBuf) {
    buffer.writeShort(service.itemId)

    val lengthStartIndex = buffer.writerIndex()
    buffer.writeShort(0)

    val dataStartIndex = buffer.writerIndex()
    buffer.writeShort(service.version)
    buffer.writeShort(service.capabilities)
    writeString(service.name, buffer)

    val length = buffer.writerIndex - dataStartIndex
    buffer.markWriterIndex()
    buffer.writerIndex(lengthStartIndex)
    buffer.writeShort(length)
    buffer.resetWriterIndex()
  }

  private def decodeServiceInformation(buffer: ByteBuf): ServiceInformation = {
    val itemId        = buffer.readUnsignedShort()
    val itemLength    = buffer.readUnsignedShort()
    val version       = buffer.readUnsignedShort()
    val capabilities  = buffer.readUnsignedShort()
    val name          = readString(buffer, itemLength - 4)

    ServiceInformation(itemId, version, capabilities, name)
  }

  private def readString(buffer: ByteBuf, length: Int): String = {
    val bs = buffer.readBytes(length - 1).array()

    val terminator = buffer.readByte()
    assert(terminator == 0x00)

    new String(bs)
  }

  private def writeString(s: String, buffer: ByteBuf) {
    val truncated = s.substring(0, Math.min(s.length, 15))

    buffer.writeBytes(truncated.getBytes)
    buffer.writeZero(1)
  }

}
