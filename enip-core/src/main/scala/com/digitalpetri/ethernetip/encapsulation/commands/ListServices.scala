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
 * The ListServices command shall determine which encapsulation service classes the target device supports. The
 * ListServices command does not require that a session be established.
 *
 * Each service class has a unique type code, and an optional ASCII name.
 */
case class ListServices(services: Seq[ServiceInformation] = Seq.empty) extends Command(ListServicesCode)

case class ServiceInformation(itemId: Int, version: Int, capabilities: Int, name: String)

object ListServices {

  def encode(command: ListServices, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    if (command.services.nonEmpty) {
      buffer.writeShort(command.services.size)
      command.services.foreach(encodeServiceInformation(_, buffer))
    }

    buffer
  }

  def decode(buffer: ByteBuf): ListServices = {
    val itemCount = {
      if (buffer.readableBytes() >= 2) buffer.readUnsignedShort()
      else 0
    }

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
