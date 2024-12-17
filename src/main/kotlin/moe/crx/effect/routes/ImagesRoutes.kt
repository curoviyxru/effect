package moe.crx.effect.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import java.io.File

fun Route.imageRoute() {
    get("/uploads/{image}") {
        val image = call.parameters["image"]

        if (image == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        call.respondFile(File("uploads", image))
    }
}