package com.juul.mcumgr.command

import com.juul.mcumgr.command.CommandResult.Failure

class ErrorResponseException(
    val code: ResponseCode
) : IllegalStateException("Request resulted in error response $code")

/**
 * Result of sending a request over the [Transport], either a [Response] or a [Failure].
 *
 * [Response] indicates that the request produced an mcumgr response, which itself may be an error.
 * [Failure] indicates that the request either failed to send, or failed to produce a response.
 */
sealed class CommandResult<out T> {

    /**
     * Indicates that the request produced an mcumgr response, which itself may be an
     * error. Generally, a non-zero [code] indicates that the response is an error. If the
     * response body failed to be decoded, but the response contained a valid "rc" field (code)
     * then the [Response.body] field will be null.
     */
    data class Response<out T>(
        val body: T?,
        val code: ResponseCode
    ) : CommandResult<T>()

    /**
     * Indicates that the request either failed to send, or failed to produce a response. These
     * failures may be specific to the transport implementation.
     */
    data class Failure<out T>(
        val throwable: Throwable
    ) : CommandResult<T>()

    /**
     * A response has been received with a non-null body and code [ResponseCode.Ok].
     */
    val isSuccess: Boolean
        get() = this is Response && body != null && code.isSuccess

    /**
     * A response has been received win a non-zero code.
     */
    val isError: Boolean
        get() = this is Response && code.isError

    /**
     * The result is a failure.
     */
    val isFailure: Boolean
        get() = this is Failure

    fun exceptionOrNull(): Throwable? =
        when (this) {
            is Failure -> throwable
            else -> null
        }
}

inline fun <T> CommandResult<T>.onSuccess(
    action: (response: T) -> Unit
): CommandResult<T> {
    when (this) {
        is CommandResult.Response -> {
            if (body != null && code.isSuccess) {
                action(body)
            }
        }
    }
    return this
}

inline fun CommandResult<*>.onFailure(action: (throwable: Throwable) -> Unit): CommandResult<*> {
    when (this) {
        is Failure -> action(throwable)
    }
    return this
}

fun <T> CommandResult<T>.getOrThrow(): T {
    return when (this) {
        is CommandResult.Response -> body ?: throw ErrorResponseException(code)
        is Failure -> throw throwable
    }
}

inline fun <T> CommandResult<T>.getOrElse(action: (exception: Throwable) -> T): T {
    return when (this) {
        is CommandResult.Response -> body ?: action(ErrorResponseException(code))
        is Failure -> action(throwable)
    }
}

/*
 * Internal extension functions for more advanced response and error handling.
 */

internal inline fun <T> CommandResult<T>.onResponse(
    action: (CommandResult.Response<T>) -> Unit
): CommandResult<T> {
    when (this) {
        is CommandResult.Response -> action(this)
    }
    return this
}

internal inline fun <T> CommandResult<T>.onResponseCode(
    vararg codes: ResponseCode,
    action: (response: CommandResult.Response<T>) -> Unit
): CommandResult<T> {
    when (this) {
        is CommandResult.Response -> {
            if (codes.contains(code)) {
                action(this)
            }
        }
    }
    return this
}

internal inline fun CommandResult<*>.onErrorOrFailure(action: (throwable: Throwable) -> Unit): CommandResult<*> {
    when (this) {
        is CommandResult.Response -> {
            if (code.isError) {
                action(ErrorResponseException(code))
            }
        }
        is CommandResult.Failure -> action(throwable)
    }
    return this
}

internal inline fun <T, R> CommandResult<T>.mapResponse(transform: (response: T) -> R): CommandResult<R> {
    return when (this) {
        is CommandResult.Response -> {
            if (body != null) {
                CommandResult.Response(transform(body), code)
            } else {
                CommandResult.Response(null, code)
            }
        }
        is CommandResult.Failure<T> -> CommandResult.Failure(throwable)
    }
}

internal fun CommandResult<*>.toUnitResult(): CommandResult<Unit> {
    return when (this) {
        is CommandResult.Response<*> -> CommandResult.Response(Unit, code)
        is CommandResult.Failure<*> -> CommandResult.Failure(throwable)
    }
}
