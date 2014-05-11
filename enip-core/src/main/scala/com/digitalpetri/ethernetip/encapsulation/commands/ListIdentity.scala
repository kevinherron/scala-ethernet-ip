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

import com.digitalpetri.ethernetip.encapsulation.cpf.CpfPacket
import com.digitalpetri.ethernetip.encapsulation.cpf.items.CipIdentityItem
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
 *               [[com.digitalpetri.ethernetip.encapsulation.cpf.items.CipIdentityItem]].
 */
case class ListIdentity(packet: Option[CpfPacket] = None) extends Command(ListIdentityCode) {
  def identityItem(): Option[CipIdentityItem] = {
    packet.map(p => p.items match {
      case item :: Nil => item.asInstanceOf[CipIdentityItem]
    })
  }
}

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



