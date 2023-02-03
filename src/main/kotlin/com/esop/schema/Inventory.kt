package com.esop.schema

import com.esop.InventoryLimitExceededException
import com.esop.constant.MAX_INVENTORY_CAPACITY
import java.util.*

class Inventory(
    private var freeInventory: Long = 0L,
    private var lockedInventory: Long = 0L,
    private var type: String
) {

    private fun totalESOPQuantity(): Long {
        return freeInventory + lockedInventory
    }

    private fun willInventoryOverflowOnAdding(quantity: Long): Boolean {
        return quantity + totalESOPQuantity() > MAX_INVENTORY_CAPACITY
    }

    fun assertInventoryWillNotOverflowOnAdding(quantity: Long) {
        if (willInventoryOverflowOnAdding(quantity)) throw InventoryLimitExceededException()
    }

    fun addESOPsToInventory(esopsToBeAdded: Long) {
        assertInventoryWillNotOverflowOnAdding(esopsToBeAdded)

        this.freeInventory = this.freeInventory + esopsToBeAdded
    }

    fun moveESOPsFromFreeToLockedState(esopsToBeLocked: Long): String {
        if (this.freeInventory < esopsToBeLocked) {
            return "Insufficient ${type.lowercase(Locale.getDefault())} inventory."
        }
        this.freeInventory = this.freeInventory - esopsToBeLocked
        this.lockedInventory = this.lockedInventory + esopsToBeLocked
        return "SUCCESS"
    }

    fun getFreeInventory(): Long {
        return freeInventory
    }

    fun getLockedInventory(): Long {
        return lockedInventory
    }

    fun removeESOPsFromLockedState(esopsToBeRemoved: Long) {
        this.lockedInventory = this.lockedInventory - esopsToBeRemoved
    }
}