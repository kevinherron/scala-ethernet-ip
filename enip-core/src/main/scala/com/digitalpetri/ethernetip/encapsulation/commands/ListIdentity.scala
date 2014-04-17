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
 * A connection originator may use the ListIdentity command to locate and identify potential targets. This command
 * shall be sent as a unicast message using TCP or UDP, or as a broadcast message using UDP and does not require that a
 * session be established. The reply shall always be sent as a unicast message.
 *
 * One reply item is defined for this command, Target Identity, with item type code 0x0C. This item shall be supported
 * (returned) by all EtherNet/IP devices.
 *
 * @param packet When replying, a [[CpfPacket]] that should contain a
 *               [[com.digitalpetri.ethernetip.encapsulation.cpf.CipIdentityItem]].
 */
case class ListIdentity(packet: Option[CpfPacket] = None) extends Command(ListIdentityCode)

object ListIdentity {
  def encode(command: ListIdentity, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    if (command.packet.isDefined) {
      CpfPacket.encode(command.packet.get, buffer)
    }

    buffer
  }

  def decode(buffer: ByteBuf): ListIdentity = {
    if (buffer.readableBytes() > 0) {
      ListIdentity(Some(CpfPacket.decode(buffer)))
    } else {
      ListIdentity()
    }
  }
}



