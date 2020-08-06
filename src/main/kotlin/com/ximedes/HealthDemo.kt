package com.ximedes

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain.main
import kotlin.random.Random

fun main(args: Array<String>): Unit = main(args)

fun Application.main() {
    var pageHits = 0

    install(ContentNegotiation) {
        jackson {
            registerModules(
                    JavaTimeModule(),
                    ZonedDateTimeModule()
            )
        }
    }

    // Register a callback
    HealthService.registerCallback("randomNumber") {
        "There are ${this.items.size} items in the ktor PipeLine"
    }

    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK, HealthService.getCurrentHealth())
        }

        get("/pagehit") {
            pageHits += 1

            // Directly report an update to the HealthService
            HealthService.updateItem("pageHits", pageHits)

            call.respond(HttpStatusCode.OK, pageHits)
        }
    }
}