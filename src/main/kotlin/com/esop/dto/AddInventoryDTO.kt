package com.esop.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

@Introspected
class AddInventoryDTO @JsonCreator constructor(
    @JsonProperty("quantity")
    @field:NotNull(message = "Quantity can not be missing.")
    @field:Min(1, message = "Quantity has to be greater than zero")
    @field:Max(10000000, message = "Quantity has to be less than or equal to 10000000")
    var quantity: Long? = null,

    @JsonProperty("inventoryType")
    var inventoryType: String? = "NON_PERFORMANCE"
)
