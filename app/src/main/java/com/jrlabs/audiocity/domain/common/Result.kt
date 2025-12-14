package com.jrlabs.audiocity.domain.common

/**
 * A sealed class that encapsulates successful outcomes with a value of type [T]
 * or failure outcomes with an [AudioCityError].
 *
 * Provides type-safe error handling following functional programming patterns.
 */
sealed class Result<out T> {

    /**
     * Represents a successful result containing [data].
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed result containing an [error].
     */
    data class Error(val error: AudioCityError) : Result<Nothing>()

    /**
     * Returns true if this is a Success result.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if this is an Error result.
     */
    val isError: Boolean get() = this is Error

    /**
     * Returns the encapsulated value if this is a Success, or null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Returns the encapsulated value if this is a Success, or the [defaultValue] otherwise.
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> defaultValue
    }

    /**
     * Returns the error if this is an Error, or null otherwise.
     */
    fun errorOrNull(): AudioCityError? = when (this) {
        is Success -> null
        is Error -> error
    }

    /**
     * Maps the success value using the given [transform] function.
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    /**
     * Maps the success value using the given [transform] function that returns a Result.
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
    }

    /**
     * Executes [action] if this is a Success.
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Executes [action] if this is an Error.
     */
    inline fun onError(action: (AudioCityError) -> Unit): Result<T> {
        if (this is Error) action(error)
        return this
    }

    companion object {
        /**
         * Creates a Success result with the given [value].
         */
        fun <T> success(value: T): Result<T> = Success(value)

        /**
         * Creates an Error result with the given [error].
         */
        fun <T> error(error: AudioCityError): Result<T> = Error(error)

        /**
         * Wraps a suspending [block] in a Result, catching any exceptions.
         */
        suspend fun <T> of(block: suspend () -> T): Result<T> = try {
            Success(block())
        } catch (e: Exception) {
            Error(AudioCityError.Unknown(e.message ?: "Unknown error", e))
        }
    }
}

/**
 * Sealed class hierarchy representing all possible errors in AudioCity.
 * Follows Open/Closed Principle - new error types can be added without modifying existing code.
 */
sealed class AudioCityError(
    open val message: String,
    open val cause: Throwable? = null
) {

    // Network errors
    sealed class Network(
        override val message: String,
        override val cause: Throwable? = null
    ) : AudioCityError(message, cause) {

        data class NoConnection(
            override val message: String = "No internet connection",
            override val cause: Throwable? = null
        ) : Network(message, cause)

        data class Timeout(
            override val message: String = "Request timed out",
            override val cause: Throwable? = null
        ) : Network(message, cause)

        data class ServerError(
            override val message: String,
            val code: Int,
            override val cause: Throwable? = null
        ) : Network(message, cause)
    }

    // Data errors
    sealed class Data(
        override val message: String,
        override val cause: Throwable? = null
    ) : AudioCityError(message, cause) {

        data class NotFound(
            val entityType: String,
            val entityId: String,
            override val message: String = "$entityType with id $entityId not found"
        ) : Data(message)

        data class ParseError(
            override val message: String = "Failed to parse data",
            override val cause: Throwable? = null
        ) : Data(message, cause)

        data class ValidationError(
            val field: String,
            override val message: String
        ) : Data(message)
    }

    // Location errors
    sealed class Location(
        override val message: String,
        override val cause: Throwable? = null
    ) : AudioCityError(message, cause) {

        data class PermissionDenied(
            override val message: String = "Location permission denied"
        ) : Location(message)

        data class ServiceDisabled(
            override val message: String = "Location services are disabled"
        ) : Location(message)

        data class Unavailable(
            override val message: String = "Location unavailable"
        ) : Location(message)
    }

    // Audio errors
    sealed class Audio(
        override val message: String,
        override val cause: Throwable? = null
    ) : AudioCityError(message, cause) {

        data class InitializationFailed(
            override val message: String = "Failed to initialize audio service",
            override val cause: Throwable? = null
        ) : Audio(message, cause)

        data class LanguageNotSupported(
            val language: String,
            override val message: String = "Language $language is not supported"
        ) : Audio(message)

        data class PlaybackFailed(
            override val message: String = "Audio playback failed",
            override val cause: Throwable? = null
        ) : Audio(message, cause)
    }

    // Trip errors
    sealed class Trip(
        override val message: String,
        override val cause: Throwable? = null
    ) : AudioCityError(message, cause) {

        data class NotFound(
            val tripId: String,
            override val message: String = "Trip $tripId not found"
        ) : Trip(message)

        data class SaveFailed(
            override val message: String = "Failed to save trip",
            override val cause: Throwable? = null
        ) : Trip(message, cause)

        data class InvalidDateRange(
            override val message: String = "End date must be after start date"
        ) : Trip(message)
    }

    // Generic unknown error
    data class Unknown(
        override val message: String = "An unexpected error occurred",
        override val cause: Throwable? = null
    ) : AudioCityError(message, cause)
}
