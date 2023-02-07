package com.esop.schema

import com.esop.dto.AddInventoryDTO
import com.esop.dto.AddWalletDTO

class User(
    var firstName: String,
    var lastName: String,
    var phoneNumber: String,
    var email: String,
    var username: String
) {
    val userWallet: Wallet = Wallet()
    val userNonPerfInventory: Inventory = Inventory(type = "NON_PERFORMANCE")
    val userPerformanceInventory: Inventory = Inventory(type = "PERFORMANCE")
    val orderList: ArrayList<Order> = ArrayList()

    fun addToWallet(walletData: AddWalletDTO): String {
        userWallet.addMoneyToWallet(walletData.price!!)
        return "${walletData.price} amount added to account."
    }

    fun addToInventory(inventoryData: AddInventoryDTO): String {
        if (inventoryData.esopType.toString().uppercase() == "NON_PERFORMANCE") {
            userNonPerfInventory.addESOPsToInventory(inventoryData.quantity!!)
            return "${inventoryData.quantity} Non-Performance ESOPs added to account."
        } else if (inventoryData.esopType.toString().uppercase() == "PERFORMANCE") {
            userPerformanceInventory.addESOPsToInventory(inventoryData.quantity!!)
            return "${inventoryData.quantity} Performance ESOPs added to account."
        }
        return "None"
    }

    fun lockPerformanceInventory(quantity: Long) : String{
        return userPerformanceInventory.moveESOPsFromFreeToLockedState(quantity)
    }

    fun lockNonPerformanceInventory(quantity: Long) : String{
        return userNonPerfInventory.moveESOPsFromFreeToLockedState(quantity)
    }

    fun lockAmount(price: Long): String {
        return userWallet.moveMoneyFromFreeToLockedState(price)
    }
}