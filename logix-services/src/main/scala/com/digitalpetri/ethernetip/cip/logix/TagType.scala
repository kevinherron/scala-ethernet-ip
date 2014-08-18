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

package com.digitalpetri.ethernetip.cip.logix

import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

sealed abstract class TagType(val value: Int)

case class CipBool(bitIndex: Int) extends TagType(0x00C1)

case object CipSInt     extends TagType(0x00C2)
case object CipInt      extends TagType(0x00C3)
case object CipDInt     extends TagType(0x00C4)
case object CipLInt     extends TagType(0x00C5)

case object CipUSInt    extends TagType(0x00C6)
case object CipUInt     extends TagType(0x00C7)
case object CipUDInt    extends TagType(0x00C8)
case object CipULInt    extends TagType(0x00C9)

case object CipReal     extends TagType(0x00CA)
case object CipLReal    extends TagType(0x00CB)
case object CipDWord    extends TagType(0x00D3)

case object LogixProgram extends TagType(0x0068)

case class CipStructure(handle: Int) extends TagType(handle) {
  override def toString: String = f"$productPrefix(handle=$handle)"
}

case class CipUnknownType(override val value: Int) extends TagType(value)

object TagType {

  def encode(tagType: TagType, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    tagType match {
      case bool: CipBool =>
        buffer.writeShort(bool.bitIndex << 8 | bool.value)

      case t: TagType =>
        buffer.writeShort(t.value)
    }
  }

  /**
   * @return a [[TagType]] from the next short in the buffer. [[CipStructure]] and [[CipUnknownType]] cannot be
   *         distinguished; [[CipUnknownType]] will be returned instead.
   */
  def decode(buffer: ByteBuf): TagType = {
    fromValue(buffer.readUnsignedShort())
  }

  /**
   * @return the [[TagType]] for `value`. [[CipStructure]] and [[CipUnknownType]] cannot be distinguished;
   *         [[CipUnknownType]] will be returned instead.
   */
  def fromValue(value: Int): TagType = {
    value match {
      case 0x00C1 => CipBool(0)
      case 0x01C1 => CipBool(1)
      case 0x02C1 => CipBool(2)
      case 0x03C1 => CipBool(3)
      case 0x04C1 => CipBool(4)
      case 0x05C1 => CipBool(5)
      case 0x06C1 => CipBool(6)
      case 0x07C1 => CipBool(7)
      case CipSInt.value => CipSInt
      case CipInt.value => CipInt
      case CipDInt.value => CipDInt
      case CipLInt.value => CipLInt
      case CipReal.value => CipReal
      case CipDWord.value => CipDWord
      case LogixProgram.value => LogixProgram

      case _ => CipUnknownType(value)
    }
  }

}


