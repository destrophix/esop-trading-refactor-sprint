package com.esop.schema

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

class WalletTest {

    companion object {
        @JvmStatic
        private fun provideInputForWallet(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(100, 50, 24, 74, 26),
                Arguments.of(100, 50, -25, 25, 75),
            )
        }
    }

    @ParameterizedTest
    @MethodSource("provideInputForWallet")
    fun `it should move money from locked to free state`(
        amountToBeAdded: Long,
        amountToBeLocked: Long,
        amountToBeMovedFromLockedToFreeState: Long,
        expectedFreeAmount: Long,
        expectedLockAmount: Long
    ) {
        val wallet = Wallet()
        wallet.addMoneyToWallet(amountToBeAdded)
        wallet.moveMoneyFromFreeToLockedState(amountToBeLocked)

        wallet.moveMoneyFromLockedToFree(amountToBeMovedFromLockedToFreeState)

        assertEquals(expectedFreeAmount, wallet.getFreeMoney())
        assertEquals(expectedLockAmount, wallet.getLockedMoney())
    }

    @Test
    fun `it should check for insufficient funds`() {
        val wallet = Wallet()
        wallet.addMoneyToWallet(100)

        val response = wallet.moveMoneyFromFreeToLockedState(150)

        val expectedResult = "Insufficient funds"
        assertEquals(expectedResult, response)
    }

}