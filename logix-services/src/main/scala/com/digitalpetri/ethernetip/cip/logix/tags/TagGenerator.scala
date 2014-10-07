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
            createAtomicTag(
              name          = symbol.symbolName,
              address       = symbol.symbolName,
              path          = path,
              program       = symbol.program,
              dimensions    = arrayInfo,
              parentAddress = None,
              tagType       = symbolType.tagType,
              symbolic      = symbolicAddressing)

          case symbolType: StructuredSymbolType =>
            val template = templates.get(symbolType.templateInstanceId).get

            createStructuredTag(
              name          = symbol.symbolName,
              address       = symbol.symbolName,
              path          = path,
              program       = symbol.program,
              dimensions    = arrayInfo,
              parentAddress = None,
              template      = template,
              symbolic      = symbolicAddressing)
        }
    }
  }

  private def createAtomicTag(name: String,
                              address: String,
                              path: List[EPathSegment],
                              program: Option[String],
                              dimensions: Array[Int],
                              parentAddress: Option[String],
                              tagType: TagType,
                              symbolic: Boolean): LogixTag = {

    if (dimensions.length > 0) {
      val children = for {
        arrayElement <- arrayElements(name, dimensions)
      } yield {
        val elementName = arrayElement.name
        val memberIds = arrayElement.indices.map(i => MemberId(i)).toList
        val elementPath = path ++ memberIds

        val elementAddress = memberIds match {
          case MemberId(d1, _) :: MemberId(d2, _) :: MemberId(d3, _) :: Nil =>
            s"$address[$d1,$d2,$d3]"

          case MemberId(d1, _) :: MemberId(d2, _) :: Nil =>
            s"$address[$d1,$d2]"

          case MemberId(d1, _) :: Nil =>
            s"$address[$d1]"
        }

        createAtomicTag(
          name          = elementName,
          address       = elementAddress,
          path          = elementPath,
          program       = program,
          dimensions    = Array.empty,
          parentAddress = Some(address),
          tagType       = tagType,
          symbolic      = symbolic)
      }

      AtomicTag(name, address, path, program, dimensions, parentAddress, children, tagType)
    } else {
      AtomicTag(name, address, path, program, dimensions, parentAddress, Seq.empty, tagType)
    }
  }

  private def createStructuredTag(name: String,
                                  address: String,
                                  path: List[EPathSegment],
                                  program: Option[String],
                                  dimensions: Array[Int],
                                  parentAddress: Option[String],
                                  template: TemplateInstance,
                                  symbolic: Boolean)
                                 (implicit templates: Map[Int, TemplateInstance]): LogixTag = {

    if (dimensions.length > 0) {
      val children = for {
        arrayElement <- arrayElements(name, dimensions)
      } yield {
        val elementName = arrayElement.name
        val memberIds = arrayElement.indices.map(i => MemberId(i)).toList
        val elementPath = path ++ memberIds

        val elementAddress = memberIds match {
          case MemberId(d1, _) :: MemberId(d2, _) :: MemberId(d3, _) :: Nil =>
            s"$address[$d1,$d2,$d3]"

          case MemberId(d1, _) :: MemberId(d2, _) :: Nil =>
            s"$address[$d1,$d2]"

          case MemberId(d1, _) :: Nil =>
            s"$address[$d1]"
        }

        createStructuredTag(
          name          = elementName,
          address       = elementAddress,
          path          = elementPath,
          program       = program,
          dimensions    = Array.empty,
          parentAddress = Some(address),
          template      = template,
          symbolic      = symbolic)
      }

      StructuredTag(name, address, path, program, dimensions, parentAddress, children, template)
    } else {
      def createMemberTag(member: TemplateMember, index: Int): LogixTag = {
        val infoWord = member.infoWord
        val symbolType = member.symbolType

        val dimensions: Array[Int] = {
          if (symbolType.array) Array[Int](infoWord) // infoWord is the dimension size
          else Array.empty[Int] // scalar
        }

        val memberPath: List[EPathSegment] = {
          if (symbolic) path :+ AnsiDataSegment(member.name)
          else path :+ MemberId(index)
        }

        symbolType match {
          case t: AtomicSymbolType =>
            val tagType = t.tagType match {
              case CipBool(_) => CipBool(infoWord)
              case otherType => otherType
            }

            createAtomicTag(
              name          = member.name,
              address       = s"$address.${member.name}",
              path          = memberPath,
              program       = program,
              dimensions    = dimensions,
              parentAddress = Some(address),
              tagType       = tagType,
              symbolic      = symbolic)

          case t: StructuredSymbolType =>
            createStructuredTag(
              name          = member.name,
              address       = s"$address.${member.name}",
              path          = memberPath,
              program       = program,
              dimensions    = dimensions,
              parentAddress = Some(address),
              template      = templates.get(t.templateInstanceId).get,
              symbolic      = symbolic)
        }
      }

      val children = template.members.zipWithIndex.map(tuple => createMemberTag(tuple._1, tuple._2))

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

}
