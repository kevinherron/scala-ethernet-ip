package com.digitalpetri.ethernetip.cip.logix.tags

import com.digitalpetri.ethernetip.cip.epath._
import com.digitalpetri.ethernetip.cip.logix._
import com.digitalpetri.ethernetip.cip.logix.browsing.BrowseState

object TagGenerator {

  private[tags] def generateTags(browseState: BrowseState, symbolicAddressing: Boolean = true): Seq[LogixTag] = {
    val symbols = browseState.symbols
    implicit val templates = browseState.templates

    symbols.map {
      symbol =>
        val dimensions = symbol.symbolType.dimensionCount
        val sizes = Array(symbol.d1Size, symbol.d2Size, symbol.d3Size)
        val arrayInfo = new Array[Int](dimensions)
        for (i <- 0 until dimensions) arrayInfo(i) = sizes(i)

        val program = symbol.program.fold(List.empty[EPathSegment])(p => List(AnsiDataSegment(p)))

        val path: List[EPathSegment] = {
          if (symbolicAddressing) program :+ AnsiDataSegment(symbol.symbolName)
          else program :+ ClassId(LogixClassCodes.Symbol) :+ InstanceId(symbol.instanceId)
        }

        symbol.symbolType match {
          case symbolType: AtomicSymbolType =>
            createAtomicTag(symbol.symbolName, path, symbol.program, arrayInfo, None, symbolType.tagType)

          case symbolType: StructuredSymbolType =>
            val template = templates.get(symbolType.templateInstanceId).get

            createStructuredTag(template.name, path, symbol.program, arrayInfo, None, template)
        }
    }
  }

  private def createAtomicTag(name: String,
                              path: List[EPathSegment],
                              program: Option[String],
                              dimensions: Array[Int],
                              parentAddress: Option[String],
                              tagType: TagType): LogixTag = {

    val address = tagAddress(name, path)

    if (dimensions.length > 0) {
      val children = for {
        arrayElement <- arrayElements(name, dimensions)
      } yield {
        val elementName = arrayElement.name
        val elementPath = path ++ arrayElement.indices.map(i => MemberId(i))

        createAtomicTag(elementName, elementPath, program, Array.empty, Some(address), tagType)
      }

      AtomicTag(name, address, path, program, dimensions, parentAddress, children, tagType)
    } else {
      AtomicTag(name, address, path, program, dimensions, parentAddress, Seq.empty, tagType)
    }
  }

  private def createStructuredTag(name: String,
                                  path: List[EPathSegment],
                                  program: Option[String],
                                  dimensions: Array[Int],
                                  parentAddress: Option[String],
                                  template: TemplateInstance)
                                 (implicit templates: Map[Int, TemplateInstance]): LogixTag = {

    val address = tagAddress(name, path)

    if (dimensions.length > 0) {
      val children = for {
        arrayElement <- arrayElements(name, dimensions)
      } yield {
        val elementName = arrayElement.name
        val elementPath = path ++ arrayElement.indices.map(i => MemberId(i))

        createStructuredTag(elementName, elementPath, program, Array.empty, Some(address), template)
      }

      StructuredTag(name, address, path, program, dimensions, parentAddress, children, template)
    } else {
      def createMemberTag(member: TemplateMember): LogixTag = {
        val infoWord = member.infoWord
        val symbolType = member.symbolType

        val dimensions: Array[Int] = {
          if (symbolType.array) Array[Int](infoWord) // infoWord is the dimension size
          else Array.empty[Int] // scalar
        }

        val memberPath = path :+ AnsiDataSegment(member.name)

        symbolType match {
          case t: AtomicSymbolType =>
            val tagType = t.tagType match {
              case CipBool(_) => CipBool(infoWord)
              case otherType => otherType
            }

            createAtomicTag(member.name, memberPath, program, dimensions, Some(address), tagType)

          case t: StructuredSymbolType =>
            createStructuredTag(member.name, memberPath, program, dimensions, Some(address), templates.get(t.templateInstanceId).get)
        }
      }

      val children = template.members.map(createMemberTag)

      StructuredTag(name, address, path, program, dimensions, parentAddress, children, template)
    }
  }

  private case class ArrayElement(name: String, indices: Array[Int])

  private def arrayElements(prefix: String, arrayDimensions: Array[Int]): List[ArrayElement] = {
    val ls = for (i <- 0 until arrayDimensions.takeWhile(_ > 0).length) yield List.range(0, arrayDimensions(i))
    val product = cartesianProduct(ls.toList)

    for {
      p <- product
    } yield {
      val name = p.addString(new StringBuilder, prefix + "[", ",", "]").mkString
      val indices = p.toArray

      ArrayElement(name, indices)
    }
  }

  private def cartesianProduct[T](listOfLists: List[List[T]]): List[List[T]] = listOfLists match {
    case Nil => List(List())
    case xs :: xss => for (y <- xs; ys <- cartesianProduct(xss)) yield y :: ys
  }

  private def tagAddress(tagName: String, tagPath: Seq[EPathSegment]): String = {
    def _path(segments: List[EPathSegment], path: String): String = {
      segments match {
        case Nil => path

        case ClassId(_, _) :: InstanceId(_, _) :: remaining =>
          if (path.length == 0) _path(remaining, tagName)
          else _path(remaining, s"$path.$tagName")

        case AnsiDataSegment(s1) :: remaining =>
          if (path.length == 0) _path(remaining, s1)
          else _path(remaining, s"$path.$s1")

        case MemberId(d1, _) :: MemberId(d2, _) :: MemberId(d3, _) :: remaining =>
          _path(remaining, s"$path[$d1,$d2,$d3]")

        case MemberId(d1, _) :: MemberId(d2, _) :: remaining =>
          _path(remaining, s"$path[$d1,$d2]")

        case MemberId(d1, _) :: remaining =>
          _path(remaining, s"$path[$d1]")

        case segment :: remaining =>
          // Don't append unknown segment types.
          _path(remaining, path)
      }
    }

    _path(tagPath.toList, "")
  }

}
