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

package com.digitalpetri.ethernetip.encapsulation.commands

/**
 * Base class for an encapsulation layer command.
 * @param code The code assigned to the command.
 */
abstract class Command(val code: CommandCode)


/** A code indicating an encapsulation command. */
sealed abstract class CommandCode(val value: Int)

case object NopCode               extends CommandCode(0x00)
case object ListServicesCode      extends CommandCode(0x04)
case object ListIdentityCode      extends CommandCode(0x63)
case object ListInterfacesCode    extends CommandCode(0x64)
case object RegisterSessionCode   extends CommandCode(0x65)
case object UnRegisterSessionCode extends CommandCode(0x66)
case object SendRRDataCode        extends CommandCode(0x6F)
case object SendUnitDataCode      extends CommandCode(0x70)


/** Catch-all for unsupported or unrecognized command codes. */
case class UnsupportedCode(code: Int) extends CommandCode(code)

object CommandCode {

  def apply(code: Int): CommandCode = {
    code match {
      case ListIdentityCode.value       => ListIdentityCode
      case ListInterfacesCode.value     => ListInterfacesCode
      case ListServicesCode.value       => ListServicesCode
      case NopCode.value                => NopCode
      case RegisterSessionCode.value    => RegisterSessionCode
      case SendRRDataCode.value         => SendRRDataCode
      case SendUnitDataCode.value       => SendUnitDataCode
      case UnRegisterSessionCode.value  => UnRegisterSessionCode

      case _ => UnsupportedCode(code)
    }
  }

}
