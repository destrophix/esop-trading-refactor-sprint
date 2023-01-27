package com.esop.dto

import com.esop.constant.MAX_INVENTORY_CAPACITY
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

@Introspected
class AddInventoryDTO @JsonCreator constructor(
    @JsonProperty("quantity")
    @field:NotNull(message = "Quantity can not be missing.")
    @field:Min(1, message = "Quantity has to be greater than zero")
    @field:Max(MAX_INVENTORY_CAPACITY, message = "quantity can't exceed maximum inventory capacity of ${MAX_INVENTORY_CAPACITY.toString()}")
    var quantity: Long? = null,

    @JsonProperty("inventoryType")
    @field:Pattern(regexp = "^((?i)NON_PERFORMANCE|(?i)PERFORMANCE)$", message = "inventoryType should be one of NON_PERFORMANCE or PERFORMANCE")
    var inventoryType: String? = "NON_PERFORMANCE"
)
