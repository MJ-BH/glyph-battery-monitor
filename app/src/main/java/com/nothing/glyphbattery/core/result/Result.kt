package com.nothing.glyphbattery.core.result

sealed interface Result<out T, out E : Throwable> {
    data class Success<out T>(val data: T) : Result<T, Nothing>
    data class Failure<out E : Throwable>(val error: E) : Result<Nothing, E>
}

inline fun <T, E : Throwable, R> Result<T, E>.fold(
    onSuccess: (T) -> R,
    onFailure: (E) -> R
): R = when (this) {
    is Result.Success -> onSuccess(data)
    is Result.Failure -> onFailure(error)
}

fun <T, E : Throwable> Result<T, E>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    is Result.Failure -> null
}
