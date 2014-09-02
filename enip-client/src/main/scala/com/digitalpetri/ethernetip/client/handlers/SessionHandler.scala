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

import java.util

import com.digitalpetri.ethernetip.encapsulation.commands.{RegisterSession, RegisterSessionCode}
import com.digitalpetri.ethernetip.encapsulation.{EipSuccess, EncapsulationPacket}
import com.typesafe.scalalogging.StrictLogging
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec

import scala.collection.mutable.ListBuffer

class SessionHandler() extends MessageToMessageCodec[EncapsulationPacket, EncapsulationPacket] with StrictLogging {

  private val packetQueue = new ListBuffer[EncapsulationPacket]()

  private var sessionHandle = 0L
  private var sessionRegistered = false

  override def encode(ctx: ChannelHandlerContext, packet: EncapsulationPacket, out: util.List[AnyRef]): Unit = {
    if (sessionRegistered) {
      out.add(packet.copy(sessionHandle = sessionHandle))
    } else {
      packetQueue += packet
    }
  }

  override def decode(ctx: ChannelHandlerContext, packet: EncapsulationPacket, out: util.List[AnyRef]): Unit = {
    if (packet.commandCode == RegisterSessionCode.value) {
      if (packet.status == EipSuccess) {
        sessionHandle = packet.sessionHandle
        sessionRegistered = true
        drainPacketQueue(ctx)
      } else {
        logger.error(s"RegisterSession failed: ${packet.status}")
        ctx.close()
      }
    } else {
      out.add(packet)
    }
  }

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    val command = RegisterSession()

    val packet = EncapsulationPacket(
      commandCode   = command.code.value,
      sessionHandle = 0L,
      senderContext = 0L,
      data          = Some(command))

    ctx.writeAndFlush(packet)

    super.channelActive(ctx)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    sessionHandle = 0
    sessionRegistered = false

    super.channelInactive(ctx)
  }

  private def drainPacketQueue(ctx: ChannelHandlerContext) {
    packetQueue.foreach(packet => ctx.channel.write(packet.copy(sessionHandle = sessionHandle)))
    packetQueue.clear()
    ctx.flush()
  }

}

