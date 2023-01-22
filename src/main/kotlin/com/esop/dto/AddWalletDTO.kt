package com.esop.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull


@Introspected
class AddWalletDTO @JsonCreator constructor(
    @JsonProperty("amount")
    @field:NotNull(message = "Amount can not be missing.")
    @field:Min(1, message = "Amount can not be less than zero")
    @field:Max(1000, message = "Amount has to be less than or equal to 1000")
    var price: Long? = null,
)
