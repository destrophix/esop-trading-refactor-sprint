package com.esop.schema

import com.esop.dto.AddInventoryDTO
import com.esop.dto.AddWalletDTO

class User ( var firstName: String,
             var lastName: String,
             var phoneNumber: String,
             var email: String,
             var username: String){
    val userWallet: Wallet = Wallet()
    val userNormalInventory: Inventory = Inventory(type = "NORMAL")
    val userPerformanceInventory: Inventory = Inventory(type = "PERFORMANCE")
    val orderList: ArrayList<Order> = ArrayList<Order>()

    fun addToWallet(walletData: AddWalletDTO): String {
        userWallet.addMoneyToWallet(walletData.price!!)
        return "${walletData.price} amount added to account."
    }
    fun addToInventory(inventoryData: AddInventoryDTO): String {
        if(inventoryData.type == "NORMAL") {
            userNormalInventory.addESOPsToInventory(inventoryData.quantity!!)
            return "${inventoryData.quantity} normal ESOPs added to account."
        }else if( inventoryData.type == "PERFORMANCE" ){
            userPerformanceInventory.addESOPsToInventory(inventoryData.quantity!!)
            return "${inventoryData.quantity} performance ESOPs added to account."
        }
        return "None"
    }

}