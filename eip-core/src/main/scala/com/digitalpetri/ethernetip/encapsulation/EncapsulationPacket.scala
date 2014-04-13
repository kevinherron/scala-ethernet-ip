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

package com.digitalpetri.ethernetip.encapsulation

import com.digitalpetri.ethernetip.encapsulation.commands._
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf
import scala.Some
import scala.util.Try


/**
 * All encapsulation messages, sent via TCP or sent to UDP port 0xAF12, shall be composed of a fixed-length header of
 * 24 bytes followed by an optional data portion. The total encapsulation message length (including header) shall be
 * limited to 65535 bytes.
 *
 * @param commandCode Encapsulation command.
 *
 * @param sessionHandle The Session Handle shall be generated by the target and returned to the originator in response
 *                      to a RegisterSession request. The originator shall insert it in all subsequent encapsulation
 *                      commands which require sessions to that particular target. In the case where the target
 *                      initiates and sends a command to the originator, the target shall include this field in the
 *                      request that it sends to the originator.
 *
 * @param status The value in the Status field shall indicate whether or not the receiver was able to execute the
 *               requested encapsulation command.
 *
 * @param senderContext The sender of the command shall assign the value in the Sender Context field of the header. The
 *                      receiver shall return this value without modification in its reply. Commands with no expected
 *                      reply may ignore this field.
 *
 * @param data The encapsulation data portion of the message is required only for certain commands.
 */
case class EncapsulationPacket(commandCode: Int,
                               sessionHandle: Long,
                               status: EncapsulationStatus = EipSuccess,
                               senderContext: Long,
                               data: Option[Command])


object EncapsulationPacket {

  def encode(packet: EncapsulationPacket, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(packet.commandCode)

    // Length placeholder...
    val lengthStartIndex = buffer.writerIndex()
    buffer.writeShort(0)

    buffer.writeInt(packet.sessionHandle.asInstanceOf[Int])
    buffer.writeInt(packet.status.status)
    buffer.writeLong(packet.senderContext)
    buffer.writeInt(0)

    val dataStartIndex = buffer.writerIndex()

    packet.data match {
      case Some(cmd) => encodeCommand(cmd, buffer)
      case _ => // Nothing to encode.
    }

    // Go back and update the length.
    val bytesWritten = buffer.writerIndex() - dataStartIndex
    buffer.markWriterIndex()
    buffer.writerIndex(lengthStartIndex)
    buffer.writeShort(bytesWritten)
    buffer.resetWriterIndex()
  }

  def decode(buffer: ByteBuf): Try[EncapsulationPacket] = Try {
    val commandCode   = buffer.readUnsignedShort()
    val length        = buffer.readUnsignedShort()
    val sessionHandle = buffer.readUnsignedInt()
    val statusValue   = buffer.readUnsignedInt()
    val senderContext = buffer.readLong()
    val options       = buffer.readUnsignedInt()

    EncapsulationStatus(statusValue) match {
      case EipSuccess =>
        val command = decodeCommand(commandCode, buffer)
        EncapsulationPacket(commandCode, sessionHandle, EipSuccess, senderContext, Some(command))

      case status: EncapsulationStatus =>
        EncapsulationPacket(commandCode, sessionHandle, status, senderContext, None)
    }
  }

  private def encodeCommand(command: Command, buffer: ByteBuf) {
    command match {
      case cmd: ListIdentity      => ListIdentity.encode(cmd, buffer)
      case cmd: ListInterfaces    => ListInterfaces.encode(cmd, buffer)
      case cmd: ListServices      => ListServices.encode(cmd, buffer)
      case cmd: Nop               => Nop.encode(cmd, buffer)
      case cmd: RegisterSession   => RegisterSession.encode(cmd, buffer)
      case cmd: SendRRData        => SendRRData.encode(cmd, buffer)
      case cmd: SendUnitData      => SendUnitData.encode(cmd, buffer)
      case cmd: UnRegisterSession => UnRegisterSession.encode(cmd, buffer)
    }
  }

  private def decodeCommand(code: Int, buffer: ByteBuf): Command = {
    CommandCode(code) match {
      case ListIdentityCode       => ListIdentity.decode(buffer)
      case ListInterfacesCode     => ListInterfaces.decode(buffer)
      case ListServicesCode       => ListServices.decode(buffer)
      case NopCode                => Nop.decode(buffer)
      case RegisterSessionCode    => RegisterSession.decode(buffer)
      case SendRRDataCode         => SendRRData.decode(buffer)
      case SendUnitDataCode       => SendUnitData.decode(buffer)
      case UnRegisterSessionCode  => UnRegisterSession.decode(buffer)

      case UnsupportedCode(c)     => throw new Exception(f"unsupported command: 0x$c%02X")
    }
  }

}
