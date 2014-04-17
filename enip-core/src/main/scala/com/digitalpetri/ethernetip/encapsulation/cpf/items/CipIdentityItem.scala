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

package com.digitalpetri.ethernetip.encapsulation.cpf.items

import com.digitalpetri.ethernetip.encapsulation.cpf.CpfItem
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

/**
 * @param protocolVersion Encapsulation Protocol Version supported (also returned with Register Session reply).
 * @param socketAddress Sockaddr structure.
 * @param vendorId Device manufacturers Vendor ID.
 * @param deviceType Device Type of product.
 * @param productCode Product Code assigned with respect to device type.
 * @param revisionMajor Device major revision.
 * @param revisionMinor Device minor revision.
 * @param status Current status of device.
 * @param serialNumber Serial number of device.
 * @param productName Human readable description of device.
 * @param state Current state of device.
 */
case class CipIdentityItem(protocolVersion: Int,
                           socketAddress: Sockaddr,
                           vendorId: Int,
                           deviceType: Int,
                           productCode: Int,
                           revisionMajor: Short,
                           revisionMinor: Short,
                           status: Short,
                           serialNumber: Long,
                           productName: String,
                           state: Short) extends CpfItem(CipIdentityItem.TypeId)

object CipIdentityItem {

  val TypeId = 0x0C

  def encode(item: CipIdentityItem, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(TypeId)

    // Length placeholder...
    val lengthStartIndex = buffer.writerIndex
    buffer.writeShort(0)

    // Encode the item...
    val itemStartIndex = buffer.writerIndex
    buffer.writeShort(item.protocolVersion)
    Sockaddr.encode(item.socketAddress, buffer)
    buffer.writeShort(item.vendorId)
    buffer.writeShort(item.deviceType)
    buffer.writeShort(item.productCode)
    buffer.writeByte(item.revisionMajor.toByte)
    buffer.writeByte(item.revisionMinor.toByte)
    buffer.writeShort(item.status)
    buffer.writeInt(item.serialNumber.toInt)
    writeString(item.productName, buffer)
    buffer.writeByte(item.state.toByte)

    // Go back and update the length.
    val bytesWritten = buffer.writerIndex - itemStartIndex
    buffer.markWriterIndex()
    buffer.writerIndex(lengthStartIndex)
    buffer.writeShort(bytesWritten)
    buffer.resetWriterIndex()

    buffer
  }

  def decode(buffer: ByteBuf): CipIdentityItem = {
    val typeId = buffer.readUnsignedShort()
    val length = buffer.readUnsignedShort()

    assert(typeId == TypeId)

    CipIdentityItem(
      protocolVersion = buffer.readUnsignedShort(),
      socketAddress   = Sockaddr.decode(buffer),
      vendorId        = buffer.readUnsignedShort(),
      deviceType      = buffer.readUnsignedShort(),
      productCode     = buffer.readUnsignedShort(),
      revisionMajor   = buffer.readUnsignedByte(),
      revisionMinor   = buffer.readUnsignedByte(),
      status          = buffer.readShort(),
      serialNumber    = buffer.readUnsignedInt(),
      productName     = readString(buffer),
      state           = buffer.readUnsignedByte())
  }

  private def readString(buffer: ByteBuf): String = {
    val length = buffer.readUnsignedByte()
    val bs = buffer.readBytes(length).array()

    new String(bs)
  }

  private def writeString(s: String, buffer: ByteBuf) {
    buffer.writeByte(s.length.toByte)
    buffer.writeBytes(s.getBytes)
  }

}

