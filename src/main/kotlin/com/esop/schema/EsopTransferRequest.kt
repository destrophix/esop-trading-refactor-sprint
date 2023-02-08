package com.esop.schema

data class EsopTransferRequest(
    val esopType : String,
    val currentTradeQuantity: Long = 0
)