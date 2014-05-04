package com.digitalpetri.ethernetip.cip

import scala.collection.immutable.HashMap


object CipStatusCodes {

  type NameAndDescription = (String, String)

  private val statusCodes = HashMap[Int, NameAndDescription](
    0x00 ->("success", "Service was successfully performed by the object specified."),
    0x01 ->("connection failure", "A connection related service failed along the connection path."),
    0x02 ->("resource unavailable", "Resources needed for the object to perform the requested service were unavailable."),
    0x03 ->("invalid parameter value", "A parameter associated with the request was invalid. This code is used when a parameter does not meet the requirements of this specification and/or the requirements defined in an Application Object Specification."),
    0x04 ->("path segment error", "The path segment identifier or the segment syntax was not understood by the processing node. Path processing shall stop when a path segment error is encountered."),
    0x05 ->("path destination unknown", "The path is referencing an object class, instance or structure element that is not known or is not contained in the processing node. Path processing shall stop when a path destination unknown error is encountered."),
    0x06 ->("partial transfer", "Only part of the expected data was transferred."),
    0x07 ->("connection lost", "The messaging connection was lost."),
    0x08 ->("service not supported", "The requested service was not implemented or was not defined for this Object Class/Instance."),
    0x09 ->("invalid attribute value", "Invalid attribute data detected."),
    0x0A ->("attribute list error", "An attribute in the Get_Attribute_List or Set_Attribute_List response has a non-zero status."),
    0x0B ->("already in requested mode/state", "The object is already in the mode/state being requested by the service."),
    0x0C ->("object state conflict", "The object cannot perform the requested service in its current mode/state."),
    0x0D ->("object already exists", "The requested instance of object to be created already exists."),
    0x0E ->("attribute not settable", "A request to modify a non-modifiable attribute was received."),
    0x0F ->("privilege violation", "A permission/privilege check failed."),
    0x10 ->("device state conflict", "The device’s current mode/state prohibits the execution of the requested service."),
    0x11 ->("reply data too large", "The data to be transmitted in the response buffer is larger than the allocated response buffer."),
    0x12 ->("fragmentation of a primitive value", "The service specified an operation that is going to fragment a primitive data value, i.e. half a REAL data type."),
    0x13 ->("not enough data", "The service did not supply enough data to perform the specified operation."),
    0x14 ->("attribute not supported", "The attribute specified in the request is not supported."),
    0x15 ->("too much data", "The service supplied more data than was expected."),
    0x16 ->("object does not exist", "The object specified does not exist in the device."),
    0x17 ->("service fragmentation sequence not in progress", "The fragmentation sequence for this service is not currently active for this data."),
    0x18 ->("no stored attribute data", "The attribute data of this object was not saved prior to the requested service."),
    0x19 ->("store operation failure", "The attribute data of this object was not saved due to a failure during the attempt."),
    0x1A ->("routing failure, request packet too large", "The service request packet was too large for transmission on a network in the path to the destination. The routing device was forced to abort the service."),
    0x1B ->("routing failure, response packet too large", "The service response packet was too large for transmission on a network in the path from the destination. The routing device was forced to abort the service."),
    0x1C ->("missing attribute list entry data", "The service did not supply an attribute in a list of attributes that was needed by the service to perform the requested behavior."),
    0x1D ->("invalid attribute value list", "The service is returning the list of attributes supplied with status information for those attributes that were invalid."),
    0x1E ->("embedded service error", "An embedded service resulted in an error."),
    0x1F ->("vendor specific error", "A vendor specific error has been encountered.")
  )

  def getName(status: Int): Option[String] = statusCodes.get(status).map(_._1)

  def getDescription(status: Int): Option[String] = statusCodes.get(status).map(_._2)

}
