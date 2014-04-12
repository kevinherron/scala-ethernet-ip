package com.digitalpetri.ethernetip.cip.epath

import io.netty.buffer.ByteBuf

sealed abstract class EPath {
  def segments: Seq[EPathSegment]

  override def toString: String = {
    s"${getClass.getSimpleName}(${segments.mkString(",")})"
  }
}

case class PackedEPath(segments: EPathSegment*) extends EPath
case class PaddedEPath(segments: EPathSegment*) extends EPath


object PackedEPath {
  def encode(path: PackedEPath, buffer: ByteBuf) {
    // TODO
  }

  def decode(buffer: ByteBuf): PackedEPath = {
    ??? // TODO
  }
}

object PaddedEPath {
  def encode(path: PaddedEPath, buffer: ByteBuf) {
    // TODO
  }

  def decode(buffer: ByteBuf): PaddedEPath = {
    ??? // TODO
  }
}
