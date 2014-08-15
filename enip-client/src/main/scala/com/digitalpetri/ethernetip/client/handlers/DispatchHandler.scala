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

package com.digitalpetri.ethernetip.client.handlers

import com.digitalpetri.ethernetip.encapsulation.EncapsulationPacket
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}

import scala.concurrent.ExecutionContext

class DispatchHandler(packetReceiver: PacketReceiver, executionContext: ExecutionContext)
  extends SimpleChannelInboundHandler[EncapsulationPacket] {

  override def channelRead0(ctx: ChannelHandlerContext, packet: EncapsulationPacket): Unit = {
    executionContext.execute(new Runnable {
      override def run(): Unit = packetReceiver.onPacketReceived(packet)
    })
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

