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
 * A SendRRData command shall transfer an encapsulated request/reply packet between the originator and target, where the
 * originator initiates the command. The actual request/reply packets shall be encapsulated in the data portion of the
 * message and shall be the responsibility of the target and originator.
 *
 * The SendRRData reply shall contain data in response to the SendRRData request. The reply to the original encapsulated
 * protocol request shall be contained in the data portion of the SendRRData reply.
 *
 * @param interfaceHandle The Interface handle shall identify the Communications Interface to which the request is
 *                        directed. This handle shall be 0 for encapsulating CIP packets.
 *
 * @param timeout The target shall abort the requested operation after the timeout expires. When the “timeout” field is
 *                in the range 1 to 65535, the timeout shall be set to this number of seconds. When the “timeout” field
 *                is set to 0, the encapsulation protocol shall not have its own timeout. Instead, it shall rely on the
 *                timeout mechanism of the encapsulated protocol. When the SendRRData command is used to encapsulate CIP
 *                packets, the Timeout field shall be set to 0, and shall be ignored by the target.
 *
 * @param packet The encapsulated protocol packet shall be encoded in the Common Packet Format.
 */
case class SendRRData(interfaceHandle: Long = 0, timeout: Int = 0, packet: CpfPacket)
  extends Command(SendRRDataCode)

object SendRRData {

  def encode(command: SendRRData, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeInt(command.interfaceHandle.asInstanceOf[Int])
    buffer.writeShort(command.timeout)

    CpfPacket.encode(command.packet, buffer)

    buffer
  }

  def decode(buffer: ByteBuf): SendRRData = {
    SendRRData(
      interfaceHandle = buffer.readUnsignedInt(),
      timeout         = buffer.readUnsignedShort(),
      packet          = CpfPacket.decode(buffer)
    )
  }

}

