package com.esop.service

import com.esop.constant.errors
import com.esop.dto.AddInventoryDTO
import com.esop.dto.AddWalletDTO
import com.esop.dto.UserCreationDTO
import com.esop.repository.UserRecords
import com.esop.schema.Order
import com.esop.schema.User
import jakarta.inject.Singleton

@Singleton
class UserService(private val userRecords: UserRecords) {

    fun orderCheckBeforePlace(order: Order): MutableList<String> {
        val errorList = mutableListOf<String>()

        if (!userRecords.checkIfUserExists(order.getUserName())) {
            errorList.add("User doesn't exist.")
            return errorList
        }

        val user = userRecords.getUser(order.getUserName())!!
        val wallet = user.userWallet
        val nonPerformanceInventory = user.userNonPerfInventory



        if (order.getType() == "BUY") {
            nonPerformanceInventory.assertInventoryWillNotOverflowOnAdding(order.getQuantity())

            val response = user.userWallet.moveMoneyFromFreeToLockedState(order.getPrice() * order.getQuantity())
            if (response != "SUCCESS") {
                errorList.add(response)
            }
        } else if (order.getType() == "SELL") {
            wallet.assertWalletWillNotOverflowOnAdding(order.getPrice() * order.getQuantity())

            if (order.esopType == "PERFORMANCE") {
                val response = user.userPerformanceInventory.moveESOPsFromFreeToLockedState(order.getQuantity())
                if (response != "SUCCESS") {
                    errorList.add(response)
                }
            } else if (order.esopType == "NON_PERFORMANCE") {
                val response = user.lockNonPerformanceInventory(order.getQuantity())
                if (response != "SUCCESS") {
                    errorList.add(response)
                }
            }
        }
        return errorList
    }


    fun registerUser(userData: UserCreationDTO): Map<String, String> {
        val user = User(
            userData.firstName!!.trim(),
            userData.lastName!!.trim(),
            userData.phoneNumber!!,
            userData.email!!,
            userData.username!!
        )
        userRecords.addUser(user)
        userRecords.addEmail(user.email)
        userRecords.addPhoneNumber(user.phoneNumber)
        return mapOf(
            "firstName" to user.firstName,
            "lastName" to user.lastName,
            "phoneNumber" to user.phoneNumber,
            "email" to user.email,
            "username" to user.username
        )
    }

    fun accountInformation(userName: String): Map<String, Any?> {
        val errorList = mutableListOf<String>()

        if (!userRecords.checkIfUserExists(userName)) {
            errorList.add(errors["USER_DOES_NOT_EXISTS"].toString())
        }

        if (errorList.size > 0) {
            return mapOf("error" to errorList)
        }
        val user = userRecords.getUser(userName)!!

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


    fun addingInventory(inventoryData: AddInventoryDTO, userName: String): Map<String, Any> {
        val errorList = mutableListOf<String>()

        if (inventoryData.esopType.toString().uppercase() != "NON_PERFORMANCE" && inventoryData.esopType.toString()
                .uppercase() != "PERFORMANCE"
        ) {
            errorList.add(errors["INVALID_TYPE"].toString())
        } else if (!userRecords.checkIfUserExists(userName)) {
            errorList.add(errors["USER_DOES_NOT_EXISTS"].toString())
        }

        if (errorList.size > 0) {
            return mapOf("error" to errorList)
        }
        return mapOf("message" to userRecords.getUser(userName)!!.addToInventory(inventoryData))
    }

    fun addingMoney(walletData: AddWalletDTO, userName: String): Map<String, Any> {
        val errorList = mutableListOf<String>()

        if (!userRecords.checkIfUserExists(userName)) {
            errorList.add(errors["USER_DOES_NOT_EXISTS"].toString())
        }

        if (errorList.size > 0) {
            return mapOf("error" to errorList)
        }

        return mapOf("message" to userRecords.getUser(userName)!!.addToWallet(walletData))
    }
}