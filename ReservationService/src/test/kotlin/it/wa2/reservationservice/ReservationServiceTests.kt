package it.wa2.reservationservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import feign.FeignException
import io.mockk.*
import it.wa2.reservationservice.advice.*
import it.wa2.reservationservice.controllers.AvailabilityNotFound
import it.wa2.reservationservice.controllers.VehicleNotFound
import it.wa2.reservationservice.dtos.*
import it.wa2.reservationservice.entities.*
import it.wa2.reservationservice.repositories.AvailabilityRepository
import it.wa2.reservationservice.repositories.ReservationRepository
import it.wa2.reservationservice.repositories.StatusRepository
import it.wa2.reservationservice.repositories.VehicleRepository
import it.wa2.reservationservice.services.PaymentClient
import it.wa2.reservationservice.services.ReservationServiceImpl
import it.wa2.reservationservice.services.UserClient
import net.bytebuddy.matcher.ElementMatchers.any
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.support.Acknowledgment
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class ReservationServiceImplTest : IntegrationTest() {

    @MockkBean
    private lateinit var reservationRepository: ReservationRepository

    @MockkBean
    private lateinit var vehicleRepository: VehicleRepository

    @MockkBean
    private lateinit var statusRepository: StatusRepository

    @MockkBean
    private lateinit var userClient: UserClient

    @MockkBean
    private lateinit var availabilityRepository: AvailabilityRepository

/*    @MockkBean
    private lateinit var objectMapper: ObjectMapper*/

    @MockkBean
    private lateinit var paymentClient: PaymentClient

    @Autowired
    private lateinit var reservationService: ReservationServiceImpl

    private val acknowledgment = mockk<Acknowledgment>(relaxed = true)

    private lateinit var testReservation: Reservation
    private lateinit var testReservationDTO: ReservationDTO
    private lateinit var testVehicle: Vehicle
    private lateinit var testStatus: Status
    private lateinit var testCarModel: CarModel
    private lateinit var testAvailability: Availability

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        // Setup test data
        testCarModel = CarModel(
            id = -1,
            brand = "Toyota",
            model = "Corolla",
            modelYear = 2020,
            segment = "Sedan",
            doorsNo = 5,
            seatingCapacity = 5,
            luggageCapacity = 500f,
            manufacturer = "Toyota",
            costPerDay = 10.0,
            motorDisplacement = 1800f,
            airConditioning = true,
            category = Category(), // Assuming Category is another entity, set it up as needed
            refEngine = Engine(type = "Diesel"), // Assuming Engine is another entity, set it up as needed
            refTransmission = Transmission(type = ""), // Assuming Transmission is another entity, set it up as needed
            refDrivetrain = Drivetrain(type = ""), // Assuming Drivetrain is another entity, set it up as needed
            safetyFeatures = mutableSetOf(), // Assuming SafetyFeatures is another entity, set it up as needed
            infotainments = mutableSetOf() // Assuming Infotainment is another entity, set it up as needed
        )

        testAvailability = Availability(
            id = 1L,
            type = "available"
        )

        testVehicle = Vehicle(
            id = 1L,
            refCarModel = testCarModel,
            refAvailability = testAvailability,
            licencePlate = "ABC123",
            vin = "VIN123456",
            kilometers = 10000f
        )

        testStatus = Status(
            reservation = mutableListOf(),
            status = "PENDING"
        ).apply { id = 1L }

        testReservation = Reservation(
            customerId = 1L,
            employeeId = 1L,
            vehicle = testVehicle,
            status = testStatus,
            reservationDate = Date(),
            startDate = Date(System.currentTimeMillis() + 86400000), // Tomorrow
            endDate = Date(System.currentTimeMillis() + 172800000), // Day after tomorrow
            paymentAmount = 100.0
        ).apply {
            id = 1L
            version = 1L
        }

        testReservationDTO = ReservationDTO(
            id = 1L,
            customerId = 1L,
            employeeId = 1L,
            vehicleId = 1L,
            status = StatusDTO(1L, "PENDING"),
            reservationDate = Date(),
            startDate = Date(System.currentTimeMillis() + 86400000),
            endDate = Date(System.currentTimeMillis() + 172800000),
            paymentAmount = 100.0,
            version = 1L
        )
    }

    @Test
    fun `getReservations should return filtered reservations`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val reservations = listOf(testReservation)
        val page = PageImpl(reservations, pageable, 1)

        every {
            reservationRepository.findWithFilters(any(), any(), any(), any(), any(), any(), any(), any())
        } returns page

        // When
        val result = reservationService.getReservations(
            pageable = pageable,
            customerId = 1L,
            employeeId = null,
            carModelId = null,
            status = null,
            reservationDate = null,
            startDate = null,
            endDate = null
        )

        // Then
        assertEquals(1, result.totalElements)
        assertEquals(testReservationDTO.id, result.content[0].id)
        verify { reservationRepository.findWithFilters(pageable, 1L, null, null, null, null, null, null) }
    }

    @Test
    fun `getReservationById should return reservation when found`() {
        // Given
        every { reservationRepository.findByIdOrNull(1L) } returns testReservation

        // When
        val result = reservationService.getReservationById(1L)

        // Then
        assertEquals(testReservationDTO.id, result.id)
        assertEquals(testReservationDTO.customerId, result.customerId)
        verify { reservationRepository.findByIdOrNull(1L) }
    }

    @Test
    fun `getReservationById should throw ReservationNotFound when not found`() {
        // Given
        every { reservationRepository.findByIdOrNull(1L) } returns null

        // When & Then
        assertThrows<ReservationNotFound> {
            reservationService.getReservationById(1L)
        }
    }

    @Test
    fun `addReservation should create new reservation successfully`() {
        // Given
        every { userClient.getUserById(1L) } just Runs
        every { reservationRepository.findByIdOrNull(1L) } returns null
        every { statusRepository.findFirstByStatus("PENDING") } returns testStatus
        every { vehicleRepository.findByIdOrNull(1L) } returns testVehicle
        every { reservationRepository.getReservationByVehicleAndStatus(1L, "PENDING") } returns null
        every { availabilityRepository.findByType("rented") } returns testAvailability
        every { vehicleRepository.save(any()) } returns testVehicle
        every { reservationRepository.save(any()) } returns testReservation

        // When
        val result = reservationService.addReservation(testReservationDTO)

        // Then
        assertNotNull(result)
        assertEquals(testReservationDTO.customerId, result.customerId)
        verify { reservationRepository.save(any()) }
        verify { vehicleRepository.save(testVehicle) }
    }

    @Test
    fun `addReservation should throw ReservationDuplicate when reservation already exists`() {
        // Given
        every { reservationRepository.findByIdOrNull(1L) } returns testReservation
        every {userClient.getUserById(testReservation.customerId)} returns Unit

        // When & Then
        assertThrows<ReservationDuplicate> {
            reservationService.addReservation(testReservationDTO)
        }
    }

    @Test
    fun `addReservation should throw UserNotFound when user doesn't exist`() {
        // Given
        every { userClient.getUserById(1L) } throws mockk<FeignException.NotFound>()

        // When & Then
        assertThrows<UserNotFound> {
            reservationService.addReservation(testReservationDTO)
        }
    }

    @Test
    fun `addReservation should throw VehicleNotFound when vehicle doesn't exist`() {
        // Given
        every { userClient.getUserById(1L) } just Runs
        every { reservationRepository.findByIdOrNull(1L) } returns null
        every { statusRepository.findFirstByStatus("PENDING") } returns testStatus
        every { vehicleRepository.findByIdOrNull(1L) } returns null

        // When & Then
        assertThrows<VehicleNotFound> {
            reservationService.addReservation(testReservationDTO)
        }
    }

    @Test
    fun `updateReservationById should update reservation successfully`() {
        // Given
        // every { reservationRepository.findById(any()).orElseThrow {any()} } returns Optional.of(testReservation).orElseThrow()
        every { reservationRepository.findByIdOrNull(1L) } returns testReservation
        every { vehicleRepository.findByIdOrNull(1L) } returns testVehicle
        every { userClient.getUserById(1L) } returns Unit
        every { statusRepository.findFirstByStatus("PENDING") } returns testStatus
        every { availabilityRepository.findByType("rented") } returns testAvailability
        every { vehicleRepository.save(any()) } returns testVehicle
        every { reservationRepository.save(any()) } returns testReservation

        // When
        val result = reservationService.updateReservationById(1L, testReservationDTO)

        // Then
        assertNotNull(result)
        verify { reservationRepository.save(testReservation) }
    }

    @Test
    fun `updateReservationById should throw OptimisticLockingFailureException for version mismatch`() {
        // Given
        val outdatedReservation = Reservation(
            customerId = 1L,
            employeeId = 1L,
            vehicle = testVehicle,
            status = testStatus,
            reservationDate = Date(),
            startDate = Date(System.currentTimeMillis() + 86400000), // Tomorrow
            endDate = Date(System.currentTimeMillis() + 172800000), // Day after tomorrow
            paymentAmount = 100.0
        ).apply {
            id = 1L
            version = 2L
        }
        //every { reservationRepository.findById(1L).orElseThrow{any<ReservationNotFound>()} } returns Optional.of(outdatedReservation).orElseThrow{ ReservationNotFound("") }
        every { reservationRepository.findByIdOrNull(1L) } returns outdatedReservation
        every { userClient.getUserById(1L) } returns Unit

        // When & Then
        assertThrows<OptimisticLockingFailureException> {
            reservationService.updateReservationById(1L, testReservationDTO)
        }
    }

    @Test
    fun `deleteReservationById should delete reservation successfully`() {
        // Given
        every { reservationRepository.findByIdOrNull(1L) } returns testReservation
        every { availabilityRepository.findByType("available") } returns testAvailability
        every { vehicleRepository.save(any()) } returns testVehicle
        every { reservationRepository.deleteById(1L) } just Runs

        // When
        reservationService.deleteReservationById(1L)

        // Then
        verify { reservationRepository.deleteById(1L) }
        verify { vehicleRepository.save(testVehicle) }
    }

    @Test
    fun `deleteReservationById should throw ReservationNotFound when reservation doesn't exist`() {
        // Given
        every { reservationRepository.findByIdOrNull(1L) } returns null

        // When & Then
        assertThrows<ReservationNotFound> {
            reservationService.deleteReservationById(1L)
        }
    }

    @Test
    fun `getReservationsByUserId should return user reservations`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val reservations = listOf(testReservation)
        val page = PageImpl(reservations, pageable, 1)

        every { userClient.getUserById(1L) } just Runs
        every { reservationRepository.getReservationsByCustomerId(1L, pageable) } returns page

        // When
        val result = reservationService.getReservationsByUserId(pageable, 1L)

        // Then
        assertEquals(1, result.totalElements)
        verify { userClient.getUserById(1L) }
        verify { reservationRepository.getReservationsByCustomerId(1L, pageable) }
    }

    @Test
    fun `getReservationsByUserId should throw ReservationNotFound when no reservations found`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val emptyPage = PageImpl<Reservation>(emptyList(), pageable, 0)

        every { userClient.getUserById(1L) } just Runs
        every { reservationRepository.getReservationsByCustomerId(1L, pageable) } returns emptyPage

        // When & Then
        assertThrows<ReservationNotFound> {
            reservationService.getReservationsByUserId(pageable, 1L)
        }
    }

    @Test
    fun `payReservation should create payment order successfully`() {
        // Given
        val approvedStatus = Status(mutableListOf(), "APPROVED").apply { id = 2L }
        val approvedReservation = testReservation.apply { status = approvedStatus }

        every { reservationRepository.findByIdOrNull(1L) } returns approvedReservation
        every { paymentClient.createPayPalOrder(any()) } returns "payment-url"

        // When
        val result = reservationService.payReservation(1L)

        // Then
        assertEquals("payment-url", result)
        verify { paymentClient.createPayPalOrder(any()) }
    }

    @Test
    fun `payReservation should throw ReservationNotAccepted when reservation not approved`() {
        // Given
        every { reservationRepository.findByIdOrNull(1L) } returns testReservation

        // When & Then
        assertThrows<ReservationNotAccepted> {
            reservationService.payReservation(1L)
        }
    }
