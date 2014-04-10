package com.digitalpetri.ethernetip.cip

import io.netty.buffer.ByteBuf


case class MessageRouterRequest(serviceCode: Int, requestPath: EPath, requestData: ByteBuf)

object MessageRouterRequest {

  def encode(request: MessageRouterRequest, buffer: ByteBuf) {
    buffer.writeByte(request.serviceCode)
    EPath.encode(request.requestPath, buffer)
    buffer.writeBytes(request.requestData)
  }

  def decode(buffer: ByteBuf): MessageRouterRequest = {
    MessageRouterRequest(
      serviceCode = buffer.readUnsignedByte(),
      requestPath = EPath.decode(buffer),
      requestData = buffer.slice())
  }

}


case class MessageRouterResponse(serviceCode: Int,
                                 generalStatus: Short,
                                 additionalStatus: Seq[Short],
                                 data: Option[ByteBuf])

object MessageRouterResponse {

  def encode(response: MessageRouterResponse, buffer: ByteBuf) {
    buffer.writeShort(response.serviceCode)
    buffer.writeByte(0x00)
    buffer.writeByte(response.generalStatus)
    buffer.writeByte(response.additionalStatus.size)
    response.additionalStatus.foreach(s => buffer.writeShort(s))
    response.data.foreach(buffer.writeBytes)
  }

  def decode(buffer: ByteBuf): MessageRouterResponse = {
    val replyService  = buffer.readUnsignedShort()
    val reserved      = buffer.readByte()
    val generalStatus = buffer.readUnsignedByte()

    assert(reserved == 0)

    def decodeAdditionalStatus(additional: List[Short], count: Int): List[Short] = {
      if (count == 0) additional
      else decodeAdditionalStatus(additional :+ buffer.readShort(), count - 1)
    }

    val additionalStatus = decodeAdditionalStatus(List.empty, buffer.readUnsignedByte())

    val data: Option[ByteBuf] = {
      if (generalStatus != 0x00) None
      else Some(buffer.slice())
    }

    MessageRouterResponse(replyService, generalStatus, additionalStatus, data)
  }

}
