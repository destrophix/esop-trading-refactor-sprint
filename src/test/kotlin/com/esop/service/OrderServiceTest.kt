package com.esop.service

import com.esop.InventoryLimitExceededException
import com.esop.WalletLimitExceededException
import com.esop.constant.MAX_INVENTORY_CAPACITY
import com.esop.constant.MAX_WALLET_CAPACITY
import com.esop.dto.AddInventoryDTO
import com.esop.dto.AddWalletDTO
import com.esop.dto.CreateOrderDTO
import com.esop.dto.UserCreationDTO
import com.esop.exceptions.InsufficientFreeAmountInWalletException
import com.esop.exceptions.InsufficientFreeESOPsInInventoryException
import com.esop.repository.UserRecords
import com.esop.schema.Order
import com.esop.schema.User
import org.junit.jupiter.api.*

class OrderServiceTest {

    lateinit var orderService: OrderService
    lateinit var userRecords: UserRecords
    lateinit var orderExecutionPool: OrderExecutionPool

    @BeforeEach
    fun `setup`() {
        userRecords = UserRecords()
        orderExecutionPool = OrderExecutionPool()
        orderService = OrderService(userRecords, orderExecutionPool)

    }

    @Test
    fun `should check if exception is not being thrown there is sufficient free amount is in wallet to place BUY order`() {
        val sankar = User("Sankaranarayanan", "M", "7550276216", "sankaranarayananm@sahaj.ai", "sankar")
        sankar.addMoneyToWallet(100)
        val order = CreateOrderDTO(
            quantity = 10, type = "BUY", price = 10
        )


        assertDoesNotThrow {
            orderService.placeOrder(order, sankar)
        }
    }

    @Test
    fun `it should throw exception if there is insufficient free amount in wallet to place BUY order`() {
        val sankar = User("Sankar", "M", "+917550276216", "sankar@sahaj.ai", "sankar06")
        val order = CreateOrderDTO(
            quantity = 10, type = "BUY", price = 10
        )
        sankar.addMoneyToWallet(99)

        assertThrows<InsufficientFreeAmountInWalletException> { orderService.placeOrder(order, sankar) }
    }

    @Test
    fun `it should throw exception when the buyer inventory overflows`() {
        val sankar = User("Sankar", "M", "+917550276216", "sankar@sahaj.ai", "sankar06")
        sankar.addMoneyToWallet(100)
        val order = CreateOrderDTO(
            quantity = 10, type = "BUY", price = 10
        )

        sankar.addToInventory(AddInventoryDTO(MAX_INVENTORY_CAPACITY))

        assertThrows<InventoryLimitExceededException> {
            orderService.placeOrder(order, sankar)
        }
    }

    @Test
    fun `it should not throw exception when there is sufficient free Non Performance ESOPs in the Inventory`() {
        val sankar = User("Sankar", "M", "+917550276216", "sankar@sahaj.ai", "sankar06")
        sankar.addToInventory(AddInventoryDTO(10))
        val order = CreateOrderDTO(
            quantity = 10, type = "SELL", price = 10
        )

        assertDoesNotThrow {
            orderService.placeOrder(order, sankar)
        }
    }

    @Test
    fun `it should throw exception when there is insufficient free Non Performance ESOPs in Inventory`() {
        val sankar = User("Sankar", "M", "+917550276216", "sankar@sahaj.ai", "sankar06")
        sankar.addToInventory(AddInventoryDTO(10))
        val order = CreateOrderDTO(
            quantity = 29, type = "SELL", price = 10
        )

        assertThrows<InsufficientFreeESOPsInInventoryException> {
            orderService.placeOrder(order, sankar)
        }
    }

    @Test
    fun `it should throw exception when the seller wallet overflows`() {
        val sankar = User("Sankar", "M", "+917550276216", "sankar@sahaj.ai", "sankar06")
        sankar.addToInventory(AddInventoryDTO(10))
        val order = CreateOrderDTO(
            quantity = 10, type = "SELL", price = 10
        )
        sankar.addMoneyToWallet(MAX_WALLET_CAPACITY)

        assertThrows<WalletLimitExceededException> {
            orderService.placeOrder(order, sankar)
        }
    }

    @Test
    fun `it should not throw exception when there is sufficient free Performance ESOPs in the Inventory`() {
        val sankar = User("Sankar", "M", "+917550276216", "sankar@sahaj.ai", "sankar06")

        sankar.addToInventory(AddInventoryDTO(10, "PERFORMANCE"))
        val order = CreateOrderDTO(
            quantity = 10, type = "SELL", price = 10, esopType = "PERFORMANCE"
        )

        assertDoesNotThrow {
            orderService.placeOrder(order, sankar)
        }

    }

    @Test
    fun `it should throw exception when there is insufficient free Performance ESOPs in Inventory`() {
        val sankar = User("Sankar", "M", "+917550276216", "sankar@sahaj.ai", "sankar06")
        val order = CreateOrderDTO(
            quantity = 29, type = "SELL", price = 10, esopType = "PERFORMANCE"
        )

        assertThrows<InsufficientFreeESOPsInInventoryException> {
            orderService.placeOrder(order, sankar)
        }
    }
}