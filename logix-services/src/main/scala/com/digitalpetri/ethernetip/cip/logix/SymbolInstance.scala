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

case class SymbolInstance(instanceId: Int, symbolName: String, symbolType: SymbolType,
                          d1Size: Int, d2Size: Int, d3Size: Int, program: Option[String]) {

  override def toString: String = f"$productPrefix(instanceId=0x$instanceId%04X, name=$symbolName, " +
    f"type=$symbolType, dimensionSizes=[$d1Size, $d2Size, $d3Size])"

}
