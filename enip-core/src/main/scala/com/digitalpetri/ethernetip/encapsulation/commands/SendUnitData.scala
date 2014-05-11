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
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

/**
 * The SendUnitData command shall send encapsulated connected messages. This command may be used when the encapsulated
 * protocol has its own underlying end-to-end transport mechanism. A reply shall not be returned. The SendUnitData
 * command may be sent by either end of the TCP connection.
 *
 * When used to encapsulate the CIP, the SendUnitData command is used to send CIP connected data in both the O->T and
 * T->O directions.
 *
 * @param interfaceHandle Shall be 0.
 * @param timeout Shall be 0.
 * @param packet The encapsulated connected message.
 */
case class SendUnitData(interfaceHandle: Long = 0, timeout: Int = 0, packet: CpfPacket)
  extends Command(SendUnitDataCode)

object SendUnitData {

  def encode(command: SendUnitData, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeInt(command.interfaceHandle.asInstanceOf[Int])
    buffer.writeShort(command.timeout)

    CpfPacket.encode(command.packet, buffer)

    buffer
  }

  def decode(buffer: ByteBuf): SendUnitData = {
    SendUnitData(
      interfaceHandle = buffer.readUnsignedInt(),
      timeout         = buffer.readUnsignedShort(),
      packet          = CpfPacket.decode(buffer)
    )
  }

}
