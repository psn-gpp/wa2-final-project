package it.wa2.reservationservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import feign.FeignException
import it.wa2.reservationservice.ManageToken
import it.wa2.reservationservice.TokenState
import it.wa2.reservationservice.advice.*
import it.wa2.reservationservice.controllers.AvailabilityNotFound
import it.wa2.reservationservice.controllers.VehicleNotFound
import it.wa2.reservationservice.dtos.PaymentStatus
import it.wa2.reservationservice.dtos.PaymentOrderRequestDTO
import it.wa2.reservationservice.dtos.PaymentOrderResponseDTO
import it.wa2.reservationservice.dtos.ReservationDTO
import it.wa2.reservationservice.entities.*
import it.wa2.reservationservice.repositories.AvailabilityRepository
import it.wa2.reservationservice.repositories.ReservationRepository
import it.wa2.reservationservice.repositories.StatusRepository
import it.wa2.reservationservice.repositories.VehicleRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.validation.annotation.Validated
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@Service
@Primary
@Validated
@Transactional
class ReservationServiceImpl(
    private val reservationRepository: ReservationRepository,
    private val vehicleRepository: VehicleRepository,
    private val statusRepository: StatusRepository,
    //private val userClient: UserClient,
    private val availabilityRepository: AvailabilityRepository,
    private val objectMapper: ObjectMapper,
    //private val paymentClient: PaymentClient,
    //private val restTemplate: RestTemplate
    private val manageToken: ManageToken
) : ReservationService {

    private val logger = LoggerFactory.getLogger(ReservationServiceImpl::class.java)


    fun getUserByIdWithAuth(userId:Long){
        //val token = getAccessToken()
        val token=manageToken.getCachedToken()
        val headers = HttpHeaders().apply {
            setBearerAuth(token)
        }
        val entity = HttpEntity(null, headers)
        RestTemplate().exchange(
            "http://localhost:8081/api/v1/users/${userId}",
            HttpMethod.GET,
            entity,
            String::class.java
        )
    }

    fun getPaypalOrderWithAuth(paypalToken: String): PaymentOrderResponseDTO {
        //val token = getAccessToken()
        val token=manageToken.getCachedToken()
        val headers = HttpHeaders().apply {
            setBearerAuth(token)
        }
        val entity = HttpEntity(null, headers)
        val response = RestTemplate().exchange(
            "http://localhost:8082/api/v1/orders/order/$paypalToken",
            HttpMethod.GET,
            entity,
            PaymentOrderResponseDTO::class.java
        )
        //TODO sistemare eccezione
        return response.body ?: throw Exception("Response body empty")
    }

    fun createPaypalOrder(paymentOrderRequestDTO: PaymentOrderRequestDTO): String {
        //val token = getAccessToken()
        val token=manageToken.getCachedToken()
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON)
            setBearerAuth(token)
        }
        val entity = HttpEntity(paymentOrderRequestDTO, headers)
        val response= RestTemplate().exchange(
            "http://localhost:8082/api/v1/orders/create",
            HttpMethod.POST,
            entity,
            String::class.java
        )

        //Todo sistemare eccezione
        return response.body ?: throw Exception("Response body empty")
    }


    override fun getReservations(
        pageable: Pageable,
        customerId: Long?,
        employeeId: Long?,
        carModelId: Long?,
        status: String?,
        reservationDate: Date?,
        startDate: Date?,
        endDate: Date?
    ): Page<ReservationDTO> {
        return reservationRepository.findWithFilters(
            pageable,
            customerId,
            employeeId,
            carModelId,
            status,
            reservationDate,
            startDate,
            endDate,
        ).map { it.toDTO() }
    }

    override fun getReservationById(reservationId: Long): ReservationDTO {
        return reservationRepository.findByIdOrNull(reservationId)?.toDTO() ?: run {
            logger.warn("Reservation with id $reservationId not found")
            throw ReservationNotFound("Reservation with id $reservationId not found")
        }
    }

    override fun addReservation(reservation: ReservationDTO): ReservationDTO {
        checkUser(reservation.customerId)


        val duplicateReservation = reservationRepository.findByIdOrNull(reservation.id)

        if (duplicateReservation != null) {
            logger.warn("Reservation ${reservation.id} already exists")
            throw ReservationDuplicate("Reservation with id ${reservation.id} already exists")
        }

        val status = statusRepository.findFirstByStatus(reservation.status.status) ?: run {
            logger.warn("Status ${reservation.status.status} not found")
            throw StatusNotFound("Status ${reservation.status.status} not found")
        }


        val vehicle = vehicleRepository.findByIdOrNull(reservation.vehicleId) ?: run {
            logger.warn("Vehicle with id ${reservation.vehicleId} not found")
            throw VehicleNotFound("Vehicle with id ${reservation.vehicleId} not found")
        }

        val doubleReservation=reservationRepository.getReservationByVehicleAndStatus(vehicle.id,status.status)
        if(doubleReservation!=null){
            logger.warn("Reservation ${reservation.id} already exists")
            throw ReservationDuplicate("Reservation with id ${reservation.id} already exists")
        }

        val availability = availabilityRepository.findByType("rented") ?: run {
            logger.warn("Availability \"rented\" not found")
            throw AvailabilityNotFound("rented Availability not found")
        }

        vehicle.refAvailability = availability
        vehicleRepository.save(vehicle)

        val amount = vehicle.refCarModel.costPerDay * (reservation.endDate.time - reservation.startDate.time)/(1000 * 60 * 60 * 24) //milliseconds to days

        if(amount != reservation.paymentAmount){
            logger.warn("Payment amount ${reservation.paymentAmount} did not match the calculated amount $amount")
        }

        val reservationEntity = Reservation(
            customerId = reservation.customerId,
            employeeId = reservation.employeeId,
            vehicle = vehicle,
            status = status,
            startDate = reservation.startDate,
            endDate = reservation.endDate,
            reservationDate = reservation.reservationDate,
            paymentAmount = amount,
        )
        val savedReservation = reservationRepository.save(reservationEntity)
        logger.info("Reservation added successfully with ID: ${savedReservation.id}")
        return savedReservation.toDTO()
    }

    override fun updateReservationById(reservationId: Long, reservation: ReservationDTO): ReservationDTO {
        val existing = reservationRepository.findByIdOrNull(reservationId)
            ?: throw ReservationNotFound("Reservation with ID $reservationId not found")

        if (existing.version != reservation.version) {
            throw OptimisticLockingFailureException("Reservation was modified by another transaction")
        }

        val reservationEntity = reservationRepository.findByIdOrNull(reservationId) ?: run {
            logger.warn("Reservation with id ${reservation.id} not found")
            throw ReservationNotFound("Reservation with id ${reservationId}")
        }
        val vehicleEntity = vehicleRepository.findByIdOrNull(reservation.vehicleId) ?: run {
            logger.warn("Vehicle with id ${reservation.vehicleId} not found")
            throw VehicleNotFound("Vehicle with id ${reservation.vehicleId} not found")
        }
        checkUser(reservation.customerId)
        //checkUser(reservation.employeeId)

        val status = statusRepository.findFirstByStatus(reservation.status.status) ?: run {
            logger.warn("Status ${reservation.status.status} not found")
            throw StatusNotFound("Status ${reservation.status.status} not found")
        }

        lateinit var availability: Availability

        if (status.status == "PENDING" || status.status == "APPROVED" || status.status == "ON_COURSE") {
            availability = availabilityRepository.findByType("rented") ?: run {
                logger.warn("Availability \"rented\" not found")
                throw AvailabilityNotFound("rented Availability not found")
            }
        }else{
            availability = availabilityRepository.findByType("available") ?: run {
                logger.warn("Availability \"available\" not found")
                throw AvailabilityNotFound("rented Availability not found")
            }
        }

        vehicleEntity.refAvailability = availability
        vehicleRepository.save(vehicleEntity)

        val amount = vehicleEntity.refCarModel.costPerDay * (reservation.endDate.time - reservation.startDate.time)/(1000 * 60 * 60 * 24) //milliseconds to days

        if(amount != reservation.paymentAmount){
            logger.warn("Payment amount ${reservation.paymentAmount} did not match the calculated amount $amount")
        }

        reservationEntity.customerId = reservation.customerId
        reservationEntity.employeeId = reservation.employeeId
        reservationEntity.vehicle = vehicleEntity
        reservationEntity.status = status
        reservationEntity.startDate = reservation.startDate
        reservationEntity.endDate = reservation.endDate
        reservationEntity.reservationDate = reservation.reservationDate
        reservationEntity.paymentAmount = amount

        return reservationRepository.save(reservationEntity).toDTO()
    }

    override fun deleteReservationById(reservationId: Long) {
        val reservation = reservationRepository.findByIdOrNull(reservationId) ?: run {
            logger.warn("Reservation with id $reservationId not found")
            throw ReservationNotFound("Reservation with id ${reservationId}")
        }

        val availability = availabilityRepository.findByType("available") ?: run {
            logger.warn("Availability \"available\" not found")
            throw AvailabilityNotFound("available Availability not found")
        }
        reservation.vehicle.refAvailability = availability
        vehicleRepository.save(reservation.vehicle)

        reservationRepository.deleteById(reservationId)
    }

    override fun getReservationsByUserId(
        pageable: Pageable,
        userId: Long
    ): Page<ReservationDTO> {

        checkUser(userId)
        val reservations = reservationRepository.getReservationsByCustomerId(userId, pageable).map { it.toDTO() }
        logger.info("Reservations by user id: ${reservations.totalPages}")
        if (reservations.isEmpty) {
            throw ReservationNotFound("Reservation with userId $userId does not exists")
        }
        return reservations
    }

    fun checkUser(userId: Long) {
        try {
            logger.debug("Checking if passed user exists with id: ${userId}")
            //userClient.getUserById(userId)
            getUserByIdWithAuth(userId)
        } catch (ex: FeignException.NotFound) {
            throw UserNotFound("User with id $userId does not exist")
        }
    }

    override fun payReservation(reservationId: Long) : String {
        val reservation = reservationRepository.findByIdOrNull(reservationId) ?: run {
            logger.warn("Reservation with id $reservationId not found")
            throw ReservationNotFound("Reservation with id $reservationId not found")
        }

        if(reservation.status.status != "APPROVED" ) throw ReservationNotAccepted("Reservation with id $reservationId not yet approved")

        val amount = reservation.vehicle.refCarModel.costPerDay * (reservation.endDate.time - reservation.startDate.time)/(1000 * 60 * 60 * 24) //milliseconds to days
        logger.debug("Calculated amount: $amount")

        if(amount != reservation.paymentAmount){
            logger.warn("Payment amount ${reservation.paymentAmount} did not match the calculated amount $amount")
        }

        logger.info("Paying reservation with id: ${reservation.id} and amount: ${reservation.paymentAmount}")
        val id = reservation.id

        val paymentOrder = PaymentOrderRequestDTO(
            reservationId = id,
            paymentAmount = amount
        )

        //return paymentClient.createPayPalOrder(paymentOrder)
        return createPaypalOrder(paymentOrder)

    }

    @KafkaListener(
        id = "reservation-listener",
        topics = ["paypal.public.paypal_outbox_events"],
        groupId = "reservation-service"
    )
    fun handlePaymentStatusChange(
        event: String,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        @Header(KafkaHeaders.ACKNOWLEDGMENT) ack: Acknowledgment
    ) {

        try {
            //println("Evento ricevuto per reservationId=${event.reservationId} con stato=${event.paymentStatus}")

            val eventData = objectMapper.readValue(event, PaypalOutboxEvent::class.java)



            //val paymentData = paymentClient.getPaypalOrder(eventData.paypalToken)
            val paymentData = getPaypalOrderWithAuth(eventData.paypalToken)

            val reservationToUpdate = getReservationById(paymentData.reservationId)

            val reservationStatusToUpdate = when (paymentData.status) {
                PaymentStatus.COMPLETED -> "PAYED"
                PaymentStatus.CANCELLED -> "PAYMENT_REFUSED"
                else -> throw UnableToProcessEvent("Paypal status ${paymentData.status}")
            }

            reservationStatusToUpdate.let {
                val newStatusDTO = statusRepository.findFirstByStatus(it)?.toDTO()!!
                reservationToUpdate.status = newStatusDTO
                updateReservationById(paymentData.reservationId, reservationToUpdate)
            }

            ack.acknowledge() // conferma manuale del messaggio solo dopo successo*/

        } catch (ex: Exception) {
            println("Error in the event management: ${ex.message}")
            ack.nack(Duration.ofSeconds(10)) // riprova dopo 10 secondi
        }
    }

    override fun getFullyBookedDates(carModelId: Long): Set<LocalDate> {
        val rawData = reservationRepository.findReservationIntervalsByCarModel(carModelId)
        val vehicles = vehicleRepository.findAllByRefCarModelId(carModelId).filter { v-> v.refAvailability.type=="available" }
        val vehicleCount = vehicles.size

        val vehicleToDates = mutableMapOf<Long, MutableSet<LocalDate>>()

        for (row in rawData) {
            val vehicleId = row[0] as Long
            val start = (row[1] as Date).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val end = (row[2] as Date).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

            val dates = vehicleToDates.computeIfAbsent(vehicleId) { mutableSetOf() }

            var current = start
            while (!current.isAfter(end)) {
                dates.add(current)
                current = current.plusDays(1)
            }
        }

        // Intersezione delle date in cui TUTTI i vehicle sono prenotati
        if (vehicleToDates.isEmpty() || vehicleToDates.size < vehicleCount) {
            return emptySet()
        }

        return vehicleToDates.values.reduce { acc, set -> acc.intersect(set).toMutableSet() }
    }
}