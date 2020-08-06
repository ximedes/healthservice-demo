package com.ximedes

import mu.KotlinLogging
import java.time.ZonedDateTime

internal val TIMESTAMP_KEY = "healthTimestamp"
internal val STARTUP_TIME_KEY = "applicationStartedAt"

private val logger = KotlinLogging.logger {}

/**
 * Provides functionality for storing and retrieving health
 * information, optionally to be exposed through an endpoint.
 */
object HealthService {
    private val cacheSeconds: Long = 1
    private var startedAt = ZonedDateTime.now()
    private var refreshAt = startedAt.plusSeconds(cacheSeconds)

    /** Holds the values that are reported to the HealthService */
    private val healthValues = initialHealthValue()

    private fun initialHealthValue() =
            mutableMapOf<String, Any>().apply {
                put(STARTUP_TIME_KEY, startedAt)
                put(TIMESTAMP_KEY, startedAt)
            }


    /** Functions that get called by the HealthService when refreshing */
    private val healthCallbacks = mutableMapOf<String, suspend () -> Any>()

    /** The cached health data that gets returned on each call */
    private var cachedHealth = healthValues.toMap()

    /** Resets the HealthService to where it was when it started up */
    fun reset(){
        healthValues.clear()
        healthValues.putAll(initialHealthValue())
        cachedHealth = healthValues.toMap()
        invalidateCache()
    }

    fun invalidateCache() {
        refreshAt = startedAt
    }

    /**
     * Reports current system health. Please note that current
     * system health is cached for one second.
     */
    suspend fun getCurrentHealth(): Map<String, Any> {
        val now = ZonedDateTime.now()
        if (refreshAt.isBefore(now)) {
            healthValues.putAll(executeCallbacks())
            healthValues.put(TIMESTAMP_KEY, now)
            cachedHealth = healthValues.toMap()
            refreshAt = now.plusSeconds(cacheSeconds)
        }
        return cachedHealth
    }

    /**
     * Updates the value of a health item upon internal system event.
     */
    fun updateItem(key: String, value: Any) = healthValues.put(key, value)

    /**
     * Registers a callback function to execute when [getCurrentHealth] is called
     * and the cached value is stale.
     */
    fun registerCallback(key: String, callback: suspend () -> Any) {
        if (healthCallbacks.containsKey(key)) {
            logger.warn {
                "A callback for key '$key' was already registered and will be " +
                        "overwritten with the new callback ${callback.javaClass}."
            }
        }
        healthCallbacks.put(key, callback)
    }

    /** Executes (async) callbacks and returns results for each callback in a map */
    private suspend fun executeCallbacks(): Map<String, Any> =
            HashMap<String, Any>().apply {
                for (healthCallback in healthCallbacks) {
                    put(
                            healthCallback.key,
                            try {
                                healthCallback.value()
                            } catch (e: Exception) {
                                e.message ?: "failed"
                            }
                    )
                }
            }
}