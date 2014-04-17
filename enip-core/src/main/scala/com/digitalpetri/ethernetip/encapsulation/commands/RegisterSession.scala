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

package com.digitalpetri.ethernetip.encapsulation.commands

import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

/**
 * An originator shall send a RegisterSession command to a target to initiate a session. The RegisterSession command
 * does not require that a session be established.
 *
 * The target shall send a RegisterSession reply to indicate that it has registered the originator.
 *
 * @param protocolVersion Shall be 1.
 * @param optionFlags Shall be 0. No options are currently defined.
 */
case class RegisterSession(protocolVersion: Int = 1, optionFlags: Int = 0)
  extends Command(RegisterSessionCode)

object RegisterSession {
  def encode(command: RegisterSession, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(command.protocolVersion)
    buffer.writeShort(command.optionFlags)

    buffer
  }

  def decode(buffer: ByteBuf): RegisterSession = {
    RegisterSession(
      protocolVersion = buffer.readUnsignedShort(),
      optionFlags     = buffer.readUnsignedShort()
    )
  }
}

