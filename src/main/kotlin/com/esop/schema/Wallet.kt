package com.esop.schema

import com.esop.WalletLimitExceededException
import com.esop.constant.MAX_WALLET_CAPACITY

class Wallet{
    private var freeMoney: Long = 0
    private var lockedMoney: Long = 0

    fun addMoneyToWallet(amountToBeAdded : Long){
        if (amountToBeAdded + freeMoney > MAX_WALLET_CAPACITY) {
            throw WalletLimitExceededException()
        }

        this.freeMoney = this.freeMoney + amountToBeAdded
    }

    fun moveMoneyFromFreeToLockedState(amountToBeLocked : Long) : String{
        if( this.freeMoney < amountToBeLocked){
            return "Insufficient funds";
        }
        this.freeMoney = this.freeMoney - amountToBeLocked
        this.lockedMoney = this.lockedMoney + amountToBeLocked
        return "SUCCESS"
    }

    fun getFreeMoney():Long{
        return freeMoney;
    }

    fun getLockedMoney():Long{
        return lockedMoney;
    }

    fun removeMoneyFromLockedState( amountToBeRemoved: Long){
        this.lockedMoney = this.lockedMoney - amountToBeRemoved
    }
}