package com.esop.schema

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class UserTest {
    companion object {
        @JvmStatic
        private fun provideInputForUserEsop(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(20, 10, 10, 10, 10),
                Arguments.of(20, 10, -10, 10, -10)
            )
        }
    }

    @ParameterizedTest
    @MethodSource("provideInputForUserEsop")
    fun `it should transfer Performance ESOPs to the other user`(
        esopToBeAdded: Long,
        esopToBeLocked: Long,
        currentTradeQuantity: Long,
        expectedFreeInventoryForUserOne: Long,
        expectedNonPerformanceInventoryForUserTwo: Long
    ) {
        val userOne = User("Sankaranarayanan", "M", "7550276216", "sankaranarayananm@sahaj.ai", "sankar")
        val userTwo = User("Aditya", "Tiwari", "", "aditya@sahaj.ai", "aditya")
        userOne.userPerformanceInventory.addESOPsToInventory(esopToBeAdded)
        userOne.userPerformanceInventory.moveESOPsFromFreeToLockedState(esopToBeLocked)

        userOne.transferLockedESOPsTo(userTwo, EsopTransferRequest("PERFORMANCE", currentTradeQuantity))

        Assertions.assertEquals(expectedFreeInventoryForUserOne, userOne.userPerformanceInventory.getFreeInventory())
        Assertions.assertEquals(expectedNonPerformanceInventoryForUserTwo, userTwo.userNonPerfInventory.getFreeInventory())
    }
}