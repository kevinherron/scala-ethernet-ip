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

package com.digitalpetri.ethernetip.cip.logix

case class TemplateInstance(name: String, attributes: TemplateAttributes, members: Seq[TemplateMember])

case class TemplateAttributes(handle: Int, memberCount: Int, objectDefinitionSize: Int, structureSize: Int)

/**
 * @param name        the name of the member
 *
 * @param infoWord    if the member is an atomic data type, the value is zero. If the member is an array data type, the
 *                    value is the array size (max 65535). If the member is a Boolean data type, the value is the bit
 *                    location (0-31; 0-7 if mapped to a SInt).
 *
 * @param symbolType  the [[SymbolType]] (reserved, array/scalar, TagType or Template instance id).
 *
 * @param offset      where the value is located in the stream of bytes returned by reading the parent structure.
 */
case class TemplateMember(name: String, infoWord: Int, symbolType: SymbolType, offset: Int) {
  override def toString: String = f"$productPrefix($name, info=$infoWord, type=$symbolType, offset=$offset)"
}

