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

package com.digitalpetri.ethernetip.cip.logix.tags

import com.digitalpetri.ethernetip.cip.logix.TagType
import com.digitalpetri.ethernetip.cip.logix.browsing.BrowseState
import io.netty.buffer.ByteBuf

import scala.util.Try

trait LogixTagSupport {

  /**
   * Read all values for `tag` from the response of a [[com.digitalpetri.ethernetip.cip.logix.services.ReadTagService]]
   * or [[com.digitalpetri.ethernetip.cip.logix.services.ReadTagFragmentedService]].
   *
   * @param tag the tag read.
   * @param tagType the [[TagType]] from the response.
   * @param tagData the tag data from the response.
   * @return tag/value pairs for `tag` and all children of `tag`, recursively.
   */
  def readTag(tag: LogixTag, tagType: TagType, tagData: ByteBuf): Try[Seq[(LogixTag, Any)]] = {
    TagReader.readTag(tag, tagType, tagData)
  }

  /**
   * Generate [[LogixTag]]s from a [[BrowseState]].
   *
   * @param browseState the [[BrowseState]] to generate tags from.
   * @param symbolicAddressing whether to use symbolic addressing or instance addressing when generating tag paths.
   * @return a Seq of all top-level [[LogixTag]]s, i.e. tags that are directly backed by a Symbol instance. All other
   *         tags are available as children or children of children belonging to these top-level tags.
   */
  def generateTags(browseState: BrowseState, symbolicAddressing: Boolean = true): Seq[LogixTag] = {
    TagGenerator.generateTags(browseState, symbolicAddressing)
  }

  /**
   * Expand a [[LogixTag]] into a [[Seq]] containing the tag and all its tags, recursively.
   * @param tag the [[LogixTag]] to expand.
   * @return a [[Seq]] containing the given tag and all child tags, recursively.
   */
  def expandTag(tag: LogixTag): Seq[LogixTag] = {
    if (tag.children.isEmpty) Seq(tag)
    else Seq(tag) ++ tag.children.flatMap(expandTag)
  }

}
