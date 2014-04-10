package com.digitalpetri.ethernetip.cip

import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

case class ConnectedPacket(sequenceNumber: Short)(val data: ByteBuf)

object ConnectedPacket {

  def encode(packet: ConnectedPacket, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(packet.sequenceNumber)
    buffer.writeBytes(packet.data)

    buffer
  }

  def decode(buffer: ByteBuf): ConnectedPacket = {
    ConnectedPacket(buffer.readShort())(buffer.slice())
  }

}
