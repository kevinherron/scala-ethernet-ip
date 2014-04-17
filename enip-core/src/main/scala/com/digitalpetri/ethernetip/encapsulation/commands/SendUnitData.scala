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

import com.digitalpetri.ethernetip.encapsulation.cpf.CpfPacket
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

/**
 * The SendUnitData command shall send encapsulated connected messages. This command may be used when the encapsulated
 * protocol has its own underlying end-to-end transport mechanism. A reply shall not be returned. The SendUnitData
 * command may be sent by either end of the TCP connection.
 *
 * When used to encapsulate the CIP, the SendUnitData command is used to send CIP connected data in both the O->T and
 * T->O directions.
 *
 * @param interfaceHandle Shall be 0.
 * @param timeout Shall be 0.
 * @param packet The encapsulated connected message.
 */
case class SendUnitData(interfaceHandle: Long = 0, timeout: Int = 0, packet: CpfPacket)
  extends Command(SendUnitDataCode)

object SendUnitData {

  def encode(command: SendUnitData, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeInt(command.interfaceHandle.asInstanceOf[Int])
    buffer.writeShort(command.timeout)

    CpfPacket.encode(command.packet, buffer)

    buffer
  }

  def decode(buffer: ByteBuf): SendUnitData = {
    SendUnitData(
      interfaceHandle = buffer.readUnsignedInt(),
      timeout         = buffer.readUnsignedShort(),
      packet          = CpfPacket.decode(buffer)
    )
  }

}
