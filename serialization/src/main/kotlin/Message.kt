package com.juul.mcumgr.serialization

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.node.ObjectNode
import com.juul.mcumgr.McuMgrResult
import com.juul.mcumgr.message.Command
import com.juul.mcumgr.message.Group
import com.juul.mcumgr.message.Operation
import com.juul.mcumgr.message.Response
import java.io.IOException

data class Message(
    val header: Header,
    val payload: ObjectNode
)

data class Header(
    val operation: Int,
    val group: Int,
    val command: Int,
    val length: Int, // UInt16
    val sequenceNumber: Int, // UInt8
    val flags: Byte
)

fun <T> Message.toResult(type: Class<T>): McuMgrResult<T> {
    val rawCode = payload["rc"].asInt(-1)
    val code = Response.Code.valueOf(rawCode) ?: Response.Code.Ok
    return try {
        val response = cbor.treeToValue(payload, type)
        McuMgrResult.Success(response, code)
    } catch (e: IOException) {
        // Failed to parse full response. Try to get code.
        when (code) {
            Response.Code.Ok -> McuMgrResult.Failure(e)
            else -> McuMgrResult.Error(code)
        }
    }
}

/**
 * The serialization object defining a request.
 */
@JsonIgnoreProperties("operation", "group", "command")
internal abstract class RequestDefinition {
    abstract val operation: Operation
    abstract val group: Group
    abstract val command: Command
}

/**
 * The serialization object defining a response.
 */
internal abstract class ResponseDefinition
