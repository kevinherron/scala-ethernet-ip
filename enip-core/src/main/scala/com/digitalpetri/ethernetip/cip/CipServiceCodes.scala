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

package com.digitalpetri.ethernetip.cip

object CipServiceCodes {

  val GetAttributesAll = 0x01
  val SetAttributesAll = 0x02
  val GetAttributeList = 0x03
  val SetAttributeList = 0x04
  val Reset = 0x05
  val Start = 0x06
  val Stop = 0x07
  val Create = 0x08
  val Delete = 0x09
  val MultipleServicePacket = 0x0A
  val ApplyAttributes = 0x0D
  val GetAttributeSingle = 0x0E
  val SetAttributeSingle = 0x10
  val FindNextObjectInstance = 0x011
  val Restore = 0x15
  val Save = 0x16
  val Nop = 0x17
  val GetMember = 0x18
  val SetMember = 0x19
  val InsertMember = 0x1A
  val RemoveMember = 0x1B
  val GroupSync = 0x1C

}
