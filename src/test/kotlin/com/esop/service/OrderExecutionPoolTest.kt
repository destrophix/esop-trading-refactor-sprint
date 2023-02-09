package com.esop.service

import com.esop.dto.AddInventoryDTO
import com.esop.repository.UserRecords
import com.esop.schema.Order
import com.esop.schema.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OrderExecutionPoolTest {

    private lateinit var userRecords: UserRecords
    private lateinit var orderExecutionPool: OrderExecutionPool

    @BeforeEach
    fun `It should create user`() {
        userRecords = UserRecords()
        orderExecutionPool = OrderExecutionPool()

        val buyer1 = User("Sankaranarayanan", "M", "7550276216", "sankaranarayananm@sahaj.ai", "sankar")
        val buyer2 = User("Aditya", "Tiwari", "", "aditya@sahaj.ai", "aditya")
        val seller1 = User("Kajal", "Pawar", "", "kajal@sahaj.ai", "kajal")
        val seller2 = User("Arun", "Murugan", "", "arun@sahaj.ai", "arun")

        userRecords.addUser(buyer1)
        userRecords.addUser(buyer2)
        userRecords.addUser(seller1)
        userRecords.addUser(seller2)
    }


    @Test
    fun `It should match BUY order for existing SELL order`() {
        //Arrange
        val kajal = userRecords.getUser("kajal")!!
        kajal.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrder = Order(10, "SELL", 10, kajal)
        kajal.lockNonPerformanceInventory(10)

        orderExecutionPool.add(sellOrder)

        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(100)
        val buyOrder = Order(10, "BUY", 10, sankar)
        sankar.lockAmount(100)

        //Act
        orderExecutionPool.add(buyOrder)

        //Assert
        assertEquals(40, kajal.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(10, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(98, kajal.getFreeAmountInWallet())
        assertEquals(0, sankar.getFreeAmountInWallet())
    }

    @Test
    fun `It should place 2 SELL orders followed by a BUY order where the BUY order is partial`() {
        //Arrange
        val kajal = userRecords.getUser("kajal")!!
        kajal.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrderByKajal = Order(10, "SELL", 10, kajal)
        kajal.lockNonPerformanceInventory(10)

        orderExecutionPool.add(sellOrderByKajal)

        val arun = userRecords.getUser("arun")!!
        arun.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrderByArun = Order(10, "SELL", 10, arun)
        arun.lockNonPerformanceInventory(10)

        orderExecutionPool.add(sellOrderByArun)

        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(250)
        val buyOrderBySankar = Order(25, "BUY", 10, sankar)
        sankar.lockAmount(250)

        //Act
        orderExecutionPool.add(buyOrderBySankar)

        //Assert
        assertEquals(40, kajal.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(40, arun.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(20, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(98, kajal.getFreeAmountInWallet())
        assertEquals(98, arun.getFreeAmountInWallet())
        assertEquals(50, sankar.getLockedAmountInWallet())

        assertEquals("PARTIAL", buyOrderBySankar.getOrderStatus())
        assertEquals(
            "COMPLETED", sellOrderByKajal.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByArun.getOrderStatus()
        )
    }

    @Test
    fun `It should place 2 SELL orders followed by a BUY order where the BUY order is complete`() {


        //Arrange
        val kajal = userRecords.getUser("kajal")!!
        kajal.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrderByKajal = Order(10, "SELL", 10, kajal)
        kajal.lockNonPerformanceInventory(10)

        orderExecutionPool.add(sellOrderByKajal)

        val arun = userRecords.getUser("arun")!!
        arun.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrderByArun = Order(10, "SELL", 10, arun)
        arun.lockNonPerformanceInventory(10)

        orderExecutionPool.add(sellOrderByArun)

        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(250)
        val buyOrderBySankar = Order(20, "BUY", 10, sankar)
        sankar.lockAmount(200)

        //Act
        orderExecutionPool.add(buyOrderBySankar)
        //Assert
        assertEquals(40, kajal.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(40, arun.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(20, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(98, kajal.getFreeAmountInWallet())
        assertEquals(98, arun.getFreeAmountInWallet())
        assertEquals(0, sankar.getLockedAmountInWallet())
        assertEquals(
            "COMPLETED",
            buyOrderBySankar.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByKajal.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByArun.getOrderStatus()
        )
    }

    @Test
    fun `It should place 1 SELL orders followed by a BUY order where the BUY order is complete`() {
        //Arrange
        val kajal = userRecords.getUser("kajal")!!
        kajal.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrderByKajal = Order(10, "SELL", 10, kajal)
        kajal.lockNonPerformanceInventory(10)
        orderExecutionPool.add(sellOrderByKajal)

        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(250)
        val buyOrderBySankar = Order(5, "BUY", 10, sankar)
        sankar.lockAmount(50)

        //Act
        orderExecutionPool.add(buyOrderBySankar)

        //Assert
        assertEquals(40, kajal.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(5, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(49, kajal.getFreeAmountInWallet())
        assertEquals(0, sankar.getLockedAmountInWallet())
        assertEquals(
            "COMPLETED",
            buyOrderBySankar.getOrderStatus()
        )
        assertEquals(
            "PARTIAL",
            sellOrderByKajal.getOrderStatus()
        )
    }

    @Test
    fun `It should place 1 SELL orders followed by a BUY order where the BUY order is partial`() {
        //Arrange
        val kajal = userRecords.getUser("kajal")!!
        kajal.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrderByKajal = Order(10, "SELL", 10, kajal)
        kajal.lockNonPerformanceInventory(10)

        orderExecutionPool.add(sellOrderByKajal)

        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(250)
        val buyOrderBySankar = Order(15, "BUY", 10, sankar)
        sankar.lockAmount(150)

        //Act
        orderExecutionPool.add(buyOrderBySankar)

        //Assert
        assertEquals(40, kajal.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(10, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(98, kajal.getFreeAmountInWallet())
        assertEquals(50, sankar.getLockedAmountInWallet())
        assertEquals(
            "PARTIAL", buyOrderBySankar.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByKajal.getOrderStatus()
        )
    }

    @Test
    fun `It should place 2 BUY orders followed by a SELL order where the SELL order is partial`() {
        //Arrange
        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(100)
        val buyOrderBySankar = Order(10, "BUY", 10, sankar)
        sankar.lockAmount(100)
        orderExecutionPool.add(buyOrderBySankar)

        val aditya = userRecords.getUser("aditya")!!
        aditya.addMoneyToWallet(100)
        val buyOrderByAditya = Order(10, "BUY", 10, aditya)
        sankar.lockAmount(100)
        orderExecutionPool.add(buyOrderByAditya)

        userRecords.getUser("kajal")!!.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrderByKajal = Order(25, "SELL", 10, userRecords.getUser("kajal")!!)
        userRecords.getUser("kajal")!!.lockNonPerformanceInventory(25)

        //Act
        orderExecutionPool.add(sellOrderByKajal)

        //Assert
        assertEquals(25, userRecords.getUser("kajal")!!.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(10, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(10, aditya.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(196, userRecords.getUser("kajal")!!.getFreeAmountInWallet())
        assertEquals(0, sankar.getFreeAmountInWallet())
        assertEquals(0, sankar.getFreeAmountInWallet())
        assertEquals("PARTIAL", sellOrderByKajal.getOrderStatus())
        assertEquals(
            "COMPLETED",
            buyOrderBySankar.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            buyOrderByAditya.getOrderStatus()
        )
    }

    @Test
    fun `It should place 2 BUY orders followed by a SELL order where the SELL order is complete`() {
        //Arrange
        val kajal = userRecords.getUser("kajal")!!
        kajal.addMoneyToWallet(100)
        val buyOrderByKajal = Order(10, "BUY", 10, kajal)
        kajal.lockAmount(10 * 10)
        orderExecutionPool.add(buyOrderByKajal)

        val arun = userRecords.getUser("arun")!!
        arun.addMoneyToWallet(100)
        val buyOrderByArun = Order(10, "BUY", 10, arun)
        arun.lockAmount(10 * 10)
        orderExecutionPool.add(buyOrderByArun)

        val sankar = userRecords.getUser("sankar")!!
        sankar.addToInventory(AddInventoryDTO(30, esopType = "NON_PERFORMANCE"))
        val sellOrderBySankar = Order(20, "SELL", 10, sankar)
        sankar.lockNonPerformanceInventory(20)

        //Act
        orderExecutionPool.add(sellOrderBySankar)

        //Assert
        assertEquals(10, kajal.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(0, kajal.getFreeAmountInWallet())

        assertEquals(10, arun.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(0, arun.getFreeAmountInWallet())

        assertEquals(10, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(98 + 98, sankar.getFreeAmountInWallet())

        assertEquals(
            "COMPLETED",
            sellOrderBySankar.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            buyOrderByKajal.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            buyOrderByArun.getOrderStatus()
        )
    }

    @Test
    fun `It should match BUY order for existing SELL order for PERFORMANCE esop type`() {
        //Arrange
        val kajal = userRecords.getUser("kajal")!!
        kajal.addToInventory(AddInventoryDTO(50, esopType = "PERFORMANCE"))
        val sellOrder = Order(10, "SELL", 10, kajal, esopType = "PERFORMANCE")
        kajal.lockPerformanceInventory(10)
        orderExecutionPool.add(sellOrder)

        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(100)
        val buyOrder = Order(10, "BUY", 10, sankar)
        sankar.lockAmount(100)

        //Act
        orderExecutionPool.add(buyOrder)

        //Assert
        assertEquals(40, kajal.getFreeESOPsInInventory("PERFORMANCE"))
        assertEquals(10, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(100, kajal.getFreeAmountInWallet())
        assertEquals(0, sankar.getFreeAmountInWallet())
    }

    @Test
    fun `It should match SELL order for PERFORMANCE for existing BUY order`() {
        //Arrange
        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(100)
        val buyOrder = Order(10, "BUY", 10, sankar)
        sankar.lockAmount(100)
        orderExecutionPool.add(buyOrder)

        val kajal = userRecords.getUser("kajal")!!
        kajal.addToInventory(AddInventoryDTO(50, esopType = "PERFORMANCE"))
        val sellOrder = Order(10, "SELL", 10, kajal, "PERFORMANCE")
        kajal.lockPerformanceInventory(10)

        //Act
        orderExecutionPool.add(sellOrder)

        //Assert
        assertEquals(40, kajal.getFreeESOPsInInventory("PERFORMANCE"))
        assertEquals(10, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(100, kajal.getFreeAmountInWallet())
        assertEquals(0, sankar.getFreeAmountInWallet())
    }

    @Test
    fun `It should match SELL order for existing BUY order where SELL order is complete`() {
        //Arrange
        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(200)
        val buyOrder = Order(20, "BUY", 10, sankar)
        sankar.lockAmount(200)
        orderExecutionPool.add(buyOrder)

        val kajal = userRecords.getUser("kajal")!!
        kajal.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrder = Order(10, "SELL", 10, kajal)
        kajal.lockNonPerformanceInventory(10)

        //Act
        orderExecutionPool.add(sellOrder)

        //Assert
        assertEquals(40, kajal.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(10, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(98, kajal.getFreeAmountInWallet())
        assertEquals(0, sankar.getFreeAmountInWallet())
    }

    @Test
    fun `It should place 2 SELL orders where one SELL order is of PERFORMANCE esop type and other is of NON-PERFORMANCE esop type followed by a BUY order where the BUY order is complete`() {
        //Arrange
        val kajal = userRecords.getUser("kajal")!!
        kajal.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrderByKajal = Order(10, "SELL", 10, kajal)
        kajal.lockNonPerformanceInventory(10)
        orderExecutionPool.add(sellOrderByKajal)

        val arun = userRecords.getUser("arun")!!
        arun.addToInventory(AddInventoryDTO(50, esopType = "PERFORMANCE"))
        val sellOrderByArun = Order(10, "SELL", 10, arun, "PERFORMANCE")
        arun.lockPerformanceInventory(10)
        orderExecutionPool.add(sellOrderByArun)

        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(250)
        val buyOrderBySankar = Order(20, "BUY", 10, sankar)
        sankar.lockAmount(200)

        //Act
        orderExecutionPool.add(buyOrderBySankar)

        //Assert
        assertEquals(40, kajal.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(40, arun.getFreeESOPsInInventory("PERFORMANCE"))
        assertEquals(20, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(98, kajal.getFreeAmountInWallet())
        assertEquals(100, arun.getFreeAmountInWallet())
        assertEquals(0, sankar.getLockedAmountInWallet())
        assertEquals(
            "COMPLETED",
            buyOrderBySankar.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByKajal.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByArun.getOrderStatus()
        )
    }

    @Test
    fun `It should place 2 SELL orders of PERFORMANCE esop type followed by a BUY order where the BUY order is complete`() {
        //Arrange
        val kajal = userRecords.getUser("kajal")!!
        kajal.addToInventory(AddInventoryDTO(50, esopType = "PERFORMANCE"))
        val sellOrderByKajal = Order(10, "SELL", 10, kajal, "PERFORMANCE")
        kajal.lockPerformanceInventory(10)
        orderExecutionPool.add(sellOrderByKajal)

        val arun = userRecords.getUser("arun")!!
        arun.addToInventory(AddInventoryDTO(50, esopType = "PERFORMANCE"))
        val sellOrderByArun = Order(10, "SELL", 10, arun, "PERFORMANCE")
        arun.lockPerformanceInventory(10)
        orderExecutionPool.add(sellOrderByArun)

        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(250)
        val buyOrderBySankar = Order(20, "BUY", 10, sankar)
        sankar.lockAmount(200)

        //Act
        orderExecutionPool.add(buyOrderBySankar)

        //Assert
        assertEquals(40, kajal.getFreeESOPsInInventory("PERFORMANCE"))
        assertEquals(40, arun.getFreeESOPsInInventory("PERFORMANCE"))
        assertEquals(20, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(100, kajal.getFreeAmountInWallet())
        assertEquals(100, arun.getFreeAmountInWallet())
        assertEquals(0, sankar.getLockedAmountInWallet())
        assertEquals(
            "COMPLETED",
            buyOrderBySankar.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByKajal.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByArun.getOrderStatus()
        )
    }

    @Test
    fun `It should place 2 SELL orders of PERFORMANCE esop type followed by a BUY order where higher timestamp order placed first`() {
        //Arrange

        val arun = userRecords.getUser("arun")!!
        arun.addToInventory(AddInventoryDTO(50, esopType = "PERFORMANCE"))
        val sellOrderByArun = Order(10, "SELL", 10, arun, "PERFORMANCE")
        arun.lockPerformanceInventory(10)
        orderExecutionPool.add(sellOrderByArun)

        val kajal = userRecords.getUser("kajal")!!
        kajal.addToInventory(AddInventoryDTO(50, esopType = "PERFORMANCE"))
        val sellOrderByKajal = Order(10, "SELL", 10, kajal, "PERFORMANCE")
        kajal.lockPerformanceInventory(10)
        orderExecutionPool.add(sellOrderByKajal)

        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(250)
        val buyOrderBySankar = Order(20, "BUY", 10, sankar)
        sankar.lockAmount(200)

        //Act
        orderExecutionPool.add(buyOrderBySankar)

        //Assert
        assertEquals(40, kajal.getFreeESOPsInInventory("PERFORMANCE"))
        assertEquals(40, arun.getFreeESOPsInInventory("PERFORMANCE"))
        assertEquals(20, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(100, kajal.getFreeAmountInWallet())
        assertEquals(100, arun.getFreeAmountInWallet())
        assertEquals(0, sankar.getLockedAmountInWallet())
        assertEquals(
            "COMPLETED",
            buyOrderBySankar.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByKajal.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByArun.getOrderStatus()
        )
    }

    @Test
    fun `It should place 2 SELL orders of NON-PERFORMANCE esop type followed by a BUY order where higher timestamp order placed first`() {
        //Arrange
        val arun = userRecords.getUser("arun")!!
        arun.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrderByArun = Order(10, "SELL", 10, arun)
        arun.lockNonPerformanceInventory(10)
        orderExecutionPool.add(sellOrderByArun)

        val kajal = userRecords.getUser("kajal")!!
        kajal.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrderByKajal = Order(10, "SELL", 10, kajal)
        kajal.lockNonPerformanceInventory(10)
        orderExecutionPool.add(sellOrderByKajal)

        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(250)
        val buyOrderBySankar = Order(20, "BUY", 10, sankar)
        sankar.lockAmount(200)

        //Act
        orderExecutionPool.add(buyOrderBySankar)

        //Assert
        assertEquals(40, kajal.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(40, arun.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(20, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(98, kajal.getFreeAmountInWallet())
        assertEquals(98, arun.getFreeAmountInWallet())
        assertEquals(0, sankar.getLockedAmountInWallet())
        assertEquals(
            "COMPLETED",
            buyOrderBySankar.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByKajal.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByArun.getOrderStatus()
        )
    }

    @Test
    fun `It should place 2 SELL orders of NON-PERFORMANCE esop type followed by a BUY order where SELL order price is different`() {
        //Arrange
        val kajal = userRecords.getUser("kajal")!!
        kajal.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrderByKajal = Order(10, "SELL", 20, kajal)
        kajal.lockNonPerformanceInventory(10)
        orderExecutionPool.add(sellOrderByKajal)

        val arun = userRecords.getUser("arun")!!
        arun.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrderByArun = Order(10, "SELL", 10, arun)
        arun.lockNonPerformanceInventory(10)
        orderExecutionPool.add(sellOrderByArun)


        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(400)
        val buyOrderBySankar = Order(20, "BUY", 20, sankar)
        sankar.lockAmount(400)

        //Act
        orderExecutionPool.add(buyOrderBySankar)

        //Assert
        assertEquals(40, kajal.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(40, arun.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(20, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(196, kajal.getFreeAmountInWallet())
        assertEquals(98, arun.getFreeAmountInWallet())
        assertEquals(0, sankar.getLockedAmountInWallet())
        assertEquals(
            "COMPLETED",
            buyOrderBySankar.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByKajal.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByArun.getOrderStatus()
        )
    }

    @Test
    fun `It should place 2 SELL orders of NON-PERFORMANCE esop type followed by a BUY order where lower SELL order price is placed first`() {
        //Arrange
        val kajal = userRecords.getUser("kajal")!!
        kajal.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrderByKajal = Order(10, "SELL", 20, kajal)
        kajal.lockNonPerformanceInventory(10)

        val arun = userRecords.getUser("arun")!!
        arun.addToInventory(AddInventoryDTO(50, esopType = "NON_PERFORMANCE"))
        val sellOrderByArun = Order(10, "SELL", 10, arun)
        arun.lockNonPerformanceInventory(10)

        orderExecutionPool.add(sellOrderByArun)
        orderExecutionPool.add(sellOrderByKajal)

        val sankar = userRecords.getUser("sankar")!!
        sankar.addMoneyToWallet(400)
        val buyOrderBySankar = Order(20, "BUY", 20, sankar)
        sankar.lockAmount(400)

        //Act
        orderExecutionPool.add(buyOrderBySankar)

        //Assert
        assertEquals(40, kajal.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(40, arun.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(20, sankar.getFreeESOPsInInventory("NON_PERFORMANCE"))
        assertEquals(196, kajal.getFreeAmountInWallet())
        assertEquals(98, arun.getFreeAmountInWallet())
        assertEquals(0, sankar.getLockedAmountInWallet())
        assertEquals(
            "COMPLETED",
            buyOrderBySankar.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByKajal.getOrderStatus()
        )
        assertEquals(
            "COMPLETED",
            sellOrderByArun.getOrderStatus()
        )
    }
}