/*
    @Test
    fun `handlePaymentStatusChange should update reservation status on COMPLETED payment`() {
        // Given
        val eventJson = """{"paypalToken": "token123"}"""
        val paypalEvent = PaypalOutboxEvent("token123")
        val paymentResponse = PaymentOrderResponseDTO(1L, "")
        val payedStatus = Status(mutableListOf(), "PAYED").apply { id = 3L }

        //every { objectMapper.readValue(eventJson, PaypalOutboxEvent::class.java) } returns paypalEvent
        every { paymentClient.getPaypalOrder("token123") } returns paymentResponse
        every { reservationRepository.findByIdOrNull(1L) } returns testReservation
        every { statusRepository.findFirstByStatus("PAYED") } returns payedStatus
        every { reservationRepository.findById(1L) } returns Optional.of(testReservation)
        every { vehicleRepository.findByIdOrNull(1L) } returns testVehicle
        every { userClient.getUserById(1L) } just Runs
        every { availabilityRepository.findByType("rented") } returns testAvailability
        every { vehicleRepository.save(any()) } returns testVehicle
        every { reservationRepository.save(any()) } returns testReservation

        // When
        reservationService.handlePaymentStatusChange(eventJson, "key", acknowledgment)

        // Then
        verify { acknowledgment.acknowledge() }
        verify { reservationRepository.save(any()) }
    }

    @Test
    fun `handlePaymentStatusChange should update reservation status on CANCELLED payment`() {
        // Given
        val eventJson = """{"paypalToken": "token123"}"""
        val paypalEvent = PaypalOutboxEvent("token123")
        val paymentResponse = PaymentOrderResponseDTO(1L, "")
        val refusedStatus = Status(mutableListOf(), "PAYMENT_REFUSED").apply { id = 4L }

        every { objectMapper.readValue(eventJson, PaypalOutboxEvent::class.java) } returns paypalEvent
        every { paymentClient.getPaypalOrder("token123") } returns paymentResponse
        every { reservationRepository.findByIdOrNull(1L) } returns testReservation
        every { statusRepository.findFirstByStatus("PAYMENT_REFUSED") } returns refusedStatus
        every { reservationRepository.findById(1L) } returns Optional.of(testReservation)
        every { vehicleRepository.findByIdOrNull(1L) } returns testVehicle
        every { userClient.getUserById(1L) } just Runs
        every { availabilityRepository.findByType("available") } returns testAvailability
        every { vehicleRepository.save(any()) } returns testVehicle
        every { reservationRepository.save(any()) } returns testReservation

        // When
        reservationService.handlePaymentStatusChange(eventJson, "key", acknowledgment)

        // Then
        verify { acknowledgment.acknowledge() }
        verify { reservationRepository.save(any()) }
    }

    @Test
    fun `handlePaymentStatusChange should nack on exception`() {
        // Given
        val eventJson = """{"paypalToken": "token123"}"""

        every { objectMapper.readValue(eventJson, PaypalOutboxEvent::class.java) } throws RuntimeException("Parse error")

        // When
        reservationService.handlePaymentStatusChange(eventJson, "key", acknowledgment)

        // Then
        verify { acknowledgment.nack(Duration.ofSeconds(10)) }
        verify(exactly = 0) { acknowledgment.acknowledge() }
    }*/

    @Test
    fun `getFullyBookedDates should return intersection of all vehicle bookings`() {
        // Given
        val carModelId = 1L
        val now = Date()
        val tomorrow = Date(now.time + 86400000)
        val dayAfter = Date(now.time + 172800000)

        val rawData = listOf(
            arrayOf<Any>(1L, now, tomorrow), // Vehicle 1: today-tomorrow
            arrayOf<Any>(2L, now, tomorrow), // Vehicle 2: today-tomorrow
            arrayOf<Any>(1L, tomorrow, dayAfter), // Vehicle 1: tomorrow-dayAfter
        )

        val vehicles = listOf(
            testVehicle,
            Vehicle(
                id = 2L,
                refCarModel = testCarModel,
                licencePlate = "XYZ789",
                vin = "VIN654321",
                kilometers = 20000f,
                pendingCleaning = false,
                pendingMaintenance = false,
                refAvailability = Availability(1L, "available")
            )
        )

        every { reservationRepository.findReservationIntervalsByCarModel(carModelId) } returns rawData
        every { vehicleRepository.findAllByRefCarModelId(carModelId) } returns vehicles

        // When
        val result = reservationService.getFullyBookedDates(carModelId)

        // Then
        val expectedDate = now.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        assertTrue(result.contains(expectedDate))
    }

    @Test
    fun `getFullyBookedDates should return empty set when no vehicles or reservations`() {
        // Given
        val carModelId = 1L

        every { reservationRepository.findReservationIntervalsByCarModel(carModelId) } returns emptyList()
        every { vehicleRepository.findAllByRefCarModelId(carModelId) } returns emptyList()

        // When
        val result = reservationService.getFullyBookedDates(carModelId)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `checkUser should not throw when user exists`() {
        // Given
        every { userClient.getUserById(1L) } just Runs

        // When & Then (no exception should be thrown)
        reservationService.checkUser(1L)
        verify { userClient.getUserById(1L) }
    }

    @Test
    fun `checkUser should throw UserNotFound when user doesn't exist`() {
        // Given
        every { userClient.getUserById(1L) } throws mockk<FeignException.NotFound>()

        // When & Then
        assertThrows<UserNotFound> {
            reservationService.checkUser(1L)
        }
    }
}
/*

// Exception classes
class ReservationNotFound(message: String) : RuntimeException(message)
class ReservationDuplicate(message: String) : RuntimeException(message)
class StatusNotFound(message: String) : RuntimeException(message)
class UserNotFound(message: String) : RuntimeException(message)
class ReservationNotAccepted(message: String) : RuntimeException(message)
class UnableToProcessEvent(message: String) : RuntimeException(message)*/
