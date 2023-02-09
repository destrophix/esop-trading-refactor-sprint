package com.esop.service

import com.esop.dto.AddInventoryDTO
import com.esop.dto.AddWalletDTO
import com.esop.dto.UserCreationDTO
import com.esop.repository.UserRecords
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserServiceTest {

    private lateinit var userService: UserService

    private lateinit var userRecords: UserRecords

    @BeforeEach
    fun setup() {
        userRecords = UserRecords()
        userService = UserService(userRecords)
    }


    @Test
    fun `should register a valid user`() {
        val user = UserCreationDTO("Sankar", "M", "+917550276216", "sankar@sahaj.ai", "sankar06")
        val expected = mapOf(
            "firstName" to "Sankar",
            "lastName" to "M",
            "phoneNumber" to "+917550276216",
            "email" to "sankar@sahaj.ai",
            "username" to "sankar06"
        )

        //Action
        val response = userService.registerUser(user)

        //Assert
        assertEquals(response, expected)
    }

    @Test
    fun `should add money to wallet`() {
        val user = UserCreationDTO("Sankar", "M", "+917550276216", "sankar@sahaj.ai", "Sankar")
        userService.registerUser(user)
        val walletDetails = AddWalletDTO(price = 1000)
        val expectedFreeMoney: Long = 1000
        val expectedUsername = "Sankar"

        userService.addingMoney(walletDetails, "Sankar")

        val actualFreeMoney = userRecords.getUser(expectedUsername)!!.getFreeAmountInWallet()
        assertEquals(expectedFreeMoney, actualFreeMoney)
    }

    @Test
    fun `should add ESOPS to inventory`() {
        val user = UserCreationDTO("Sankar", "M", "+917550276216", "sankar@sahaj.ai", "Sankar")
        userService.registerUser(user)
        val inventoryDetails = AddInventoryDTO(quantity = 1000L, esopType = "NON_PERFORMANCE")
        val expectedFreeInventory: Long = 1000
        val expectedUsername = "Sankar"

        userService.addingInventory(inventoryDetails, "Sankar")

        val actualFreeMoney = userRecords.getUser(expectedUsername)!!.getFreeESOPsInInventory("NON_PERFORMANCE")
        assertEquals(expectedFreeInventory, actualFreeMoney)
    }


}