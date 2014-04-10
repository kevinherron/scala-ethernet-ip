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

package com.digitalpetri.ethernetip.encapsulation.layers

import com.digitalpetri.ethernetip.encapsulation.EncapsulationPacket
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}

class DispatchLayer(receiver: PacketReceiver) extends SimpleChannelInboundHandler[EncapsulationPacket] {

  def channelRead0(ctx: ChannelHandlerContext, packet: EncapsulationPacket): Unit = {
    receiver.onPacketReceived(packet)
  }

}

trait PacketReceiver {

  /**
   * An [[EncapsulationPacket]] has been decoded.
   *
   * At this point the current thread is still an event loop thread. Any un-decoded [[io.netty.buffer.ByteBuf]]s
   * contained in the packet must be decoded before leaving this thread.
   *
   * @param packet an [[EncapsulationPacket]].
   */
  def onPacketReceived(packet: EncapsulationPacket)

}
