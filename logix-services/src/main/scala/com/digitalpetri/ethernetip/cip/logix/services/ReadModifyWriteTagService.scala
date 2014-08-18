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

package com.digitalpetri.ethernetip.cip.logix.services

import com.digitalpetri.ethernetip.cip.epath.PaddedEPath
import com.digitalpetri.ethernetip.cip.logix._
import com.digitalpetri.ethernetip.cip.logix.services.ReadModifyWriteTagService.ReadModifyWriteTagRequest
import com.digitalpetri.ethernetip.cip.logix.services.ReadModifyWriteTagService.ReadModifyWriteTagRequest._
import com.digitalpetri.ethernetip.cip.structs.MessageRouterRequest
import com.digitalpetri.ethernetip.client.cip.services.AbstractInvokableService
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf

import scala.util.Try

class ReadModifyWriteTagService(request: ReadModifyWriteTagRequest, requestPath: PaddedEPath)
  extends AbstractInvokableService[Unit] {

  override def getRequestData: ByteBuf = {
    val routerRequest = MessageRouterRequest(
      serviceCode = LogixServiceCodes.ReadModifyWriteTag,
      requestPath = requestPath,
      requestData = ReadModifyWriteTagRequest.encode(request))

    MessageRouterRequest.encode(routerRequest)
  }

  /**
    * Decode `responseData` and return a response.
   * @param responseData the [[ByteBuf]] containing the response data.
   * @return a decoded response.
   */
  override def decodeResponse(responseData: ByteBuf): Try[Unit] = Try(Unit)

}

object ReadModifyWriteTagService {

  case class ReadModifyWriteTagRequest(sizeOfMasks: MaskSize, orMask: Long, andMask: Long)

  object ReadModifyWriteTagRequest {

    sealed abstract class MaskSize
    case object OneByteMask extends MaskSize
    case object TwoByteMask extends MaskSize
    case object FourByteMask extends MaskSize
    case object EightByteMask extends MaskSize

    def encode(request: ReadModifyWriteTagRequest, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
      request.sizeOfMasks match {
        case OneByteMask =>
          buffer.writeShort(1)
          buffer.writeByte(request.orMask.toByte)
          buffer.writeByte(request.andMask.toByte)

        case TwoByteMask =>
          buffer.writeShort(2)
          buffer.writeShort(request.orMask.toShort)
          buffer.writeShort(request.andMask.toShort)

        case FourByteMask =>
          buffer.writeShort(4)
          buffer.writeInt(request.orMask.toInt)
          buffer.writeInt(request.andMask.toInt)

        case EightByteMask =>
          buffer.writeShort(8)
          buffer.writeLong(request.orMask)
          buffer.writeLong(request.andMask)
      }
    }

    def decode(buffer: ByteBuf): ReadModifyWriteTagRequest = {
      val maskSize = buffer.readShort()

      if (maskSize == 1) {
        ReadModifyWriteTagRequest(
          sizeOfMasks = OneByteMask,
          orMask      = buffer.readUnsignedByte(),
          andMask     = buffer.readUnsignedByte())
      } else if (maskSize == 2) {
        ReadModifyWriteTagRequest(
          sizeOfMasks = TwoByteMask,
          orMask      = buffer.readUnsignedShort(),
          andMask     = buffer.readUnsignedShort())
      } else if (maskSize == 4) {
        ReadModifyWriteTagRequest(
          sizeOfMasks = FourByteMask,
          orMask      = buffer.readUnsignedInt(),
          andMask     = buffer.readUnsignedInt())
      } else {
        ReadModifyWriteTagRequest(
          sizeOfMasks = EightByteMask,
          orMask      = buffer.readLong() & 0xFFFFFFFFFFFFFFFFL,
          andMask     = buffer.readLong() & 0xFFFFFFFFFFFFFFFFL)
      }
    }

  }

  def apply(requestPath: PaddedEPath,
            tagType: TagType,
            bitIndex: Int,
            bitValue: Boolean): ReadModifyWriteTagService = {

    val orMask  = 0x0000000000000000L | ((if (bitValue) 1L else 0L) << bitIndex)
    val andMask = 0xFFFFFFFFFFFFFFFFL ^ ((if (bitValue) 0L else 1L) << bitIndex)

    val sizeOfMasks = tagType match {
      case CipSInt  => OneByteMask
      case CipInt   => TwoByteMask
      case CipDInt  => FourByteMask
      case CipReal  => FourByteMask
      case CipDWord => FourByteMask
      case CipLInt  => EightByteMask
      case _        => throw new Exception(s"Cannot apply ReadModifyWrite service to TagType $tagType.")
    }

    val request = ReadModifyWriteTagRequest(sizeOfMasks, orMask, andMask)

    new ReadModifyWriteTagService(request, requestPath)
  }

}
