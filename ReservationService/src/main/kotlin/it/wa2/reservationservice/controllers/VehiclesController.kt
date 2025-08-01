package it.wa2.reservationservice.controllers

import com.github.fge.jsonpatch.JsonPatch
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import it.wa2.reservationservice.dtos.*
import it.wa2.reservationservice.services.VehicleService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@RestController
@Validated
class VehiclesController(private val vehicleService: VehicleService) {

    @Operation(
        summary = "Get all vehicles",
        description = "Returns all vehicles"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("api/v1/vehicles")
    fun getVehicles(
        pageable: Pageable,
        @RequestParam(required = false) refCarModel : Long?,
        @RequestParam(required = false) availability : String?,
        @RequestParam(required = false) licencePlate : String?,
        @RequestParam(required = false) vin : String?,
        @RequestParam(required = false) kilometers : Float?,
        @RequestParam(required = false) pendingCleaning : Boolean?,
        @RequestParam(required = false) pendingMaintenance : Boolean?,
    ): Page<VehicleDTO> {
        return vehicleService.getVehicles(pageable, refCarModel, availability, licencePlate, vin, kilometers, pendingCleaning, pendingMaintenance )
    }
    //#############################

    // ########### GIUSEPPE #########################
    @Operation(
        summary = "Get a specific vehicle by id",
        description = "Returns the vehicle specified by id"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Not found"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
        ApiResponse(responseCode = "400", description = "Bad request"),
    ])
    @GetMapping("api/v1/vehicles/{vehicleId}")
    fun getVehicleById(@PathVariable("vehicleId") vehicleId : Long):VehicleDTO{
        return vehicleService.getVehicleById(vehicleId)
    }

