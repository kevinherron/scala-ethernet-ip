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

package com.digitalpetri.ethernetip.cip.structs

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
