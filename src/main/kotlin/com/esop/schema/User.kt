package com.esop.schema

import com.esop.dto.AddInventoryDTO
import com.esop.dto.AddWalletDTO

class User ( var firstName: String,
             var lastName: String,
             var phoneNumber: String,
             var email: String,
             var username: String){
    val userWallet: Wallet = Wallet()
    val userNonPerfInventory: Inventory = Inventory(type = "NON_PERFORMANCE")
    val userPerformanceInventory: Inventory = Inventory(type = "PERFORMANCE")
//    val orderList: ArrayList<Order> = ArrayList<Order>()
    val orderList: ArrayList<History> = ArrayList<History>()

    fun addToWallet(walletData: AddWalletDTO): String {
        userWallet.addMoneyToWallet(walletData.price!!)
        return "${walletData.price} amount added to account."
    }
    fun addToInventory(inventoryData: AddInventoryDTO): String {
        if(inventoryData.inventoryType == "NON_PERFORMANCE") {
            userNonPerfInventory.addESOPsToInventory(inventoryData.quantity!!)
            return "${inventoryData.quantity} NON_PERFORMANCE ESOPs added to account."
        }else if( inventoryData.inventoryType == "PERFORMANCE" ){
            userPerformanceInventory.addESOPsToInventory(inventoryData.quantity!!)
            return "${inventoryData.quantity} PERFORMANCE ESOPs added to account."
        }
        return "None"
    }

}