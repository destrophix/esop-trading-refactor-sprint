package com.esop.schema

import com.esop.InventoryLimitExceededException
import com.esop.constant.MAX_INVENTORY_CAPACITY

class Inventory(
    private var freeInventory: Long = 0L ,
    private var lockedInventory: Long = 0L,
    private var type: String
) {
    fun addESOPsToInventory(esopsToBeAdded: Long) {
        if (esopsToBeAdded + freeInventory > MAX_INVENTORY_CAPACITY) {
            throw InventoryLimitExceededException()
        }

        this.freeInventory = this.freeInventory + esopsToBeAdded
    }

    fun moveESOPsFromFreeToLockedState(esopsToBeLocked: Long): String {
        if (this.freeInventory < esopsToBeLocked) {
            return "Insufficient ${type.toLowerCase()} inventory.";
        }
        this.freeInventory = this.freeInventory - esopsToBeLocked
        this.lockedInventory = this.lockedInventory + esopsToBeLocked
        return "SUCCESS"
    }

    fun getFreeInventory():Long{
        return freeInventory;
    }

    fun getLockedInventory():Long{
        return lockedInventory;
    }

    fun removeESOPsFromLockedState( esopsToBeRemoved: Long){
        this.lockedInventory = this.lockedInventory - esopsToBeRemoved
    }
}