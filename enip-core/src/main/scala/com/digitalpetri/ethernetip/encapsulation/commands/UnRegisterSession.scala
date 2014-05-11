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
 * Either an originator or a target may send this command to terminate the session. The receiver shall initiate a close
 * of the underlying TCP/IP connection when it receives this command. The session shall also be terminated when the
 * transport connection between the originator and target is terminated. The receiver shall perform any other associated
 * cleanup required on its end. There shall be no reply to this command, except in the event that the command is
 * received via UDP. If the command is received via UDP, the receiver shall reply with encapsulation error code 0x01
 * (invalid or unsupported command).
 *
 * The receiver shall not reject the UnRegisterSession due to unexpected values in the encapsulation header (invalid
 * Session Handle, non-zero Status, non-zero Options, or additional command data). In all cases the TCP connection shall
 * be closed.
 */
case class UnRegisterSession() extends Command(UnRegisterSessionCode)

object UnRegisterSession {
  def encode(t: UnRegisterSession, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = buffer

  def decode(buffer: ByteBuf): UnRegisterSession = UnRegisterSession()
}
