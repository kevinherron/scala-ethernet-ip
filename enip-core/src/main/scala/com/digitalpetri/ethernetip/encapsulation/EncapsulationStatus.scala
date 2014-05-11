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


