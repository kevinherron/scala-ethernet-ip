package com.digitalpetri.ethernetip.cip.logix.tags

import com.digitalpetri.ethernetip.cip.logix._
import io.netty.buffer.ByteBuf

import scala.util.Try

object TagReader {

  /**
   * Read all values for `tag` from the response of a [[com.digitalpetri.ethernetip.cip.logix.services.ReadTagService]]
   * or [[com.digitalpetri.ethernetip.cip.logix.services.ReadTagFragmentedService]].
   *
   * @param tag the tag read.
   * @param tagType the [[TagType]] from the response.
   * @param tagData the tag data from the response.
   * @return tag/value pairs for `tag` and all children of `tag`, recursively.
   */
  private[tags] def readTag(tag: LogixTag, tagType: TagType, tagData: ByteBuf): Try[Seq[(LogixTag, Any)]] = Try {
    tag match {
      case atomicTag: AtomicTag =>
        val thisValue = atomicTag.tagType.value & 0xFFF
        val thatValue = tagType.value & 0xFFF

        if (thisValue == thatValue) {
          readTag(tag, tagData)
        } else {
          throw new Exception(s"atomic TagType mismatch ($thisValue != $thatValue)")
        }

      case structuredTag: StructuredTag =>
        val thisValue = structuredTag.template.attributes.handle
        val thatValue = tagType.value

        if (thisValue == thatValue) {
          readTag(tag, tagData)
        } else {
          throw new Exception(s"structure handle mismatch ($thisValue != $thatValue)")
        }
    }
  }

  private def readTag(tag: LogixTag, buffer: ByteBuf): Seq[(LogixTag, Any)] = {
    tag match {
      case atomicTag: AtomicTag =>
        if (atomicTag.dimensions.length > 0) {
          atomicTag.children.map(readTag(_, buffer)).flatten
        } else {
          readAtomicTag(atomicTag, buffer)
        }

      case structuredTag: StructuredTag =>
        if (structuredTag.dimensions.length > 0) {
          structuredTag.children.map(readTag(_, buffer)).flatten
        } else {
          readStructuredTag(structuredTag, buffer)
        }
    }
  }

  private def readAtomicTag(atomicTag: AtomicTag, buffer: ByteBuf): Seq[(LogixTag, Any)] = {
    val value = atomicTag.tagType match {
      case t: CipBool =>
        val hostByte = buffer.readByte()
        ((hostByte >> t.bitIndex) & 1) == 1

      /* Signed types */
      case CipSInt => buffer.readByte()
      case CipInt => buffer.readShort()
      case CipDInt => buffer.readInt()
      case CipDWord => buffer.readInt()
      case CipLInt => buffer.readLong()

      /* Floating point */
      case CipReal => buffer.readFloat()
      case CipLReal => buffer.readDouble()

      case tagType =>
        throw new Exception(s"cannot read ${atomicTag.name} atomically as $tagType")
    }

    Seq((atomicTag, value))
  }

  private def readStructuredTag(structuredTag: StructuredTag, buffer: ByteBuf): Seq[(LogixTag, Any)] = {
    val template = structuredTag.template
    val attributes = template.attributes

    val templateBuffer = buffer.readSlice(attributes.structureSize).markReaderIndex()

    val members = template.members.zip(structuredTag.children)

    val values = members.flatMap {
      case (member, memberTag) =>
        templateBuffer.readerIndex(member.offset)
        readTag(memberTag, templateBuffer)
    }

    if (isStringLikeTag(structuredTag)) {
      templateBuffer.resetReaderIndex()
      readStringLikeTag(structuredTag, templateBuffer) ++ values
    } else {
      values
    }
  }

  private def isStringLikeTag(structuredTag: StructuredTag): Boolean = {
    val template = structuredTag.template

    template.members.length == 2 &&
      template.members(0).name == "LEN" &&
      template.members(1).name == "DATA"
  }

  private def readStringLikeTag(stringTag: StructuredTag, buffer: ByteBuf): Seq[(LogixTag, Any)] = {
    val length  = Math.min(buffer.readUnsignedInt(), buffer.readableBytes())
    val data    = new Array[Byte](length.toInt)

    buffer.readBytes(data)

    Seq((stringTag, new String(data)))
  }

}
