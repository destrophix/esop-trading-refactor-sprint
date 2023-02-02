package com.esop.service

import com.esop.constant.errors
import com.esop.dto.AddInventoryDTO
import com.esop.dto.AddWalletDTO
import com.esop.dto.UserCreationDTO
import com.esop.schema.User
import jakarta.inject.Singleton
import com.esop.schema.Order

@Singleton
class UserService {
    companion object {
        val emailList = mutableSetOf<String>()
        val phoneNumberList = mutableSetOf<String>()
        var userList = HashMap<String, User>()

        fun orderCheckBeforePlace(order: Order): MutableList<String> {
            val errorList = mutableListOf<String>()
            val user = userList.get(order.userName)!!
            val wallet = user.userWallet
            val nonPerformanceInventory = user.userNonPerfInventory

            if(!userList.containsKey(order.userName)){
                errorList.add("User doesn't exist.")
                return errorList
            }

            if(order.type == "BUY"){
                nonPerformanceInventory.assertInventoryWillNotOverflowOnAdding(order.quantity)

                val response = user!!.userWallet.moveMoneyFromFreeToLockedState(order.price*order.quantity)
                if (response != "SUCCESS"){
                    errorList.add(response)
                }
            }
            else if(order.type == "SELL"){
                wallet.assertWalletWillNotOverflowOnAdding(order.price * order.quantity)

                if(order.esopType == "PERFORMANCE"){
                    val response = user!!.userPerformanceInventory.moveESOPsFromFreeToLockedState(order.quantity)
                    if ( response != "SUCCESS" ){
                        errorList.add(response)
                    }
                }else if(order.esopType == "NON_PERFORMANCE"){
                    val response = user!!.userNonPerfInventory.moveESOPsFromFreeToLockedState(order.quantity)
                    if ( response != "SUCCESS" ){
                        errorList.add(response)
                    }
                }
            }
            return errorList
        }
    }

    fun check_username( search_value: String): Boolean
    {
        return !userList.contains(search_value)
    }

    fun check_phonenumber(usernumber_set: MutableSet<String>, search_value: String): Boolean
    {
        return usernumber_set.contains(search_value)
    }

    fun check_email(useremail_set: MutableSet<String>, search_value: String): Boolean
    {
        return useremail_set.contains(search_value)
    }


    fun validateUserDetails(userData: UserCreationDTO): List<String>
    {
        val errorList = mutableListOf<String>()

        if(check_username(userData.username!!)){
            errorList.add(errors["USERNAME_EXISTS"].toString())
        }
        if(check_email(emailList, userData.email!!)){
            errorList.add(errors["EMAIL_EXISTS"].toString())
        }
        if(check_phonenumber(phoneNumberList, userData.phoneNumber!!)){
            errorList.add(errors["PHONENUMBER_EXISTS"].toString())
        }

        return errorList
    }

    fun registerUser(userData: UserCreationDTO): Map<String,Any>
    {
//        val errors = validateUserDetails(userData)
//        if(errors.size > 0) {
//            return mapOf("error" to errors)
//        }
        val user = User(
            userData.firstName!!.trim(),
            userData.lastName!!.trim(),
            userData.phoneNumber!!,
            userData.email!!,
            userData.username!!
        );
        userList.put(userData.username!!,user)
        emailList.add(userData.email!!)
        phoneNumberList.add(userData.phoneNumber!!)
        return mapOf(
            "firstName" to user.firstName.toString(),
            "lastName" to user.lastName.toString(),
            "phoneNumber" to user.phoneNumber.toString(),
            "email" to user.email.toString(),
            "username" to user.username.toString()
        )
    }

    fun accountInformation(userName: String): Map<String, Any?>
    {
        var errorList = mutableListOf<String>()

        if ( !check_username(userName) ){
            errorList.add(errors["USER_DOES_NOT_EXISTS"].toString())
        }

        if( errorList.size > 0 ){
            return mapOf("error" to errorList)
        }
        val user = userList.get(userName)!!

        return mapOf(
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "phoneNumber" to user.phoneNumber,
            "email" to user.email,
            "wallet" to mapOf(
                "free" to user.userWallet.getFreeMoney(),
                "locked" to user.userWallet.getLockedMoney()
            ),
            "inventory" to arrayListOf<Any>(
                mapOf(
                    "type" to "PERFORMANCE",
                    "free" to user.userPerformanceInventory.getFreeInventory(),
                    "locked" to user.userPerformanceInventory.getLockedInventory()
                ),
                mapOf(
                    "type" to "NON_PERFORMANCE",
                    "free" to user.userNonPerfInventory.getFreeInventory(),
                    "locked" to user.userNonPerfInventory.getLockedInventory()
                )
            )
        )
    }


        fun addingInventory(inventoryData: AddInventoryDTO, userName: String): Map<String, Any>
        {
            var errorList = mutableListOf<String>()

            if ( inventoryData.esopType.toString().uppercase() != "NON_PERFORMANCE" && inventoryData.esopType.toString().uppercase() != "PERFORMANCE" ){
                errorList.add(errors["INVALID_TYPE"].toString())
            }
            else if ( !check_username(userName) ){
                errorList.add(errors["USER_DOES_NOT_EXISTS"].toString())
            }

            if( errorList.size > 0 ) {
                return mapOf("error" to errorList)
            }
            return mapOf("message" to userList.get(userName)!!.addToInventory(inventoryData))
        }

    fun addingMoney(walletData: AddWalletDTO, userName: String): Map<String, Any>
    {
        var errorList = mutableListOf<String>()

        if ( !check_username(userName) ){
            errorList.add(errors["USER_DOES_NOT_EXISTS"].toString())
        }

        if( errorList.size > 0 ) {
            return mapOf("error" to errorList)
        }

        return mapOf("message" to userList.get(userName)!!.addToWallet(walletData))
    }
}