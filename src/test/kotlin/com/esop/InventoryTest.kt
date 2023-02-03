package com.esop

import com.esop.constant.MAX_INVENTORY_CAPACITY
import com.esop.schema.Inventory
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@MicronautTest
class InventoryTest {

    @Test
    fun `It should be able to retrieve the free inventory amount`() {
        val expectedFreeInventory = 100L
        val inventory = Inventory(freeInventory = expectedFreeInventory, lockedInventory = 10, type = "PERFORMANCE")

        val actualFreeInventory = inventory.getFreeInventory()

        Assertions.assertEquals(expectedFreeInventory, actualFreeInventory)
    }

    @Test
    fun `It should be able to retrieve the locked inventory amount`() {
        val expectedLockedInventory = 10L
        val inventory = Inventory(freeInventory = 100, lockedInventory = expectedLockedInventory, type = "PERFORMANCE")

        val actualLockedInventory = inventory.getLockedInventory()

        Assertions.assertEquals(expectedLockedInventory, actualLockedInventory)
    }

    @Test
    fun `It should remove locked ESOPs by the given quantity`() {
        val initialLockedESOPS = 10L
        val deductQuantity = 10L
        val inventory = Inventory(freeInventory = 100, lockedInventory = initialLockedESOPS, type = "PERFORMANCE")
        val expectedNewLockedESOPs = initialLockedESOPS - deductQuantity

        inventory.removeESOPsFromLockedState(deductQuantity)
        val actualLockedESOPs = inventory.getLockedInventory()

        Assertions.assertEquals(expectedNewLockedESOPs, actualLockedESOPs)
    }

    @Test
    fun `It should throw error when the total inventory is greater than the inventory limit`() {
        val inventory = Inventory(freeInventory = 1, lockedInventory = 0, type = "PERFORMANCE")

        Assertions.assertThrows(
            InventoryLimitExceededException::class.java,
            fun() { inventory.addESOPsToInventory(MAX_INVENTORY_CAPACITY) })
    }


    @Test
    fun `It should move ESOPs from free to locked by the given quantity and should return SUCCESS on success`() {
        val initialFreeInventory = 100L
        val initialLockedInventory = 0L
        val esopsToBeMoved = 10L
        val inventory = Inventory(
            freeInventory = initialFreeInventory,
            lockedInventory = initialLockedInventory,
            type = "PERFORMANCE"
        )

        val message = inventory.moveESOPsFromFreeToLockedState(esopsToBeMoved)
        val actualFreeInventory = inventory.getFreeInventory()
        val actualLockedInventory = inventory.getLockedInventory()

        Assertions.assertEquals("SUCCESS", message)
        Assertions.assertEquals(initialFreeInventory - esopsToBeMoved, actualFreeInventory)
        Assertions.assertEquals(initialLockedInventory + esopsToBeMoved, actualLockedInventory)
    }
}