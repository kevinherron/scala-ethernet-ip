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

import com.digitalpetri.ethernetip.cip.epath.{ClassId, InstanceId, PaddedEPath}
import com.digitalpetri.ethernetip.cip.logix._
import com.digitalpetri.ethernetip.cip.logix.browsing.BrowseState.BrowseHash
import com.digitalpetri.ethernetip.cip.logix.services.{GetInstanceAttributeListService, ReadTemplateService}
import com.digitalpetri.ethernetip.client.cip.CipClient
import com.digitalpetri.ethernetip.client.cip.services.GetAttributeListService
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent._

trait LogixBrowseSupport extends StrictLogging {

  private implicit val ec = executionContext

  protected def client: CipClient
  protected def executionContext: ExecutionContext

  def browse(): Future[BrowseState] = {
    val promise = Promise[BrowseState]()

    val future = for {
      browseHash1 <- readBrowseHash()
      browseState <- browse(browseHash1)
      browseHash2 <- readBrowseHash()
    } yield {
      if (browseHash1 == browseHash2) {
        promise.success(browseState)
      } else {
        logger.info(s"Browse state changed mid-browse; re-browsing...")
        promise.completeWith(browse())
      }
    }

    future.onFailure {
      case ex => promise.failure(ex)
    }

    promise.future
  }

  private def browse(browseHash: BrowseHash): Future[BrowseState] = {
    for {
      symbols <- browseSymbols(None)

      templateInstanceIds = symbols.collect {
        case StructuredSymbol(s) => s.templateInstanceId
      }

      templates <- browseTemplates(templateInstanceIds, Map.empty[Int, TemplateInstance])
    } yield {
      BrowseState(symbols, templates, browseHash)
    }
  }

  private def browseSymbols(program: Option[String]): Future[Seq[SymbolInstance]] = {
    logger.debug(s"Browsing ${program.getOrElse("Controller:Global")}")

    val service = new GetInstanceAttributeListService(program)

    for {
      response <- client.invokeService(service)(None)

      futures = response.symbols.collect {
        case ProgramSymbol(programName) => browseSymbols(Some(programName))
      }

      programSymbols <- Future.fold(futures)(Seq.empty[SymbolInstance])((z, s) => z ++ s)
    } yield {
      (response.symbols ++ programSymbols)
        .filterNot(_.symbolType.reserved)
        .filterNot(_.symbolName.startsWith("_"))
    }
  }

  private def browseTemplates(instanceIds: Seq[Int],
                              browsedTemplates: Map[Int, TemplateInstance]): Future[Map[Int, TemplateInstance]] = {

    if (instanceIds.isEmpty) return Future.successful(Map.empty)

    logger.trace(s"browseTemplates(${instanceIds.size})")

    val futures = instanceIds.distinct
      .filterNot(id => browsedTemplates.contains(id))
      .map(id => browseTemplate(id))

    for {
      templates <- Future.fold(futures)(Map.empty[Int, TemplateInstance]) {
        case (z, t) => z + t
      }

      memberInstanceIds = templates.values.toSeq.flatMap { case template =>
        template.members.collect {
          case StructuredMember(t) => t.templateInstanceId
        }
      }

      memberTemplates <- browseTemplates(memberInstanceIds, browsedTemplates ++ templates)
    } yield {
      templates ++ memberTemplates
    }
  }

  private def browseTemplate(templateInstanceId: Int): Future[(Int, TemplateInstance)] = {
    val requestPath = PaddedEPath(
      ClassId(LogixClassCodes.Template),
      InstanceId(templateInstanceId))

    logger.debug(s"Reading template at path: $requestPath")

    for {
      /* Read template attributes and definition */
      attributes <- readTemplateAttributes(templateInstanceId)
      service = new ReadTemplateService(attributes, requestPath)
      response <- client.invokeService(service)(None)
    } yield {
      templateInstanceId -> response.template
    }
  }

  private def readTemplateAttributes(templateInstanceId: Int): Future[TemplateAttributes] = {
    val service = new GetAttributeListService(
      Seq(1, 2, 4, 5), // handle, memberCount, objectDefinitionSize, structureSize
      Seq(2, 2, 4, 4), // short, short, int, int
      PaddedEPath(ClassId(LogixClassCodes.Template), InstanceId(templateInstanceId)))

    val attributes = for {
      response <- client.invokeService(service)(None)
    } yield {
      val attributes = response.attributes.map(ar => ar.id match {
        case 1 => ar.data.map(_.readShort()).get
        case 2 => ar.data.map(_.readShort()).get
        case 4 => ar.data.map(_.readInt()).get
        case 5 => ar.data.map(_.readInt()).get
      })

      TemplateAttributes(attributes(0), attributes(1), attributes(2), attributes(3))
    }

    attributes.recoverWith {
      case ex => Future.failed(new Exception(s"error reading TemplateAttributes for templateInstanceId=$templateInstanceId", ex))
    }
  }

  def readBrowseHash(): Future[BrowseHash] = {
    val service = new GetAttributeListService(
      Seq(1, 2, 3, 4, 10),
      Seq(2, 2, 4, 4, 4),
      PaddedEPath(ClassId(0xAC), InstanceId(0x01)))

    val browseHash = for {
      response <- client.invokeService(service)(None)
    } yield {
      val attributes = response.attributes.map(ar => ar.id match {
        case 1  => ar.data.map(_.readShort()).get
        case 2  => ar.data.map(_.readShort()).get
        case 3  => ar.data.map(_.readInt()).get
        case 4  => ar.data.map(_.readInt()).get
        case 10 => ar.data.map(_.readInt()).get
      })

      (attributes(0), attributes(1), attributes(2), attributes(3), attributes(4))
    }

    browseHash.recoverWith {
      case ex => Future.failed(new Exception("error reading BrowseHash", ex))
    }
  }

  private object ProgramSymbol {
    def unapply(symbol: SymbolInstance): Option[String] = {
      val symbolType: SymbolType = symbol.symbolType

      symbolType match {
        case atomicType: AtomicSymbolType =>
          atomicType.tagType match {
            case LogixProgram => Some(symbol.symbolName)
            case _ => None
          }
        case _ => None
      }
    }
  }

  private object StructuredSymbol {
    def unapply(symbol: SymbolInstance): Option[StructuredSymbolType] = {
      if (symbol.symbolType.structured && symbol.symbolType.isInstanceOf[StructuredSymbolType]) {
        Some(symbol.symbolType.asInstanceOf[StructuredSymbolType])
      } else {
        None
      }
    }
  }

  private object StructuredMember {
    def unapply(member: TemplateMember): Option[StructuredSymbolType] = {
      if (member.symbolType.structured && member.symbolType.isInstanceOf[StructuredSymbolType]) {
        Some(member.symbolType.asInstanceOf[StructuredSymbolType])
      } else {
        None
      }
    }
  }

}
