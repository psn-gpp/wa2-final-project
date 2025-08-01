package it.wa2.reservationservice.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import it.wa2.reservationservice.dtos.ReservationDTO
import it.wa2.reservationservice.entities.Reservation
import it.wa2.reservationservice.entities.Status
import it.wa2.reservationservice.entities.Vehicle
import it.wa2.reservationservice.services.ReservationService
import it.wa2.reservationservice.services.ReservationServiceImpl
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.LocalDate
import java.util.*

/**
 * Get all reservations: [getReservations]
 *
 * Get one reservation: [getReservationById]
 *
 * Get user's reservations: [getReservationsByUserId] forse
 *
 * add: [addReservation]
 *
 * delete: [deleteReservationById]
 *
 * put: [updateReservationById]
 *
 * pay: [payReservation]
 *
 * get car model taken dates: [getCarModelTakenDates]
 *
 */

@RestController
@RequestMapping("/api/v1/reservations")
@Validated
class ReservationController(private val reservationService: ReservationService) {
    private val logger = LoggerFactory.getLogger(ReservationController::class.java)


    @Operation(
        summary = "Get reservations",
        description = "get all reservations"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    @PreAuthorize("hasRole('Staff') or hasRole('Customer')")
    @GetMapping("")
    fun getReservations(
        pageable: Pageable,
        @RequestParam(required = false) customerId: Long?,
        @RequestParam(required = false) employeeId: Long?,
        @RequestParam(required = false) carModelId: Long?,
        @RequestParam(required = false) status: String?,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @RequestParam(required = false) reservationDate: Date?,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @RequestParam(required = false) startDate: Date?,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @RequestParam(required = false) endDate: Date?,
    ): Page<ReservationDTO> {
        logger.info("Getting all reservations")

        val reservations =  reservationService.getReservations(
            pageable,
            customerId,
            employeeId,
            carModelId,
            status,
            reservationDate,
            startDate,
            endDate,
        )

        logger.debug("Reservations retrieved: {} ", reservations)
        return reservations
    }


    @Operation(
        summary = "Get single reservation",
        description = "Returns a reservation",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    @PreAuthorize("hasRole('Staff') or hasRole('Customer')")
    @GetMapping("/{reservationId}")
    fun getReservationById(
        @PathVariable reservationId: Long
    ): ReservationDTO {
        logger.info("Getting reservation with id: {}", reservationId)
        val reservation = reservationService.getReservationById(reservationId)
        logger.debug("Reservation retrieved: {} ", reservation)
        return reservation
    }

    @Operation(
        summary = "Add a new reservation",
        description = "Creates a new reservation",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Created"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )
    @PreAuthorize("hasRole('Customer')")
    @PostMapping("")
    fun addReservation(
        @Valid @RequestBody reservation: ReservationDTO,
        uriBilder: UriComponentsBuilder
    ): ResponseEntity<ReservationDTO> {
        logger.info("Adding reservation: {}", reservation)

        val newReservation = reservationService.addReservation(reservation)
        logger.debug("new reservation is : {} ", newReservation)

        val location = uriBilder.path("api/v1/reservations/${reservation.id}").buildAndExpand(newReservation.id).toUri()
        logger.debug("location is: {} ", location)

        return ResponseEntity.created(location).body(newReservation)
    }

    @Operation(
        summary = "Update a reservation",
        description = "Update a reservation",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )
    @PreAuthorize("hasRole('Staff') or hasRole('Customer')")
    @PutMapping("/{reservationId}")
    fun updateReservationById(
        @Valid @PathVariable reservationId: Long,
        @RequestBody reservation: ReservationDTO
    ): ReservationDTO{
        logger.info("updating reservation: {}", reservationId)
        return reservationService.updateReservationById(reservationId, reservation)
    }

    @Operation(
        summary = "Delete a reservation",
        description = "Delete a reservation by id",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    @PreAuthorize("hasRole('Staff') or hasRole('Customer')")
    @DeleteMapping("/{reservationId}")
    fun deleteReservationById(
        @Valid @PathVariable reservationId: Long
    ){
        logger.info("Deleting reservation by id: {}", reservationId)
        return reservationService.deleteReservationById(reservationId)
    }

    @Operation(
        summary = "Get reservations by userId",
        description = "get all reservations by a specific userId"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    @PreAuthorize("hasRole('Staff') or hasRole('Customer')")
    @GetMapping("users/{userId}")
    fun getReservationsByUserId(
        pageable: Pageable,
        @PathVariable userId: Long,
    ): Page<ReservationDTO> {
        logger.info("Getting reservations for user {}", userId)
        val userReservations = reservationService.getReservationsByUserId(pageable, userId)
        logger.debug("User reservations retrieved: {} ", userReservations)
        return userReservations
    }

    @Operation(
        summary = "Pay a reservation",
        description = "Returns link to payment (PayPal)",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Reservation Not found"),
            ApiResponse(responseCode = "403", description = "Forbidden action: Reservation not yet accepted from a manager"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    @PreAuthorize("hasRole('Customer')")
    @PostMapping("/{reservationId}/pay")
    fun payReservation(
        @PathVariable reservationId: Long
    ) : String {
        logger.info("Creating payment for reservation with id: {}", reservationId)
        return reservationService.payReservation(reservationId)
    }

    @GetMapping("/{carModelId}/takenDates")
    fun getCarModelTakenDates(
        @PathVariable carModelId: Long
    ) : Set<LocalDate> {
        return reservationService.getFullyBookedDates(carModelId)
    }

}