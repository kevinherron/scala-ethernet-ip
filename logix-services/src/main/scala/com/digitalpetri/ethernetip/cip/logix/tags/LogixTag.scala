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

import com.digitalpetri.ethernetip.cip.epath.EPathSegment
import com.digitalpetri.ethernetip.cip.logix.{TagType, TemplateInstance}

sealed trait LogixTag {
  def name: String
  def address: String
  def path: Seq[EPathSegment]
  def program: Option[String]
  def dimensions: Array[Int]
  def parentAddress: Option[String]
  def children: Seq[LogixTag]
}

case class AtomicTag(name: String,
                     address: String,
                     path: Seq[EPathSegment],
                     program: Option[String],
                     dimensions: Array[Int],
                     parentAddress: Option[String],
                     children: Seq[LogixTag],
                     tagType: TagType) extends LogixTag {

  override def toString: String = {
    val sb = new StringBuilder

    sb.append(s"$productPrefix($program, $name, path=$path, type=$tagType")
    if (dimensions.nonEmpty) sb.append(s", dimensions=${dimensions.mkString("[", ",", "]")}")
    if (children.nonEmpty) sb.append(s", children=$children")
    sb.append(")")

    sb.toString()
  }

}

case class StructuredTag(name: String,
                         address: String,
                         path: Seq[EPathSegment],
                         program: Option[String],
                         dimensions: Array[Int],
                         parentAddress: Option[String],
                         children: Seq[LogixTag],
                         template: TemplateInstance) extends LogixTag {

  override def toString: String = {
    val sb = new StringBuilder

    sb.append(s"$productPrefix($program, $name, path=$path, type=${template.name}")
    if (dimensions.nonEmpty) sb.append(s", dimensions=${dimensions.mkString("[", ",", "]")}")
    if (children.nonEmpty) sb.append(s", children=$children")
    sb.append(")")

    sb.toString()
  }

}
