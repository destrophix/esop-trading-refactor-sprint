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
    private val userWallet: Wallet = Wallet()
    private val userNonPerfInventory: Inventory = Inventory(type = "NON_PERFORMANCE")
    private val userPerformanceInventory: Inventory = Inventory(type = "PERFORMANCE")
    private val orders: ArrayList<Order> = ArrayList()

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

    fun lockPerformanceInventory(quantity: Long): String {
        return userPerformanceInventory.moveESOPsFromFreeToLockedState(quantity)
    }

    fun lockNonPerformanceInventory(quantity: Long): String {
        return userNonPerfInventory.moveESOPsFromFreeToLockedState(quantity)
    }

    fun lockAmount(price: Long): String {
        return userWallet.moveMoneyFromFreeToLockedState(price)
    }
    private fun getInventory(type: String): Inventory {
        if (type == "PERFORMANCE") return userPerformanceInventory
        return userNonPerfInventory
    }

    fun transferLockedESOPsTo(buyer: User, esopTransferData : EsopTransferRequest) {
        this.getInventory(esopTransferData.esopType).removeESOPsFromLockedState(esopTransferData.currentTradeQuantity)
        buyer.getInventory("NON_PERFORMANCE").addESOPsToInventory(esopTransferData.currentTradeQuantity)
    }

    fun addOrder(order: Order) {
        orders.add(order)
    }

    fun getAllOrders(): List<Order> {
        return orders
    }

    fun removeMoneyFromLockedState(amount: Long) {
        userWallet.removeMoneyFromLockedState(amount)
    }

    fun addMoneyToWallet(amount: Long) {
        userWallet.addMoneyToWallet(amount)
    }

    fun moveMoneyFromLockedToFree(amount: Long) {
        userWallet.moveMoneyFromLockedToFree(amount)
    }

    fun getFreeAmountInWallet(): Long {
        return userWallet.getFreeMoney()
    }

    fun getLockedAmountInWallet(): Long {
        return userWallet.getLockedMoney()
    }

    fun getFreeESOPsInInventory(esopType: String): Long {
        return getInventory(esopType).getFreeInventory()
    }

    fun getLockedESOPsInInventory(esopType: String): Long {
        return getInventory(esopType).getLockedInventory()
    }

    fun lockESOPs(esopType: String, quantity: Long) {
        getInventory(esopType).moveESOPsFromFreeToLockedState(quantity)
    }

    fun getTotalESOP(): Long {
        return userPerformanceInventory.getFreeInventory() + userPerformanceInventory.getLockedInventory() + userNonPerfInventory.getFreeInventory() + userNonPerfInventory.getLockedInventory()
    }

    fun getTotalAmount(): Long {
        return userWallet.getFreeMoney() + userWallet.getLockedMoney()
    }
}