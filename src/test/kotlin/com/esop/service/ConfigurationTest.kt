package com.esop.service

import io.micronaut.context.ApplicationContext
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigInteger
import java.util.stream.Stream

@MicronautTest
class ConfigurationTest {
    companion object {
        @JvmStatic
        private fun validMaxWalletValues(): Stream<BigInteger> {
            return Stream.of(
                BigInteger.valueOf(1),
                BigInteger.valueOf(843948),
            )
        }

        @JvmStatic
        private fun invalidMaxWalletValues(): Stream<BigInteger> {
            return Stream.of(
                BigInteger.valueOf(0),
                BigInteger.valueOf(-1),
                BigInteger.valueOf(-3938),
            )
        }

        @JvmStatic
        private fun validMaxInventoryValues(): Stream<BigInteger> {
            return Stream.of(
                BigInteger.valueOf(1),
                BigInteger.valueOf(843948),
            )
        }

        @JvmStatic
        private fun inValidMaxInventoryValues(): Stream<BigInteger> {
            return Stream.of(
                BigInteger.valueOf(0),
                BigInteger.valueOf(-1),
                BigInteger.valueOf(-3938),
            )
        }

        @JvmStatic
        private fun validPlatformFeePercentageValues(): Stream<Double> {
            return Stream.of(
                0.0,
                100.0,
                46.66
            )
        }

        @JvmStatic
        private fun invalidPlatformFeePercentageValues(): Stream<Double> {
            return Stream.of(
                100.1,
                -0.01
            )
        }
    }


    @ParameterizedTest
    @MethodSource("validMaxWalletValues")
    fun `It should read the maximum wallet limit from application environment without throwing exception`(maxWalletLimit: BigInteger) {
        val properties = mapOf("app.max-wallet-limit" to maxWalletLimit)

        val ctx = ApplicationContext.run(properties)
        val configuration = ctx.getBean(Configuration::class.java)
        val actualMaxWalletLimit = configuration.getMaxWalletLimit()

        Assertions.assertEquals(maxWalletLimit, actualMaxWalletLimit)
    }

    @ParameterizedTest
    @MethodSource("invalidMaxWalletValues")
    fun `It should throw Exception when the given maximum wallet limit configuration is not a positive Integer`(
        maxWalletLimit: BigInteger
    ) {
        val properties = mapOf("app.max-wallet-limit" to maxWalletLimit)

        Assertions.assertThrows(BeanInstantiationException::class.java) {
            ApplicationContext.run(properties)
        }
    }


    @ParameterizedTest
    @MethodSource("validMaxInventoryValues")
    fun `It should read the maximum inventory limit from application environment without throwing exception`(
        maxInventoryLimit: BigInteger
    ) {
        val properties = mapOf("app.max-inventory-limit" to maxInventoryLimit)

        val ctx = ApplicationContext.run(properties)
        val configuration = ctx.getBean(Configuration::class.java)
        val actualMaxInventoryLimit = configuration.getMaxInventoryLimit()

        Assertions.assertEquals(maxInventoryLimit, actualMaxInventoryLimit)
    }

    @ParameterizedTest
    @MethodSource("inValidMaxInventoryValues")
    fun `It should throw Exception when the given maximum inventory limit configuration is not a positive Integer`(
        maxInventoryLimit: BigInteger
    ) {
        val properties = mapOf("app.max-inventory-limit" to maxInventoryLimit)

        Assertions.assertThrows(BeanInstantiationException::class.java) {
            ApplicationContext.run(properties)
        }
    }

    @ParameterizedTest
    @MethodSource("validPlatformFeePercentageValues")
    fun `It should read the platform fee percentage from application environment without throwing exception`(
        platformFeePercentage: Double
    ) {
        val properties = mapOf("app.platform-fee-percentage" to platformFeePercentage)

        val ctx = ApplicationContext.run(properties)
        val configuration = ctx.getBean(Configuration::class.java)
        val actualPlatformFeePercentage = configuration.getPlatformFeePercentage()

        Assertions.assertEquals(platformFeePercentage, actualPlatformFeePercentage)
    }

    @ParameterizedTest
    @MethodSource("invalidPlatformFeePercentageValues")
    fun `It should throw Exception when the given platform fee percentage configuration is not between 0 and 1 both inclusive`(
        platformFeePercentage: Double
    ) {
        val properties = mapOf("app.platform-fee-percentage" to platformFeePercentage)

        Assertions.assertThrows(BeanInstantiationException::class.java) {
            ApplicationContext.run(properties)
        }
    }


}