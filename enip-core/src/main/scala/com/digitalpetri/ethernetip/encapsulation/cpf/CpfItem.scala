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

package com.digitalpetri.ethernetip.encapsulation.cpf

import com.digitalpetri.ethernetip.encapsulation.cpf.items._
import io.netty.buffer.ByteBuf

abstract class CpfItem(val typeId: Int)

object CpfItem {

  def encode(item: CpfItem, buffer: ByteBuf) {
    item match {
      case i: CipIdentityItem       => CipIdentityItem.encode(i, buffer)
      case i: ConnectedAddressItem  => ConnectedAddressItem.encode(i, buffer)
      case i: ConnectedDataItem     => ConnectedDataItem.encode(i, buffer)
      case i: UnconnectedDataItem   => UnconnectedDataItem.encode(i, buffer)
      case i: NullAddressItem       => NullAddressItem.encode(i, buffer)
      case i: SequencedAddressItem  => SequencedAddressItem.encode(i, buffer)
      case i: SockaddrItem          => SockaddrItem.encode(i, buffer)
    }
  }

  def decode(buffer: ByteBuf): CpfItem = {
    val typeId = buffer.getUnsignedShort(buffer.readerIndex())

    typeId match {
      case CipIdentityItem.TypeId       => CipIdentityItem.decode(buffer)
      case ConnectedDataItem.TypeId     => ConnectedDataItem.decode(buffer)
      case UnconnectedDataItem.TypeId   => UnconnectedDataItem.decode(buffer)
      case ConnectedAddressItem.TypeId  => ConnectedAddressItem.decode(buffer)
      case NullAddressItem.TypeId       => NullAddressItem.decode(buffer)
      case SequencedAddressItem.TypeId  => SequencedAddressItem.decode(buffer)
      case SockaddrItem.TypeIdO2t       => SockaddrItem.decode(buffer)
      case SockaddrItem.TypeIdT2o       => SockaddrItem.decode(buffer)

      case _ => throw new Exception(f"unhandled item type: 0x$typeId%02X")
    }
  }

}
