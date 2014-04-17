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
