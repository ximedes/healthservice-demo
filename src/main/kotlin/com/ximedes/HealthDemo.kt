package com.ximedes

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.EngineMain.main

fun main(args: Array<String>): Unit = main(args)

fun Application.main() {
    var pageHits = 0
    HealthService.updateItem("pageHits", pageHits)


    install(ContentNegotiation) {
        jackson {
            registerModules(
                    JavaTimeModule(),
                    ZonedDateTimeModule()
            )
        }
    }

    // Register a callback
    HealthService.registerCallback("pipeline") {
        "There are ${this.items.size} items in the ktor PipeLine"
    }

    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK, HealthService.currentHealth)
        }

        get("/pagehit") {
            pageHits += 1

            // Directly report an update to the HealthService
            HealthService.updateItem("pageHits", pageHits)

            call.respond(HttpStatusCode.OK, pageHits)
        }
    }
}