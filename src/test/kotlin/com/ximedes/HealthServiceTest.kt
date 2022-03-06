package com.ximedes

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

val logger = KotlinLogging.logger { }

class HealthServiceTest {

    @BeforeEach
    fun setUp() {
        HealthService.reset()
    }


    @Test
    fun `test direct updates are delayed`() {
        val key = "testkey"
        val value = "testvalue"

        runBlocking {
            // Assert that the key does not exist yet
            assertNull(HealthService.currentHealth[key])

            // Update an item
            HealthService.updateItem(key, value)
            delay(1200)

            // Check that the item is actually stored.
            assertEquals(value, HealthService.currentHealth[key])
        }
    }

    @Test
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

    @Test
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

