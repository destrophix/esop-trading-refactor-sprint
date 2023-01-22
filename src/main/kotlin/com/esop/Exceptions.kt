package com.esop

import io.micronaut.http.HttpStatus
import kotlin.RuntimeException

open class HttpException(val status: HttpStatus, message: String): RuntimeException(message)

class InventoryLimitExceededException: HttpException(HttpStatus.BAD_REQUEST, "Inventory Limit exceeded")

class WalletLimitExceededException: HttpException(HttpStatus.BAD_REQUEST, "Wallet Limit exceeded")