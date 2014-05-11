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

package com.digitalpetri.ethernetip.encapsulation.cpf.items

import com.digitalpetri.ethernetip.encapsulation.cpf.CpfItem
import com.digitalpetri.ethernetip.util.Buffers
import io.netty.buffer.ByteBuf


/**
 * The Sockaddr Info items shall be used to communicate IP address or port information necessary to create Class 0 or
 * Class 1 connections. There are separate items for originator-to- target and target-to-originator socket information.
 *
 * The items are present as additional data in Forward_Open / Large_Forward_Open request and reply services encapsulated
 * in a SendRRData message.
 */
sealed trait SockaddrItem extends CpfItem {
  val sockaddr: Sockaddr
}

case class SockaddrItemO2t(sockaddr: Sockaddr) extends CpfItem(SockaddrItem.TypeIdO2t) with SockaddrItem
case class SockaddrItemT2o(sockaddr: Sockaddr) extends CpfItem(SockaddrItem.TypeIdT2o) with SockaddrItem

object SockaddrItem {

  val TypeIdO2t = 0x8000
  val TypeIdT2o = 0x8001
  val Length = 16

  def encode(item: SockaddrItem, buffer: ByteBuf = Buffers.unpooled()): ByteBuf = {
    buffer.writeShort(item.typeId)
    buffer.writeShort(Length)

    Sockaddr.encode(item.sockaddr, buffer)
  }

  def decode(buffer: ByteBuf): SockaddrItem = {
    val typeId    = buffer.readUnsignedShort()
    val length    = buffer.readUnsignedShort()

    assert(typeId == TypeIdO2t || typeId == TypeIdT2o)
    assert(length == Length)

    val sockaddr = Sockaddr.decode(buffer)

    typeId match {
      case TypeIdO2t => SockaddrItemO2t(sockaddr)
      case TypeIdT2o => SockaddrItemT2o(sockaddr)
      case _ => throw new Exception(s"invalid SockAddrItem type: $typeId")
    }
  }

}
