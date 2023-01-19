package com.esop.schema

class ItemData{
    var free: Long = 0
    var locked: Long = 0
}

class Inventory{
    var normalInventory :ItemData = ItemData()
    var performanceInventory :ItemData = ItemData()
}

class User {
    var firstName: String = ""
    var lastName: String = ""
    var phoneNumber: String = ""
    var email: String = ""
    var username: String = ""



    val wallet: ItemData = ItemData()
    val inventory: Inventory = Inventory()

    constructor(fName: String, lName: String, pNumber: String, em: String, uname: String){
        firstName = fName
        lastName = lName
        phoneNumber = pNumber
        email = em
        username = uname
    }

    fun addWallet(amount: Long){
        // A function to add amount to the users free wallet
        wallet.free += amount
    }
    fun addInventory(quantity: Long,type: String){
        // A function to add quantity to the users inventory
        if(type == "normal") {
            inventory.normalInventory.free += quantity
        }else{
            inventory.performanceInventory.free += quantity
        }

    }
    fun buyAndUpdateWallet(amount: Long){
        // A function which after buy order,
        // updates the free amount and places it into the locked amount
        wallet.free -= amount
        wallet.locked += amount
    }
    fun sellAndUpdateInventory(quantity: Long){
        // A function which after sell order,
        // updates the free quantity and places it into the locked quantity
//        inventory.free -= quantity
//        inventory.locked += quantity
    }

    fun orderWalletFree(amount: Long){
        // Function which adds given amount to the users wallets
        // after successful selling of his inventory
        // Or when a Users buy order is satisfied for less amount
        wallet.free += amount
    }

}