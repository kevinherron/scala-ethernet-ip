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

}

object PacketLayer {
  val HeaderSize = 24
  val LengthOffset = 2
}
