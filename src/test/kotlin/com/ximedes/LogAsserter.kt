package com.ximedes

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory
import java.io.Closeable
import kotlin.math.max
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Provides a way to assert that a certain text was sent to the given logger.
 */
class LogAsserter(val logger: Logger) : Closeable {
    private val listAppender: ListAppender<ILoggingEvent> = ListAppender()

    constructor(loggerName: String) : this(LoggerFactory.getLogger(loggerName) as Logger) {}

    init {
        listAppender.start()
        logger.addAppender(listAppender)
    }

    fun assertContainsMessage(message: String) {
        assertTrue(listAppender.list.size > 0, "Nothing was logged.")

        assertTrue(listAppender.list.any { it.message == message },
                "Log entries did not contain message '$message'.\n" +
                        "Last messages were:\n${lastMessages()}"
        )
    }

    fun assertContainsWarning(message: String) {
        assertTrue(listAppender.list.size > 0, "Nothing was logged.")

        assertTrue(listAppender.list.any { it.level == Level.WARN && it.message == message },
                "Log entries did not contain warning '$message'.\n" +
                        "Last messages were:\n${lastMessages()}"
        )
    }

    private fun lastMessages(): String {
        val size = listAppender.list.size
        val startindex = max(0, size - 10)
        val lastTenMessages = listAppender.list
                .subList(startindex, size)
                .map { it.toString() }
                .joinToString("\n")
        return lastTenMessages
    }

    fun assertAnyMessageContains(message: String) {
        assertTrue(listAppender.list.size > 0,
                "Nothing was logged."
        )

        assertTrue(listAppender.list.any { it.message.contains(message) },
                "Log entries did not messages with the text '$message'.\n" +
                        "Last messages were:\n${lastMessages()}"
        )
    }

    fun assertAnyWarningContains(message: String) {
        assertTrue(listAppender.list.size > 0,
                "Nothing was logged."
        )

        assertTrue(listAppender.list.any { it.level == Level.WARN && it.message.contains(message) },
                "Log entries did not warnings with the text '$message'.\n" +
                        "Last messages were:\n${lastMessages()}"
        )
    }

    fun assertNothingLogged() {
        assertEquals(0, listAppender.list.size, "Something was logged.")
    }

    override fun close() {
        logger.detachAppender(listAppender)
    }
}