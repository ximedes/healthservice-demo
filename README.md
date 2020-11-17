# Monitoring Application Health

A simple way of monitoring application health in Kotlin.

## Playing with the demo:

1. Start `com.ximedes.HealthDemoKt.main()`
2. Visit http://localhost:8080/health to see health information
3. Visit http://localhost:8080/pagehit to update the page hit counter

## Using HealthService in your project:

Updating a single health item:

```kotlin
 HealthService.updateItem("pageHits", pageHits)
```

Registering a callback that gets called when health information is asked:

```kotlin
HealthService.registerCallback("myLiveStatusInformation") {
    "Your callback code goes here"
}
```

Getting health information as a sorted map:

```kotlin
HealthService.getCurrentHealth()
```

Exposing health information as a REST endpoint in ktor.io:

```kotlin
routing {
    get("/health") {
        call.respond(HttpStatusCode.OK, HealthService.getCurrentHealth())
    }
}
``` 

Have fun!
