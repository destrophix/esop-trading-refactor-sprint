package com.esop.dto

import com.esop.constant.MAX_WALLET_CAPACITY
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Digits
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull


@Introspected
class AddWalletDTO @JsonCreator constructor(
    @JsonProperty("amount")
    @field:NotNull(message = "Amount can not be missing.")
    @field:Digits(
        integer = 9,
        fraction = 0,
        message = "amount can't exceed maximum wallet capacity of $MAX_WALLET_CAPACITY"
    )
    @field:Min(1, message = "Amount can not be less than zero")
    var price: Long? = null,
)
