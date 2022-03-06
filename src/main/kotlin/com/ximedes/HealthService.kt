package com.ximedes

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.time.delay
import mu.KotlinLogging
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

internal val TIMESTAMP_KEY = "healthTimestamp"
internal val STARTUP_TIME_KEY = "applicationStartedAt"

private val logger = KotlinLogging.logger {}

/**
 * Provides functionality for storing and retrieving health
 * information, optionally to be exposed through an endpoint.
 */
object HealthService {
    private var startedAt = ZonedDateTime.now()
    private val callbackEvery = Duration.ofSeconds(1)

    /** Holds the values that are reported to the HealthService */
    private val healthValues = initialHealthValue()
    private val healthCallbacks = ConcurrentHashMap<String, HealthCallBack>()

    /** Holds a cached version of the health status */
    private val cachedHealth = MutableStateFlow<Map<String, Any>>(emptyMap())

    private fun initialHealthValue() =
        ConcurrentHashMap<String, Any>().apply {
            put(STARTUP_TIME_KEY, startedAt)
            put(TIMESTAMP_KEY, startedAt)
        }

    init {
        CoroutineScope(Dispatchers.IO)
            .launch(start = CoroutineStart.DEFAULT) {
                while (true) {
                    healthValues.putAll(executeCallbacks())
                    healthValues.put(TIMESTAMP_KEY, ZonedDateTime.now())
                    cachedHealth.value = healthValues.toSortedMap()
                    delay(callbackEvery)
                }
            }
    }

    val currentHealth: Map<String, Any>
        get() = cachedHealth.value

    /** Resets the HealthService to where it was when it started up */
    fun reset() {
        healthValues.clear()
        healthValues.putAll(initialHealthValue())
        cachedHealth.value = healthValues.toSortedMap()
    }

    /**
     * Updates the value of a health item upon internal system event.
     */
    fun updateItem(key: String, value: Any) = healthValues.put(key, value)

    /**
     * Registers a callback function to execute periodically.
     */
    fun registerCallback(
        key: String,
        timeOut: Duration = Duration.ofSeconds(1),
        timeoutValue: Any = "-timeout-",
        callback: suspend () -> Any
    ) {
        if (healthCallbacks.containsKey(key)) {
            logger.warn {
                "A callback for key '$key' was already registered and will be " +
                        "overwritten with the new callback ${callback.javaClass}."
            }
        }
        healthCallbacks.put(key, HealthCallBack(timeOut, timeoutValue, callback))
    }

    /** Executes (async) callbacks and returns results for each callback in a map */
    private suspend fun executeCallbacks(): Map<String, Any> =
        HashMap<String, Any>().apply {
            for (healthCallback in healthCallbacks) {
                put(
                    healthCallback.key,
                    try {
                        withTimeoutOrNull(healthCallback.value.timeout.toMillis()) {
                            healthCallback.value.callback()
                        } ?: healthCallback.value.timeoutResponse
                    } catch (e: Exception) {
                        e.message ?: "failed"
                    }
                )
            }
        }
}

private data class HealthCallBack(
    val timeout: Duration,
    val timeoutResponse: Any,
    val callback: suspend () -> Any
)