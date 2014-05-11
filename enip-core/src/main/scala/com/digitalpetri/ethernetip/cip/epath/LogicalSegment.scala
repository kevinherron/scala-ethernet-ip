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

package com.digitalpetri.ethernetip.cip.epath

import com.digitalpetri.ethernetip.cip.epath.LogicalSegment.ElectronicKey
import com.digitalpetri.ethernetip.cip.epath.LogicalSegment.LogicalFormat
import com.digitalpetri.ethernetip.cip.epath.LogicalSegment.LogicalFormat._
import com.digitalpetri.ethernetip.cip.epath.LogicalSegment.LogicalType
import com.digitalpetri.ethernetip.cip.epath.LogicalSegment.LogicalType.LogicalType
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

/**
 * The logical segment selects a particular object address within a device (for example, Object Class, Object Instance,
 * and Object Attribute).
 * @param logicalType See [[LogicalType]].
 */
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
                           strictMajor: Boolean,
                           majorRevision: Int,
                           minorRevision: Int)

  object ElectronicKey {

    val KeyFormat = 0x04

    def encode(key: ElectronicKey, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      buffer.writeShort(key.vendorId)
      buffer.writeShort(key.deviceType)
      buffer.writeShort(key.productCode)

      var majorRevision = if (key.strictMajor) 0x80 else 0x00
      majorRevision |= (key.majorRevision & 0x7F)
      buffer.writeByte(majorRevision)

      buffer.writeByte(key.minorRevision)
    }

    def decode(buffer: ByteBuf): ElectronicKey = {
      val vendorId = buffer.readUnsignedShort()
      val deviceType = buffer.readUnsignedShort()
      val productCode = buffer.readUnsignedShort()

      val majorByte = buffer.readUnsignedByte()
      val strictMajor = ((majorByte >> 7) & 1) == 1
      val majorRevision = majorByte & 0x7F
      val minorRevision = buffer.readUnsignedByte()

      ElectronicKey(vendorId, deviceType, productCode, strictMajor, majorRevision, minorRevision)
    }

  }

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
    buffer.writeByte(ElectronicKey.KeyFormat)
    ElectronicKey.encode(segment.value, buffer)
  }
  
}
