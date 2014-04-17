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

package com.digitalpetri.ethernetip.cip.epath

import com.digitalpetri.ethernetip.cip.epath.LogicalSegment.ElectronicKey
import com.digitalpetri.ethernetip.cip.epath.LogicalSegment.LogicalFormat
import com.digitalpetri.ethernetip.cip.epath.LogicalSegment.LogicalFormat._
import com.digitalpetri.ethernetip.cip.epath.LogicalSegment.LogicalType
import com.digitalpetri.ethernetip.cip.epath.LogicalSegment.LogicalType.LogicalType
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

sealed abstract class LogicalSegment[T](val logicalType: LogicalType) extends EPathSegment {
  def format: LogicalFormat
  def value: T
}

case class ClassId(value: Int, format: LogicalFormat = LogicalFormat.Bits_16)
  extends LogicalSegment[Int](LogicalType.ClassId)

case class InstanceId(value: Int, format: LogicalFormat = LogicalFormat.Bits_16)
  extends LogicalSegment[Int](LogicalType.InstanceId)

case class MemberId(value: Int, format: LogicalFormat = LogicalFormat.Bits_16)
  extends LogicalSegment[Int](LogicalType.MemberId)

case class ConnectionPoint(value: Int, format: LogicalFormat = LogicalFormat.Bits_16)
  extends LogicalSegment[Int](LogicalType.ConnectionPoint)

case class AttributeId(value: Int, format: LogicalFormat = LogicalFormat.Bits_16)
  extends LogicalSegment[Int](LogicalType.AttributeId)

case class ServiceId(value: Int)
  extends LogicalSegment[Int](LogicalType.ServiceId) {
  override def format: LogicalFormat = LogicalFormat.Bits_8
}

case class KeySegment(value: ElectronicKey)
  extends LogicalSegment[ElectronicKey](LogicalType.Special) {
  override def format: LogicalFormat = LogicalFormat.Bits_8
}

object LogicalSegment {

  case class ElectronicKey(vendorId: Int,
                           deviceType: Int,
                           productCode: Int,
                           majorRevision: Byte,
                           strictMajor: Boolean,
                           minorRevision: Byte)

  object LogicalType {
    sealed abstract class LogicalType(val typeId: Int)

    case object ClassId         extends LogicalType(0x0)
    case object InstanceId      extends LogicalType(0x1)
    case object MemberId        extends LogicalType(0x2)
    case object ConnectionPoint extends LogicalType(0x3)
    case object AttributeId     extends LogicalType(0x4)
    case object Special         extends LogicalType(0x5)
    case object ServiceId       extends LogicalType(0x6)
    case object Reserved        extends LogicalType(0x7)
  }

  object LogicalFormat {
    sealed abstract class LogicalFormat(val formatId: Int)

    case object Bits_8    extends LogicalFormat(0x0)
    case object Bits_16   extends LogicalFormat(0x1)
    case object Bits_32   extends LogicalFormat(0x2)
    case object Reserved  extends LogicalFormat(0x3)
  }

  val SegmentType = 0x01

  def encode(segment: LogicalSegment[_], padded: Boolean, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    var segmentByte = 0

    segmentByte |= (SegmentType << 5)
    segmentByte |= (segment.logicalType.typeId << 2)
    segmentByte |= segment.format.formatId

    buffer.writeByte(segmentByte)
    if (padded) buffer.writeByte(0x00)

    segment match {
      case s: ClassId         => encodeIntSegment(s, buffer)
      case s: InstanceId      => encodeIntSegment(s, buffer)
      case s: MemberId        => encodeIntSegment(s, buffer)
      case s: ConnectionPoint => encodeIntSegment(s, buffer)
      case s: AttributeId     => encodeIntSegment(s, buffer)
      case s: ServiceId       => encodeIntSegment(s, buffer)
      case s: KeySegment      => encodeKeySegment(s, buffer)
    }

    buffer
  }

  private def encodeIntSegment(segment: LogicalSegment[Int], buffer: ByteBuf) {
    segment.format match {
      case Bits_8   => buffer.writeByte(segment.value)
      case Bits_16  => buffer.writeShort(segment.value)
      case Bits_32  => buffer.writeInt(segment.value)
      case Reserved => throw new Exception("reserved LogicalSegment format not supported")
    }
  }

  private def encodeKeySegment(segment: KeySegment, buffer: ByteBuf) {

  }
  
}