    @Operation(
        summary = "Add a vehicle",
        description = "Add a new vehicle"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Not found"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
        ApiResponse(responseCode = "400", description = "Bad request"),
    ])

    @PreAuthorize("hasRole('Fleet_Manager')")
    @PostMapping("api/v1/vehicles")
    fun addVehicle(
        @Valid @RequestBody vehicle: VehicleDTO,
        uriBuilder: UriComponentsBuilder
    ) : ResponseEntity<VehicleDTO> {

        val savedVehicle = vehicleService.addVehicle(vehicle)

        val location =  uriBuilder.path("api/v1/vehicles/{vehicleId}")
            .buildAndExpand(savedVehicle.id)
            .toUri()

        return ResponseEntity.created(location).body(savedVehicle)
    }


    @PreAuthorize("hasRole('Fleet_Manager')")
    @Operation(
        summary = "update a vehicle",
        description = "upadate a vehicle by its id"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Not found"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
        ApiResponse(responseCode = "400", description = "Bad request"),
    ])
    @PutMapping("api/v1/vehicles/{vehicleId}")
    fun updateVehicleById(
        @PathVariable("vehicleId") vehicleId : Long,
        @Valid @RequestBody vehicle: VehicleDTO)
    {
        vehicleService.modifyVehicle(vehicleId, vehicle)
    }


    @PreAuthorize("hasRole('Fleet_Manager')")
    @Operation(
        summary = "Delete a vehicle",
        description = "Delete a a vehicle by id"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Not found"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
        ApiResponse(responseCode = "400", description = "Bad request"),
    ])
    @DeleteMapping("api/v1/vehicles/{vehicleId}")
    fun deleteVehicleById(@PathVariable("vehicleId") vehicleId : Long){
        return vehicleService.deleteVehicleById(vehicleId)
    }

    //########################################àààà

    //

    //### CHRISTIAN ##############
    //////////////
    //OPTIONAL---> guarda traccia laboratorio
    @PreAuthorize("hasRole('Fleet_Manager')")
    @PatchMapping("api/v1/vehicles/{carId}", consumes = ["application/json-patch+json"])
    fun patchVehicleById(
        @PathVariable("carId") carId : Long,
        @RequestBody patch: JsonPatch
    ) {
        vehicleService.patchVehicleById(carId,patch)
    }


    //////////////
    @Operation(
        summary = "Get maintenances",
        description = "get all maintenances of a specific vehicle"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Not found"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
    ])
    @PreAuthorize("hasRole('Fleet_Manager') or hasRole('Staff')")
    @GetMapping("api/v1/vehicles/{vehicleId}/maintenances")
    fun getVehicleMaintenances(
        pageable: Pageable,
        @RequestParam(required = false) vehicleLicencePlate: String?,
        @RequestParam(required = false) defect: String?,
        @RequestParam(required = false) completedMaintenance: Boolean?,
        @RequestParam(required = false) date: String?,
        @PathVariable("vehicleId") vehicleId: Long
    ) : Page<MaintenanceDTO>{
        return vehicleService.getVehicleMaintenances(pageable,vehicleId, vehicleLicencePlate,defect,completedMaintenance,date)
    }

    @Operation(
        summary = "Get a maintenance",
        description = "get a specific maintenance of a specific vehicle"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Not found"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
    ])
    @PreAuthorize("hasRole('Fleet_Manager') or hasRole('Staff')")
    @GetMapping("api/v1/vehicles/{vehicleId}/maintenances/{maintenanceId}")
    fun getVehicleMaintenanceById(
        @PathVariable("vehicleId") vehicleId: Long,
        @PathVariable("maintenanceId") maintenanceId : Long
    ) : MaintenanceDTO{
        return vehicleService.getVehicleMaintenanceById(vehicleId,maintenanceId)
    }

    @Operation(
        summary = "Add a maintenance",
        description = "Add a new maintenance to a specific vehicle"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Not found"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
        ApiResponse(responseCode = "400", description = "Bad request"),
    ])
    @PreAuthorize("hasRole('Staff')")
    @PostMapping("api/v1/vehicles/{vehicleId}/maintenances")
    fun addVehicleMaintenance(
        @PathVariable("vehicleId") vehicleId : Long,
        @Valid @RequestBody maintenance : MaintenanceDTO,
        uriBuilder: UriComponentsBuilder
    ):ResponseEntity<MaintenanceDTO>{
        val savedMaintenance=vehicleService.addVehicleMaintenance(vehicleId, maintenance)

        val location = uriBuilder.path("api/v1/vehicles/{vehicleId}/maintenances/{maintenanceId}")
            .buildAndExpand(vehicleId,savedMaintenance.id)
            .toUri()

        //return the response to client
        return ResponseEntity.created(location).body(savedMaintenance)
    }

    //####################à

    // ###### MATTEO #########
    @Operation(summary = "Modify a maintenance about a vehicle",
        description = "Modify a specific maintantence (maintenanceId) about a specific vehicleId",)
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Vehicle not found"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
        ApiResponse(responseCode="400", description = "Bad request"),
    ])
    @PreAuthorize("hasRole('Staff') or hasRole('Fleet_Manager')")
    @PutMapping("api/v1/vehicles/{vehicleId}/maintenances/{maintenanceId}")
    fun modifyVehicleMaintenance(
        @PathVariable("vehicleId") vehicleId : Long,
        @PathVariable("maintenanceId") maintenanceId : Long,
        @Valid @RequestBody maintenance : MaintenanceDTO
    ){
        vehicleService.modifyVehicleMaintenance(vehicleId, maintenanceDTO = maintenance)
    }

    @Operation(summary = "Get notes about a vehicle",
        description = "Returns list of all notes about a specific vehicleId",)
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Vehicle not found"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
        ApiResponse(responseCode="400", description = "Bad request"),
    ])
    @PreAuthorize("hasRole('Fleet_Manager') or hasRole('Staff')")
    @GetMapping("api/v1/vehicles/{vehicleId}/notes")
    //we pass page number page size and sort as request parameter
    //like: GET /vehicles/1/notes?page=0&size=10&sort=field,desc
    // sort=field, desc --> result are sorted by field in descending way
    // ?sort=field1desc&sort=field2,asc --> result order first by field1 then by field2
    // sorting on nested fields --> sort=vehicle.name,asc
    // sorting and pages are supported automatically by springboot
    fun getVehicleNotes(
        @PathVariable("vehicleId") vehicleId : Long,
        pageable: Pageable,
        @RequestParam(required = false) startDate : String?,
        @RequestParam(required = false) endDate : String?,
        @RequestParam(required = false) author: String?) : Page<NoteDTO>{


        val startD = startDate?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
        val endD = endDate?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }

        return vehicleService.getVehicleNotes(vehicleId, pageable,startD,endD,author)
    }

    @Operation(summary = "Add a new note about a vehicle",
        description = "insert a new note for a specific vehicleId",)
    @ApiResponses(value = [
        ApiResponse(responseCode ="201", description = "Created a new note"),
        ApiResponse(responseCode = "404", description = "Vehicle not found"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
        ApiResponse(responseCode="400", description = "Bad request: note is not correct"),
    ])
    @PreAuthorize("hasRole('Fleet_Manager') or hasRole('Staff')")
    @PostMapping("api/v1/vehicles/{vehicleId}/notes")
    fun addVehicleNote(
        @PathVariable("vehicleId") vehicleId: Long,
        @Valid @RequestBody note: NoteDTO,
        uriBuilder: UriComponentsBuilder
        //può essere meglio usare NoteResponseDTO diverso da NoteDTO --> vedere meglio
    ): /*ResponseEntity<NoteResponseDTO>*/ ResponseEntity<NoteDTO> {

        val savedNote = vehicleService.addVehicleNote(vehicleId, note)

        // URI creation for the new saved note
        //val location = uriBuilder.path("api/v1/vehicles/{vehicleId}/notes/{noteId}")
        val location = uriBuilder.path("api/v1/vehicles/{vehicleId}/notes")
            .buildAndExpand(vehicleId/*, savedNote.id*/)
            .toUri()

        //return the response to client
        return ResponseEntity.created(location).body(savedNote)
    }

    // ############

    // PUT note

    @Operation(summary = "Modify a note about a vehicle",
        description = "Modify a specific note (noteId) about a specific vehicleId",)
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Vehicle not found"),
        ApiResponse(responseCode = "404", description = "Note not found"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
        ApiResponse(responseCode="400", description = "Bad request"),
    ])
    @PreAuthorize("hasRole('Fleet_Manager') or hasRole('Staff')")
    @PutMapping("api/v1/vehicles/{vehicleId}/notes/{noteId}")
    fun modifyVehicleNote(
        @PathVariable("vehicleId") vehicleId : Long,
        @PathVariable("noteId") noteId : Long,
        @Valid @RequestBody note : NoteDTO
    ){
        vehicleService.modifyVehicleNote(vehicleId, noteId, note)
    }

    // DELETE note
    @Operation(summary = "Delete a note about a vehicle",
        description = "Delete a specific note (noteId) about a specific vehicleId",)
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Vehicle not found"),
        ApiResponse(responseCode = "404", description = "Note not found"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
        ApiResponse(responseCode="400", description = "Bad request"),
    ])
    @PreAuthorize("hasRole('Fleet_Manager') or hasRole('Staff')")
    @DeleteMapping("api/v1/vehicles/{vehicleId}/notes/{noteId}")
    fun deleteVehicleNote(
        @PathVariable("vehicleId") vehicleId : Long,
        @PathVariable("noteId") noteId : Long
    ){
        vehicleService.deleteVehicleNote(vehicleId, noteId)
    }

    @Operation(
        summary = "Get all filter types",
        description = "Returns all filter types (availability)"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "OK"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    
    @GetMapping("api/v1/vehicles/filters")
    fun getVehiclesFilters(): VehicleFiltersDTO {
        return vehicleService.getFilters()
    }
}