package com.esop.schema

import com.esop.WalletLimitExceededException
import com.esop.constant.MAX_WALLET_CAPACITY

class Wallet {
    private var freeMoney: Long = 0
    private var lockedMoney: Long = 0

    private fun totalMoneyInWallet(): Long {
        return freeMoney + lockedMoney
    }

    private fun willWalletOverflowOnAdding(amount: Long): Boolean {
        return amount + totalMoneyInWallet() > MAX_WALLET_CAPACITY
    }

    fun assertWalletWillNotOverflowOnAdding(amount: Long) {
        if (willWalletOverflowOnAdding(amount)) throw WalletLimitExceededException()
    }

    fun addMoneyToWallet(amountToBeAdded: Long) {
        assertWalletWillNotOverflowOnAdding(amountToBeAdded)

        this.freeMoney = this.freeMoney + amountToBeAdded
    }

    fun moveMoneyFromFreeToLockedState(amountToBeLocked: Long): String {
        if (this.freeMoney < amountToBeLocked) {
            return "Insufficient funds"
        }
        this.freeMoney = this.freeMoney - amountToBeLocked
        this.lockedMoney = this.lockedMoney + amountToBeLocked
        return "SUCCESS"
    }

    fun getFreeMoney(): Long {
        return freeMoney
    }

    fun getLockedMoney(): Long {
        return lockedMoney
    }

    fun removeMoneyFromLockedState(amountToBeRemoved: Long) {
        this.lockedMoney = this.lockedMoney - amountToBeRemoved
    }

    fun moveMoneyFromLockedToFree(amount: Long) {
        removeMoneyFromLockedState(amount)
        addMoneyToWallet(amount)
    }
}