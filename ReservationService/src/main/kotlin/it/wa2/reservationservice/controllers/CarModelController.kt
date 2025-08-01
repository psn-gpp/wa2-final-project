package it.wa2.reservationservice.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import it.wa2.reservationservice.dtos.CarModelDTO
import it.wa2.reservationservice.dtos.CarModelFiltersDTO
import it.wa2.reservationservice.services.CarModelService
import it.wa2.reservationservice.services.CarModelServiceImpl
import jakarta.validation.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated

import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder


@RestController
@Validated
class CarModelController(private val carModelService: CarModelService) {
    private val logger = LoggerFactory.getLogger(CarModelController::class.java)

    @Operation(
        summary = "Get all car models",
        description = "Returns all car models"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("api/v1/models")
    fun getModels(
        pageable: Pageable,
        @RequestParam(required = false) brand: String?,
        @RequestParam(required = false) model: String?,
        @RequestParam(required = false) modelYear: Int?,
        @RequestParam(required = false) segment: String?,
        @RequestParam(required = false) doorsNo: Int?,
        @RequestParam(required = false) seatingCapacity: Int?,
        @RequestParam(required = false) luggageCapacity: Float?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) manufacturer: String?,
        @RequestParam(required = false) engine: String?,
        @RequestParam(required = false) transmission: String?,
        @RequestParam(required = false) drivetrain: String?,
        @RequestParam(required = false) safetyFeatures: List<String>?,
        @RequestParam(required = false) infotainments: List<String>?
    ): Page<CarModelDTO> {
        return carModelService.getModels(
            pageable,
            brand,
            model,
            modelYear,
            segment,
            doorsNo,
            seatingCapacity,
            luggageCapacity,
            category,
            manufacturer,
            engine,
            transmission,
            drivetrain,
            safetyFeatures,
            infotainments
        )
    }

    @Operation(
        summary = "Get a specific car models by id",
        description = "Returns the car model specified by id",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )
    //  da chiedere validazione parametri URL e vari codici di errore
    @GetMapping("api/v1/models/{carModelId}")
    fun getModelById(@PathVariable("carModelId") carModelId: Long): CarModelDTO {
        return carModelService.getModelById(carModelId)
    }


    @Operation(
        summary = "Add a new car model",
        description = "Creates a new car model",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Created"),
            ApiResponse(responseCode = "409", description = "Conflict"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )

    @PreAuthorize("hasRole('Fleet_Manager')")
    @PostMapping("api/v1/models")
    fun addModel(
        @Valid @RequestBody model: CarModelDTO,
        uriBilder: UriComponentsBuilder
    ): ResponseEntity<CarModelDTO> {
        logger.info("Model ${model.model} api")
        val newCarModel = carModelService.addModel(model)
        val location = uriBilder.path("api/v1/models/{carModelId}").buildAndExpand(newCarModel.id).toUri()

        return ResponseEntity.created(location).body(newCarModel)
    }


    @Operation(
        summary = "Update a car model",
        description = "Update a car model by id",
    )
    @PreAuthorize("hasRole('Fleet_Manager')")
    @PutMapping("api/v1/models/{carModelId}")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )
    fun updateModelById(@Valid @PathVariable("carModelId") carModelId: Long, @RequestBody model: CarModelDTO) {
        return carModelService.modifyCarModel(carModelId, model)
    }


    @Operation(
        summary = "Delete a car model",
        description = "Delete a car model by id",
    )
    @PreAuthorize("hasRole('Fleet_Manager')")
    @DeleteMapping("api/v1/models/{carModelId}")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "404", description = "Not found"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    fun deleteModelById(@PathVariable("carModelId") carModelId: Long) {
        return carModelService.deleteModelById(carModelId)
    }

    @Operation(
        summary = "Get filters for car models",
        description = "Return all possible filters for car models : categories, engines, transmissions, drivetrains, safety features and infotainments",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "500", description = "Internal server error"),
        ]
    )
    @GetMapping("api/v1/models/filters")
    fun getCarModelsFilters(): CarModelFiltersDTO {
        return carModelService.getFilters()
    }

}