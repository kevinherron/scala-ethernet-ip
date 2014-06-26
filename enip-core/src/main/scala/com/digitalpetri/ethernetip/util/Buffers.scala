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

package com.digitalpetri.ethernetip.util

import java.nio.ByteOrder

import io.netty.buffer.{ByteBuf, CompositeByteBuf, PooledByteBufAllocator, Unpooled}

object Buffers {

  val PooledAllocator = PooledByteBufAllocator.DEFAULT

  val EmptyBuffer = Unpooled.EMPTY_BUFFER

  def unpooled(): ByteBuf = Unpooled.buffer().order(ByteOrder.LITTLE_ENDIAN)
  def unpooled(initialCapacity: Int): ByteBuf = Unpooled.buffer(initialCapacity).order(ByteOrder.LITTLE_ENDIAN)

  def pooled(): ByteBuf = PooledAllocator.buffer().order(ByteOrder.LITTLE_ENDIAN)
  def pooled(initialCapacity: Int): ByteBuf = PooledAllocator.buffer(initialCapacity).order(ByteOrder.LITTLE_ENDIAN)

  def composite(): CompositeByteBuf = Unpooled.compositeBuffer()

}
