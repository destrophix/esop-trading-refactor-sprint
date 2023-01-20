package com.esop.service

import com.esop.dto.UserCreationDTO
import com.esop.schema.User
import com.esop.constant.errors
import com.esop.constant.success_response
import com.esop.schema.Inventory
import com.esop.dto.AddInventoryDTO
import com.esop.dto.AddWalletDTO
import jakarta.inject.Singleton


@Singleton
class UserService {
    val allEmails = mutableSetOf<String>()
    val allNumbers = mutableSetOf<String>()
    val allUsernames = mutableSetOf<String>()
    var allUsers = HashMap<String, User>()

    fun check_inventory(quantity: Long, userName: String, type: String): Boolean{
        val userinventory = all_users[userName]?.inventory!!
        if(type == "normal"){
            if(user_exists(userName) && userinventory.normalInventory.free>= quantity){
                return true
            }
        }else{
            if(user_exists(userName) && userinventory.performanceInventory.free>= quantity){
                return true
            }
        }
        return false
    }
    fun check_wallet(amount: Long, userName: String): Boolean{
        if(user_exists(userName) && allUsers[userName]?.wallet?.free!! >= amount){
            return true
        }
        return false
    }
    fun orderCheckBeforePlace(userName: String, quantity: Long, type: String, price: Long, inventoryType : String): Map<String, MutableList<String>>{
        var userErrors = mutableListOf<String>()
        if(!allUsers.containsKey(userName)){
            userErrors.add("User doesn't exist")
            return mapOf("error" to userErrors)
        }
        if(type == "buy"){
            if(!check_wallet(price*quantity, userName)){
                userErrors.add("Insufficient funds")
            }
        }
        else if(type == "sell"){
            if(inventoryType == "performance"){
                if(!check_inventory(quantity, userName,"performance")){
                    userErrors.add("Insufficient performance inventory")
                }
            }else{
                if(!check_inventory(quantity, userName,"normal")){
                    userErrors.add("Insufficient normal inventory")
                }
            }
        }
        if(userErrors.isEmpty()){
            if(type == "buy"){
                allUsers[userName]?.buyAndUpdateWallet(price*quantity)
            }
            if(type == "sell"){
                allUsers[userName]?.sellAndUpdateInventory(quantity,inventoryType)
            }
        }
        return mapOf("error" to userErrors)
    }
    fun user_exists(userName: String): Boolean{
        return allUsers.containsKey(userName)
    }
    fun check_username(username_set: MutableSet<String>, search_value: String): Boolean {
        return username_set.contains(search_value);
    }

    fun check_phonenumber(usernumber_set: MutableSet<String>, search_value: String): Boolean {
        return usernumber_set.contains(search_value);
    }

    fun check_email(useremail_set: MutableSet<String>, search_value: String): Boolean {
        return useremail_set.contains(search_value);
    }


    fun validateUserDetails(userData: UserCreationDTO): List<String> {
        var Errors = mutableListOf<String>()

        if(check_username(allUsernames, userData.username)){
            Errors.add(errors["USERNAME_EXISTS"].toString())
        }

        else if(check_email(allEmails, userData.email)){
            Errors.add(errors["EMAIL_EXISTS"].toString())
        }
        else if(check_phonenumber(allNumbers, userData.phoneNumber!!)){
            Errors.add(errors["PHONENUMBER_EXISTS"].toString())
        }

        return Errors
    }

    fun registerUser(userData: UserCreationDTO): Map<String,Any> {
    var firstName = userData.firstName!!
    var lastName = userData.lastName!!
    var phoneNumber = userData.phoneNumber!!
    var email = userData.email
    var username = userData.username


    var errorList = validateUserDetails(userData)
     if (errorList.isNotEmpty()) {
         return mapOf("errors" to errorList)
     }


    val user = User(firstName, lastName, phoneNumber, email, username);

    allUsers[username] = user
    allEmails.add(email)
    allNumbers.add(phoneNumber)
    allUsernames.add(username)


    val newUser = mapOf("firstName" to user.firstName.toString(), "lastName" to user.lastName.toString(), "phoneNumber" to user.phoneNumber.toString(), "email" to user.email.toString(), "userName" to user.username.toString() )
    return newUser
}
    fun accountInformation(userName: String): Map<String, Any?> {
        val user = allUsers[userName.toString()]

        var accountErrors = mutableListOf<String>()

        var userData= mapOf("firstName" to user?.firstName.toString() ,"lastName" to user?.lastName.toString(), "phoneNumber" to user?.phoneNumber.toString(), "email" to user?.email.toString(), "wallet" to user?.wallet, "inventory" to user?.inventory)
        if(user!=null){
            return userData
        }

        accountErrors.add(errors["USER_DOES_NOT_EXISTS"].toString())

        return mapOf("error" to accountErrors)
    }


    fun addingInventory(body: AddInventoryDTO, userName: String): Map<String, Any>
    {

        var quant=body.get("quantity").longValue
        var type:String = "normal"
        type = body.get("inventoryType").stringValue.lowercase()

        var accountErrors =mutableListOf<String>()

        var usr1= allUsers[userName]

        if (usr1 != null) {

            if(quant > 10000 || quant <=0){
                accountErrors.add(errors["QUANTITY_NOT_ACCEPTED"].toString())
            }else{
                if(type != "normal" && type != "performance"){
                    accountErrors.add(errors["INVALID_TYPE"].toString())
                }else{
                    usr1.addInventory(quant,type)
                    return if(type == "performance"){
                        mapOf("message" to "${quant} Performance ESOPS added to inventory")
                    }else{
                        mapOf("message" to "${quant} normal added to inventory")
                    }
                }
            }
        }else{
            accountErrors.add(errors["USER_DOES_NOT_EXISTS"].toString())
        }
        return mapOf("error" to accountErrors)
    }

    fun addingMoney(body: AddWalletDTO, userName: String): Map<String, Any>
    {
        var accountErrors =mutableListOf<String>()
        var usr1= allUsers[userName]

        if (usr1 != null) {
            usr1.addWallet(body.price!!)
            return mapOf("message" to "${body.price} amount added to account");
        }
        accountErrors.add(errors["USER_DOES_NOT_EXISTS"].toString())

        return mapOf("error" to accountErrors)

        //return mapOf("errors" to errors["USER_DOES_NOT_EXISTS"]).toString()
    }
}