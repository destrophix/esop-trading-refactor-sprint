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
    @field:NotNull(message = "property amount is required")
    @field:Min(1, message = "amount cannot be less than zero")
    @field:Max(MAX_PRICE, message = "amount has to be less than or equal to $MAX_PRICE")
    var price: Long? = null,
)
