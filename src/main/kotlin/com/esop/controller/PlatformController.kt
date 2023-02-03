package com.esop.controller

import com.esop.schema.PlatformFee.Companion.getPlatformFee
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller
class PlatformController {
    @Get(uri = "/platformFee", produces = [MediaType.APPLICATION_JSON])
    fun platformFee(): HttpResponse<*> {
        return HttpResponse.ok(
            mapOf(
                "platformFee" to getPlatformFee()
            )
        )
    }

}