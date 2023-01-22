package com.esop.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.NotBlank


const val MAX_QUANTITY = 1_000_000_000L
const val MAX_PRICE = 1_000_000L

@Introspected
class CreateOrderDTO @JsonCreator constructor(

    @field:NotBlank(message = "Order Type can not be missing or empty.")
    @field:Pattern(regexp = "^(BUY|SELL)$", message = "Invalid Type: should be one of BUY or SELL")
    var type: String? = null,

    @field:NotBlank(message = "Quantity can not be missing or empty.")
    @field:Min(1, message = "Quantity has to be greater than zero")
    @field:Max(MAX_QUANTITY, message = "Quantity has be less than or equal to $MAX_QUANTITY")
    var quantity: Long? = null,

    @JsonProperty("price")
    @field:NotNull(message="Price can not be missing or empty.")
    @field:Min(0, message = "Price cannot be less than zero")
    @field:Max(MAX_PRICE, message = "Price has to be less than or equal to $MAX_PRICE")
    var price: Long? = null,

    @JsonProperty("inventoryType")
    var inventoryType: String? = "NON_PERFORMANCE"
)
