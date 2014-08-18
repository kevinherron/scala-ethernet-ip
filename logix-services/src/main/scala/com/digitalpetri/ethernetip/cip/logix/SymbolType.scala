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

sealed abstract class SymbolType {
  val dimensionCount: Int
  val reserved: Boolean

  def array: Boolean = !scalar
  def scalar: Boolean = dimensionCount == 0

  def structured: Boolean = this match {
    case a: AtomicSymbolType => false
    case s: StructuredSymbolType => true
  }
}

case class AtomicSymbolType(tagType: TagType, dimensionCount: Int = 0, reserved: Boolean = false) extends SymbolType {

  override def toString: String = s"$productPrefix(type=$tagType, dimensionCount=$dimensionCount, reserved=$reserved)"
}

case class StructuredSymbolType(templateInstanceId: Int, dimensionCount: Int = 0, reserved: Boolean = false) extends SymbolType {

  override def toString: String = f"$productPrefix(templateInstanceId=0x$templateInstanceId%04X, " +
                                  s"dimensionCount=$dimensionCount, reserved=$reserved)"
}


object SymbolType {

  def encode(symbolType: SymbolType, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    var value = symbolType match {
      case atomic: AtomicSymbolType =>
        TagType.encode(atomic.tagType).readUnsignedShort()

      case structured: StructuredSymbolType =>
        structured.templateInstanceId & 0xFFF
    }

    if (symbolType.structured) value |= (1 << 15)
    if (symbolType.array) value |= (symbolType.dimensionCount << 13)
    if (symbolType.reserved) value |= (1 << 12)

    buffer.writeShort(value)
  }

  def decode(buffer: ByteBuf): SymbolType = {
    val value = buffer.readUnsignedShort()

    val structured = ((value >>> 15) & 1) == 1
    val dimensionCount = (value >>> 13) & 3
    val reserved = ((value >>> 12) & 1) == 1
    val typeField = value & 0xFFF

    if (structured) {
      StructuredSymbolType(typeField, dimensionCount, reserved)
    } else {
      val tagType = TagType.fromValue(typeField)

      AtomicSymbolType(tagType, dimensionCount, reserved)
    }
  }

}
