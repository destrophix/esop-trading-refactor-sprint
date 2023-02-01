package com.esop.service

import com.esop.exceptions.InvalidConfigurationException
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import java.math.BigInteger
import javax.annotation.PostConstruct


@Singleton
@Context
class ConfigurationService {
    private fun checkMaxWalletLimitIsValid() {
        if (maxWalletLimit <= BigInteger.ZERO)
            throw InvalidConfigurationException( "Max Wallet Limit should be greater than zero")
    }

    private fun checkMaxInventoryLimitIsValid() {
        if (maxInventoryLimit <= BigInteger.ZERO)
            throw InvalidConfigurationException("Max Inventory Limit should be greater than zero")
    }

    private fun checkPlatformFeeIsValid() {
        if (platformFeePercentage !in 0.0..1.0)
            throw InvalidConfigurationException("Platform Fee Percentage should be in between 0 and 1 both inclusive" )
    }

    @Value("\${app.max-inventory-limit}")
    private lateinit var maxInventoryLimit: BigInteger

    @Value("\${app.max-wallet-limit}")
    private lateinit var maxWalletLimit: BigInteger

    @Value("\${app.platform-fee-percentage}")
    private var platformFeePercentage: Double = 0.0

    @PostConstruct
    fun checkConfigurationIsValid() {
        checkMaxInventoryLimitIsValid()
        checkMaxWalletLimitIsValid()
        checkPlatformFeeIsValid()
    }
}
