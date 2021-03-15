package com.ximedes

import junit.framework.TestCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging

val logger = KotlinLogging.logger { }
class HealthServiceTest : TestCase() {

    override fun setUp() {
        HealthService.reset()
    }


    fun `test direct updates are direct`() {
        val key = "testkey"
        val value = "testvalue"

        runBlocking {
            // Assert that the key does not exist yet
            assertNull(HealthService.currentHealth[key])

            // Update an item!
            HealthService.updateItem(key, value)

            // Check that the item is actually stored.
            assertEquals(value, HealthService.currentHealth[key])
        }
    }

    fun `test should call the callback`() {
        var counter = 0


        runBlocking {
            // Register a callback function
            HealthService.registerCallback("callback") {
                ++counter
            }

            withTimeout(2000) {
                while (!1.equals(HealthService.currentHealth["callback"])) {
                    delay(1)
                }
            }

            assertEquals(1, HealthService.currentHealth["callback"])
        }
    }

    fun `test should log a warning when a callback is re-registered`() {
        LogAsserter(HealthService::class.qualifiedName!!).use { logger ->
            HealthService.registerCallback("testcallback") {
                "callback 1"
            }

            logger.assertNothingLogged()

            HealthService.registerCallback("testcallback") {
                "callback 2"
            }

            logger.assertAnyWarningContains("testcallback")
        }
    }
}

