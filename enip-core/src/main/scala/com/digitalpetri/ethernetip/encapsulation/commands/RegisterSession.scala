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

