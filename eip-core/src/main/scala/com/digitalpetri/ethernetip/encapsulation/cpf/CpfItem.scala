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
      case ConnectedAddressItem.TypeId  => ConnectedAddressItem.decode(buffer)
      case ConnectedDataItem.TypeId     => ConnectedDataItem.decode(buffer)
      case NullAddressItem.TypeId       => NullAddressItem.decode(buffer)
      case SequencedAddressItem.TypeId  => SequencedAddressItem.decode(buffer)
      case SockaddrItem.TypeIdO2t       => SockaddrItem.decode(buffer)
      case SockaddrItem.TypeIdT2o       => SockaddrItem.decode(buffer)
    }
  }

}
