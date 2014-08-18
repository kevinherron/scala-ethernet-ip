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

package com.digitalpetri.ethernetip.cip.logix.browsing

import com.digitalpetri.ethernetip.cip.logix.browsing.BrowseState.BrowseHash
import com.digitalpetri.ethernetip.cip.logix.{SymbolInstance, TemplateInstance}

/**
 * @param symbols all non-reserved [[SymbolInstance]]s from obtained from the browse.
 * @param templates all [[TemplateInstance]]s obtained from the browse.
 * @param browseHash the [[BrowseHash]].
 */
case class BrowseState(symbols: Seq[SymbolInstance], templates: Map[Int, TemplateInstance], browseHash: BrowseHash)

object BrowseState {

  /**
   * A 5-tuple representing attributes 1, 2, 3, 4, and 10 of class 0xAC. As described by "1756-PM020C-EN-P", any time
   * one of these attributes changes we should assume our browse state is no longer valid and initiate a re-browse.
   */
  type BrowseHash = (Int, Int, Int, Int, Int)

}
