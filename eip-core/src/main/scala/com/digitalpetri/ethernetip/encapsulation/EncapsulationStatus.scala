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

package com.digitalpetri.ethernetip.encapsulation


sealed abstract class EncapsulationStatus(val status: Int)

case object EipSuccess                  extends EncapsulationStatus(0x00)
case object InvalidUnsupported          extends EncapsulationStatus(0x01)
case object InsufficientMemory          extends EncapsulationStatus(0x02)
case object MalformedData               extends EncapsulationStatus(0x03)
case object InvalidSessionHandle        extends EncapsulationStatus(0x64)
case object InvalidLength               extends EncapsulationStatus(0x65)
case object UnsupportedProtocolVersion  extends EncapsulationStatus(0x69)

/** Catch-all status for invalid or unrecognized values. */
case class UnsupportedStatus(override val status: Int) extends EncapsulationStatus(status)

object EncapsulationStatus {

  def apply(value: Long): EncapsulationStatus = {
    apply(value.asInstanceOf[Int])
  }

  def apply(value: Int): EncapsulationStatus = {
    value match {
      case EipSuccess.status                  => EipSuccess
      case InvalidUnsupported.status          => InvalidUnsupported
      case InsufficientMemory.status          => InsufficientMemory
      case MalformedData.status               => MalformedData
      case InvalidSessionHandle.status        => InvalidSessionHandle
      case InvalidLength.status               => InvalidLength
      case UnsupportedProtocolVersion.status  => UnsupportedProtocolVersion

      case _ => UnsupportedStatus(value)
    }
  }

}


