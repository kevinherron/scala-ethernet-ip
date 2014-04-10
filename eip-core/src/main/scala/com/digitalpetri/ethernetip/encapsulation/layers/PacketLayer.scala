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
import com.digitalpetri.ethernetip.encapsulation.commands.{SendRRData, SendUnitData}
import com.digitalpetri.ethernetip.encapsulation.cpf.items.{UnconnectedDataItem, ConnectedDataItem}
import com.typesafe.scalalogging.slf4j.Logging
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageCodec
import java.nio.ByteOrder
import java.util
import scala.util.{Failure, Success}

class PacketLayer extends ByteToMessageCodec[EncapsulationPacket] with Logging {

  def encode(ctx: ChannelHandlerContext, msg: EncapsulationPacket, out: ByteBuf): Unit = {
    val buffer = out.order(ByteOrder.LITTLE_ENDIAN)

    EncapsulationPacket.encode(msg, buffer)
  }

  def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]): Unit = {
    var startIndex = in.readerIndex()

    while (in.readableBytes() >= PacketLayer.HeaderSize &&
           in.readableBytes() >= PacketLayer.HeaderSize + getLength(in, startIndex)) {

      val buffer = in.order(ByteOrder.LITTLE_ENDIAN)

      EncapsulationPacket.decode(buffer) match {
        case Success(packet) =>
          maybeRetainBuffer(ctx, packet)
          out.add(packet)

        case Failure(ex) =>
          logger.error("Error decoding packet.", ex)

          // Advance past any bytes we should have read but didn't...
          val endIndex = startIndex + PacketLayer.HeaderSize + getLength(in, startIndex)
          buffer.readerIndex(endIndex)
      }

      startIndex = buffer.readerIndex()
    }
  }

  private def getLength(in: ByteBuf, startIndex: Int): Int = {
    in.order(ByteOrder.LITTLE_ENDIAN).getUnsignedShort(startIndex + PacketLayer.LengthOffset)
  }

  private final def maybeRetainBuffer(ctx: ChannelHandlerContext, packet: EncapsulationPacket) {
    val items = packet.data match {
      case Some(cmd: SendRRData)    => Some(cmd.packet.items)
      case Some(cmd: SendUnitData)  => Some(cmd.packet.items)
      case _ => None
    }

    items match {
      case Some(is) =>
        for (item <- is) {
          item match {
            case di: ConnectedDataItem    => retainAndRelease(di.data)
            case di: UnconnectedDataItem  => retainAndRelease(di.data)
            case _ => // Other CpfItem types don't contain un-decoded buffers.
          }
        }
      case None => // No CpfItems, no chance for sliced buffers propagating to the upper layers.
    }

    def retainAndRelease(buffer: ByteBuf) {
      buffer.retain()
      ctx.executor().execute(new ReleaseBuffer(buffer))
    }
  }

  private final class ReleaseBuffer(buffer: ByteBuf) extends Runnable {
    def run() { buffer.release() }
  }

}

object PacketLayer {
  val HeaderSize = 24
  val LengthOffset = 2
}
