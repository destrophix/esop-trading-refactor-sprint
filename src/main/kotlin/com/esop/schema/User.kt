package com.esop.schema

class ItemData{
    var free: Int = 0
    var locked: Int = 0
}
class User {
    var firstName: String = ""
    var lastName: String = ""
    var phoneNumber: Int = -1
    var email: String = ""
    var username: String = ""

    val wallet: ItemData = ItemData()
    val inventory: ItemData = ItemData()

    constructor(fName: String, lName: String, pNumber: Int, em: String, uname: String){
        firstName = fName
        lastName = lName
        phoneNumber = pNumber
        email = em
        username = uname
    }

    fun addWallet(amount: Int){
        // A function to add amount to the users free wallet
        wallet.free += amount
    }
    fun addInventory(quantity: Int){
        // A function to add quantity to the users inventory
        inventory.free += quantity
    }
    fun buyAndUpdateWallet(amount: Int){
        // A function which after buy order,
        // updates the free amount and places it into the locked amount
        wallet.free -= amount
        wallet.locked += amount
    }
    fun sellAndUpdateInventory(quantity: Int){
        // A function which after sell order,
        // updates the free quantity and places it into the locked quantity
        inventory.free -= quantity
        inventory.locked += quantity
    }

    fun orderWalletFree(amount: Int){
        // Function which adds given amount to the users wallets
        // after successful selling of his inventory
        // Or when a Users buy order is satisfied for less amount
        wallet.free += amount
    }
}