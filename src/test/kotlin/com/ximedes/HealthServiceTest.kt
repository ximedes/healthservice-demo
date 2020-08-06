package com.ximedes

import junit.framework.TestCase
import kotlinx.coroutines.runBlocking

class HealthServiceTest : TestCase() {

    override fun setUp() {
        HealthService.reset()
    }

    fun testDirectUpdate() {
        val key = "testkey"
        val value = "testvalue"

        runBlocking {
            // Assert that the key does not exist yet
            assertNull(HealthService.getCurrentHealth().get(key))

            // Update an item!
            HealthService.updateItem(key, value)

            // Check that the item is actually stored.
            HealthService.invalidateCache()
            assertEquals(value, HealthService.getCurrentHealth().get(key))
        }
    }

    fun testCallback() {
        var counter = 0

        // Register a callback function
        HealthService.registerCallback("callback") {
            ++counter
        }

        // Assert that the HealthService calls the callback when the cache is stale
        runBlocking {
            HealthService.invalidateCache()
            assertEquals(1, HealthService.getCurrentHealth().get("callback"))
        }
    }

    fun testLogDoubleCallback() {
        LogAsserter(HealthService::class.qualifiedName!!).use { logger ->
            HealthService.registerCallback("testcallback"){
                "callback 1"
            }

            logger.assertNothingLogged()

            HealthService.registerCallback("testcallback"){
                "callback 2"
            }

            logger.assertAnyWarningContains("testcallback")
        }
    }
}