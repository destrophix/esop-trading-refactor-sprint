package com.esop.service

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import java.lang.RuntimeException
import java.math.BigInteger
import javax.annotation.PostConstruct


@Singleton
@Context
class ConfigurationService {

    @Value("\${app.max-inventory-limit}")
    private lateinit var maxInventoryLimit: BigInteger;

    @Value("\${app.max-wallet-limit}")
    private lateinit var maxWalletLimit: BigInteger;

    @Value("\${app.platform-fee-percentage}")
    private var platformFeePercentage: Double = 0.0;

    private fun assertMaxWalletLimitIsValid() {
        if (maxWalletLimit <= BigInteger.ZERO)
            throw RuntimeException( "Max Wallet Limit should be greater than zero")
    }

    private fun assertMaxInventoryLimitIsValid() {
        if(maxInventoryLimit <= BigInteger.ZERO)
            throw RuntimeException("Max Inventory Limit should be greater than zero")
    }

    private fun assertPlatformFeeIsValid() {
        if (platformFeePercentage !in 0.0..1.0)
            throw RuntimeException("Platform Fee Percentage should be in between 0 and 1 both inclusive")
    }

    @PostConstruct
    fun assertValidConfiguration() {
        assertMaxInventoryLimitIsValid()
        assertMaxWalletLimitIsValid()
        assertPlatformFeeIsValid()
    }
}
