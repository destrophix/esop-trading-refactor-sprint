package com.esop.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.context.annotation.Value
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern


const val MAX_QUANTITY = 1_000_000_000L
const val MAX_PRICE = 1_000_000L

@Introspected
class CreateOrderDTO @JsonCreator constructor(

    @field:NotNull(message = "property order type is required")
    @field:Pattern(regexp = "^(BUY|SELL)$", message = "Invalid Type: should be one of BUY or SELL")
    var type: String? = null,

    @field:NotNull(message = "property quantity is required")
    @field:Min(1, message = "quantity has to be greater than zero")
    @field:Max(MAX_QUANTITY, message = "quantity has be less than or equal to $MAX_QUANTITY")
    var quantity: Long? = null,

    @JsonProperty("price")
    @field:NotNull(message="property price is required")
    @field:Min(0, message = "price cannot be less than zero")
    @field:Max(MAX_PRICE, message = "price has to be less than or equal to $MAX_PRICE")
    var price: Long? = null
)
