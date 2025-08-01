package it.wa2.reservationservice.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import it.wa2.reservationservice.controllers.*

import it.wa2.reservationservice.dtos.*

import it.wa2.reservationservice.dtos.MaintenanceDTO
import it.wa2.reservationservice.dtos.NoteDTO
import it.wa2.reservationservice.dtos.VehicleDTO
import it.wa2.reservationservice.dtos.VehicleFiltersDTO

import it.wa2.reservationservice.entities.*
import it.wa2.reservationservice.repositories.*
import it.wa2.reservationservice.entities.toDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sql.DataSource


@Service
@Primary
@Validated
class VehicleServiceImpl(
    private val vehicleRepository: VehicleRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val noteRepository: NoteRepository,
    private val carModelRepository: CarModelRepository,
    private val availabilityRepository: AvailabilityRepository
) : VehicleService {
    private val logger = LoggerFactory.getLogger(VehicleServiceImpl::class.java)

    override fun getFilters(): VehicleFiltersDTO {
        logger.info("Getting available vehicle filters")
        return VehicleFiltersDTO(
            availabilities = availabilityRepository.findAll().map { it.type }.toMutableList()
        )
    }

    // ############## GIUSEPPE ##################

    override fun getVehicleById(vehicleId: Long): VehicleDTO {
        logger.info("Getting vehicle with id: $vehicleId")
        val vehicle = vehicleRepository.findByIdOrNull(vehicleId)
            ?: throw VehicleNotFound("Vehicle not found")
        logger.debug("Found vehicle: $vehicle")
        return vehicle.toDTO()
    }

    @Autowired
        lateinit var dataSource : DataSource

        override fun addVehicle(vehicleDTO: VehicleDTO): VehicleDTO {
/*        logger.info(">>> Controller DB: ${dataSource.connection.metaData.url}")
        logger.info(">>> Controller DB: ${dataSource.connection.metaData.userName}")*/

        logger.info("Adding new vehicle with licence plate: ${vehicleDTO.licencePlate}")
        val licencePlateVehicle = vehicleRepository.findByLicencePlate(vehicleDTO.licencePlate)
        if (licencePlateVehicle.isNotEmpty()) {
            logger.warn("ADD VEHICLE - Vehicle with licence plate ${vehicleDTO.licencePlate} already exists : ${licencePlateVehicle[0].id}")
            throw VehicleDuplication("ADD VEHICLE - Licence plate already exists")
        }
        val carModel = carModelRepository.findByIdOrNull(vehicleDTO.refCarModel)
            ?: run {
                logger.warn("Car model with id ${vehicleDTO.refCarModel} not found")
                throw CarModelNotFound("Car model not found")
            }

        //val availability = availabilityRepository.findByIdOrNull(vehicleDTO.refAvailability)
        //if (!availabilityRepository.existsByType(vehicleDTO.availability)) throw AvailabilityNotFound("Availability not found")
        val availability = availabilityRepository.findByType(vehicleDTO.availability)
            ?: run {
                logger.warn("Availability type ${vehicleDTO.availability} not found")
                throw AvailabilityNotFound("Availability not found")
            }

        val newVehicle = Vehicle(
            refCarModel = carModel,
            refAvailability = availability,
            licencePlate = vehicleDTO.licencePlate,
            vin = vehicleDTO.vin,
            kilometers = vehicleDTO.kilometers,
            pendingCleaning = vehicleDTO.pendingCleaning,
            pendingMaintenance = vehicleDTO.pendingMaintenance,
        )

        val savedVehicle = vehicleRepository.save(newVehicle)

        logger.info("Vehicle saved successfully with id: ${savedVehicle.id}")
        logger.debug("Saved vehicle details: $savedVehicle")

        return savedVehicle.toDTO()
    }

    override fun modifyVehicle(vehicleId: Long, vehicleDTO: VehicleDTO) {
        logger.info("Modifying vehicle with id: $vehicleId")

        val vehicle = vehicleRepository.findByIdOrNull(vehicleId)
            ?: run {
                logger.warn("Vehicle with id $vehicleId not found")
                throw VehicleNotFound("Vehicle not found")
            }

        val carModel = carModelRepository.findByIdOrNull(vehicleDTO.refCarModel)
            ?: run {
                logger.warn("Car model with id ${vehicleDTO.refCarModel} not found")
                throw CarModelNotFound("Car model not found")
            }

        /*val availability = availabilityRepository.findByIdOrNull(vehicleDTO.refAvailability)
            ?: throw AvailabilityNotFound("Availability not found")*/
        val availability = availabilityRepository.findByType(vehicleDTO.availability)
            ?: run {
                logger.warn("Availability type ${vehicleDTO.availability} not found")
                throw AvailabilityNotFound("Availability not found")
            }

        // non capisco perchè questo controllo non funziona -> ho aggiunto quello sotto
       /* if(vehicleRepository.existsByLicencePlate(vehicleDTO.licencePlate)) {
            throw VehicleDuplication("Licence plate already exists")
        }*/

        // ritorna una list di vehicles
        val vehiclesList = vehicleRepository.findByLicencePlate(vehicleDTO.licencePlate)
        if (vehiclesList.size != 1) {
            logger.warn("Multiple vehicles found with same licence plate: ${vehicleDTO.licencePlate}")
            throw VehicleDuplication("Review the request, we have more than one vehicle with the same license plate")
        } else {
            if (vehiclesList[0].id != vehicleId) {
                logger.warn("UPDATE VEHICLE - Attempted to update vehicle ${vehicleId} with duplicate licence plate ${vehicleDTO.licencePlate}")
                throw VehicleDuplication("UPDATE VEHICLE - Licence plate already exists")
            }
        }
        val updatedVehicle = Vehicle(
            id = vehicle.id,
            refCarModel = carModel,
            refAvailability = availability,
            licencePlate = vehicleDTO.licencePlate,
            vin = vehicleDTO.vin,
            kilometers = vehicleDTO.kilometers,
            pendingCleaning = vehicleDTO.pendingCleaning,
            pendingMaintenance = vehicleDTO.pendingMaintenance
        )

        vehicleRepository.save(updatedVehicle)

        logger.info("Successfully updated vehicle with id: ${updatedVehicle.id}")
        logger.debug("Updated vehicle details: $updatedVehicle")
    }

    override fun deleteVehicleById(vehicleId: Long) {
        logger.info("Deleting vehicle with id: $vehicleId")

        vehicleRepository.findByIdOrNull(vehicleId) ?: run {
            logger.warn("Vehicle with id $vehicleId not found")
            throw VehicleNotFound("Vehicle not found")
        }

        vehicleRepository.deleteById(vehicleId)

        logger.info("Successfully deleted vehicle with id: $vehicleId")
    }

    // #############################

    override fun modifyVehicleMaintenance(vehicleId: Long, maintenanceDTO: MaintenanceDTO) {
        logger.info("Modifying maintenance for vehicle with id: $vehicleId")

        val vehicle = vehicleRepository.findByIdOrNull(vehicleId) ?: run {
            logger.warn("Vehicle with id $vehicleId not found")
            throw VehicleNotFound("Vehicle not found")
        }

        vehicleRepository.findByIdOrNull(maintenanceDTO.vehicleId) ?: run {
            logger.warn("Vehicle with id ${maintenanceDTO.vehicleId} not found")
            throw VehicleNotFound("Vehicle not found")
        }

        if (maintenanceDTO.vehicleId != vehicleId) {
            logger.warn("Vehicle ID mismatch: expected $vehicleId but got ${maintenanceDTO.vehicleId}")
            throw VehicleIdInconsistent("VehicleId are inconsistent")
        }

        val maintenanceToUpdate = maintenanceRepository.findByIdOrNull(maintenanceDTO.id)
            ?: run {
                logger.warn("Maintenance with id ${maintenanceDTO.id} not found")
                throw MaintenanceNotFound("Maintenance not found")
            }

        if (maintenanceToUpdate.vehicle.id != vehicleId) {
            logger.warn("Maintenance ${maintenanceDTO.id} not found for vehicle $vehicleId")
            throw MaintenanceNotFound("Maintenance not found for the specified vehicle")
        }

        val parsedDate = maintenanceDTO.date?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }

        val newMaintenance = MaintenanceHistory(
            maintenanceToUpdate.id,
            vehicle,
            maintenanceDTO.defect,
            maintenanceDTO.completedMaintenance,
            parsedDate
        )

        maintenanceRepository.save(newMaintenance)
        logger.info("Successfully updated maintenance ${maintenanceDTO.id} for vehicle $vehicleId")
        logger.debug("Updated maintenance details: $newMaintenance")
    }

    override fun getVehicleNotes(
        vehicleId: Long,
        pageable: Pageable,
        startDate: LocalDate?,
        endDate: LocalDate?,
        author: String?
    ): Page<NoteDTO> {
        logger.info("Getting notes for vehicle $vehicleId with filters - startDate: $startDate, endDate: $endDate, author: $author")

        val vehicle = vehicleRepository.findByIdOrNull(vehicleId) ?: run {
            logger.warn("Vehicle with id $vehicleId not found")
            throw VehicleNotFound("Vehicle not found")
        }

        val result = if (startDate != null && endDate != null && author == null) {
            noteRepository.findAllByVehicleAndDateBetween(vehicle, startDate, endDate, pageable)
                .map { it.toDto() }
        } else if (startDate != null && endDate != null && author != null) {
            noteRepository.findAllByVehicleAndDateBetweenAndAuthorContainsIgnoreCase(
                vehicle, startDate, endDate, author, pageable
            ).map { it.toDto() }
        } else if (startDate == null && endDate == null && author != null) {
            noteRepository.findAllByVehicleAndAuthorContainsIgnoreCase(vehicle, author, pageable)
                .map { it.toDto() }
        } else {
            noteRepository.findAllByVehicle(vehicle, pageable).map { it.toDto() }
        }

        logger.debug("Found ${result.totalElements} notes")
        return result
    }

    override fun addVehicleNote(vehicleId: Long, noteDTO: NoteDTO): NoteDTO {
        logger.info("Adding new note for vehicle $vehicleId")

        val vehicle = vehicleRepository.findByIdOrNull(vehicleId) ?: run {
            logger.warn("Vehicle with id $vehicleId not found")
            throw VehicleNotFound("Vehicle not found: ${vehicleId}")
        }

        vehicleRepository.findByIdOrNull(noteDTO.vehicleId) ?: run {
            logger.warn("Vehicle with id ${noteDTO.vehicleId} not found")
            throw VehicleNotFound("Vehicle not found")
        }

        if (noteDTO.vehicleId != vehicleId) {
            logger.warn("Vehicle ID mismatch: expected $vehicleId but got ${noteDTO.vehicleId}")
            throw VehicleIdInconsistent("VehicleId are inconsistent")
        }

        if (noteRepository.existsNoteByTextAndVehicleId(noteDTO.text, vehicleId)) {
            logger.warn("Duplicate note text found for vehicle $vehicleId")
            throw DuplicatedNote("Note already exists for this vehicle")
        }

        val parsedDate = LocalDate.parse(noteDTO.date, DateTimeFormatter.ISO_LOCAL_DATE)

        //controllare se l'id viene gestito bene nel db
        //idea: the note id will be 0/null and the db will create a new entry
        val noteToAdd = Note(vehicle = vehicle, text = noteDTO.text, author = noteDTO.author, date = parsedDate)
        val savedNote = noteRepository.save(noteToAdd)

        logger.info("Successfully added note for vehicle $vehicleId")
        logger.debug("Added note details: $savedNote")

        return savedNote.toDto()
    }

    override fun getVehicles(
        pageable: Pageable,
        refCarModel: Long?,
        availability: String?,
        licencePlate: String?,
        vin: String?,
        kilometers: Float?,
        pendingCleaning: Boolean?,
        pendingMaintenance: Boolean?
    ): Page<VehicleDTO> {
        logger.info(
            "Getting vehicles with filters - carModel: $refCarModel, availability: $availability, " +
                    "licencePlate: $licencePlate, vin: $vin, kilometers: $kilometers, " +
                    "pendingCleaning: $pendingCleaning, pendingMaintenance: $pendingMaintenance"
        )

        if (refCarModel != null) {
            val carModel = carModelRepository.findByIdOrNull(refCarModel) ?: run {
                logger.warn("Car model with id $refCarModel not found")
                throw VehicleNotFound("Car model not found")
            }
            return vehicleRepository.findWithFilters(
                pageable, carModel, availability, licencePlate,
                vin, kilometers, pendingCleaning, pendingMaintenance
            )
                .map { it.toDTO() }
        }
        return vehicleRepository.findWithFilters(
            pageable, null, availability, licencePlate,
            vin, kilometers, pendingCleaning, pendingMaintenance
        )
            .map { it.toDTO() }
    }


    override fun modifyVehicleNote(vehicleId: Long, noteId: Long, noteDTO: NoteDTO) {
        logger.info("Modifying note $noteId for vehicle $vehicleId")

        val vehicle = vehicleRepository.findByIdOrNull(vehicleId) ?: run {
            logger.warn("Vehicle with id $vehicleId not found")
            throw VehicleNotFound("Vehicle not found")
        }

        vehicleRepository.findByIdOrNull(noteDTO.vehicleId) ?: run {
            logger.warn("Vehicle with id ${noteDTO.vehicleId} not found")
            throw VehicleNotFound("Vehicle not found")
        }

        if (noteDTO.vehicleId != vehicleId) {
            logger.warn("Vehicle ID mismatch: expected $vehicleId but got ${noteDTO.vehicleId}")
            throw VehicleIdInconsistent("VehicleId are inconsistent")
        }

        val noteToUpdate = noteRepository.findByIdOrNull(noteId) ?: run {
            logger.warn("Note with id $noteId not found")
            throw NoteNotFound("Note not found")
        }

        if (noteToUpdate.vehicle.id != vehicleId) {
            logger.warn("Note $noteId not found for vehicle $vehicleId")
            throw NoteNotFound("Note not found for the specified vehicle")
        }

        /*if(noteRepository.existsNoteByTextAndVehicleId(noteDTO.text, vehicleId)){
            throw DuplicatedNote("Note already exists for this vehicle")
        }*/
        val parsedDate = LocalDate.parse(noteDTO.date, DateTimeFormatter.ISO_LOCAL_DATE)

        //I create a new note instead of modify the old one
        val newNote = Note(
            noteToUpdate.id,
            vehicle,
            noteDTO.text,
            noteDTO.author,
            parsedDate
        )

        noteRepository.save(newNote)

        logger.info("Successfully updated note $noteId for vehicle $vehicleId")
        logger.debug("Updated note details: $newNote")
    }

    override fun deleteVehicleNote(vehicleId: Long, noteId: Long) {
        logger.info("Deleting note $noteId from vehicle $vehicleId")

        vehicleRepository.findByIdOrNull(vehicleId) ?: run {
            logger.warn("Vehicle with id $vehicleId not found")
            throw VehicleNotFound("Vehicle not found")
        }

        noteRepository.findByIdOrNull(noteId) ?: run {
            logger.warn("Note with id $noteId not found")
            throw NoteNotFound("Note not found")
        }

        noteRepository.deleteById(noteId)

        logger.info("Successfully deleted note $noteId from vehicle $vehicleId")
    }

    override fun patchVehicleById(carId: Long, patch: JsonPatch): ResponseEntity<VehicleDTO> {
        logger.info("Applying patch to vehicle $carId")

        val existingVehicle = vehicleRepository.findByIdOrNull(carId) ?: run {
            logger.warn("Vehicle with id $carId not found")
            throw VehicleNotFound("Vehicle not found")
        }

        return try {
            val patchedVehicleDTO = applyPatchToVehicle(patch, existingVehicle.toDTO())

            val carModel = carModelRepository.findByIdOrNull(patchedVehicleDTO.refCarModel) ?: run {
                logger.warn("Car model with id ${patchedVehicleDTO.refCarModel} not found")
                throw CarModelNotFound("Car model not found")
            }

            val availability = availabilityRepository.findByType(patchedVehicleDTO.availability) ?: run {
                logger.warn("Availability type ${patchedVehicleDTO.availability} not found")
                throw AvailabilityNotFound("Availability not found")
            }

            // ✨ Modifica l'oggetto esistente invece di crearne uno nuovo
            existingVehicle.apply {
                refCarModel = carModel
                refAvailability = availability
                licencePlate = patchedVehicleDTO.licencePlate
                vin = patchedVehicleDTO.vin
                kilometers = patchedVehicleDTO.kilometers
                pendingCleaning = patchedVehicleDTO.pendingCleaning
                pendingMaintenance = patchedVehicleDTO.pendingMaintenance
            }

            // ✨ Salva l'oggetto aggiornato
            vehicleRepository.save(existingVehicle)
            logger.info("Successfully patched vehicle $carId")
            logger.debug("Updated vehicle details: $existingVehicle")

            ResponseEntity.ok(existingVehicle.toDTO())
        } catch (e: Exception) {
            logger.warn("Error patching vehicle $carId: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    fun applyPatchToVehicle(patch: JsonPatch, target: VehicleDTO): VehicleDTO {
        logger.debug("Applying JSON patch to vehicle")
        val mapper = ObjectMapper()
        val targetNode: JsonNode = mapper.convertValue(target, JsonNode::class.java)
        val patchedNode: JsonNode = patch.apply(targetNode)
        return mapper.treeToValue(patchedNode, VehicleDTO::class.java)
    }

    override fun getVehicleMaintenances(
        pageable: Pageable,
        vehicleId: Long,
        vehicleLicencePlate: String?,
        defect: String?,
        completedMaintenance: Boolean?,
        date: String?
    ): Page<MaintenanceDTO> {
        logger.info(
            "Getting maintenances for vehicle $vehicleId with filters - " +
                    "licencePlate: $vehicleLicencePlate, defect: $defect, " +
                    "completedMaintenance: $completedMaintenance, date: $date"
        )

        val maintenancesList = maintenanceRepository.findWithFilters(
            pageable,
            vehicleId,
            vehicleLicencePlate,
            defect,
            completedMaintenance,
            date
        )
        logger.debug("Found ${maintenancesList.totalElements} maintenance records")
        return maintenancesList.map { it.toDTO() }
    }


    override fun getVehicleMaintenanceById(vehicleId: Long, maintenanceId: Long): MaintenanceDTO {
        logger.info("Getting maintenance $maintenanceId for vehicle $vehicleId")

        val maintenance = maintenanceRepository.findByIdOrNull(vehicleId) ?: run {
            logger.warn("Maintenance with id $vehicleId not found")
            throw MaintenanceNotFound("Maintenance not found")
        }

        return maintenance.toDTO()
    }

    override fun addVehicleMaintenance(vehicleId: Long, maintenance: MaintenanceDTO): MaintenanceDTO {
        logger.info("Adding maintenance for vehicle $vehicleId")

        val vehicle = vehicleRepository.findByIdOrNull(vehicleId) ?: run {
            logger.warn("Vehicle with id $vehicleId not found")
            throw CarModelNotFound("Vehicle with id: ${vehicleId} was not found")
        }

        vehicle.let {
            if (maintenanceRepository.existsByVehicleAndId(it, maintenance.id) == true) {
                logger.warn("Maintenance ${maintenance.id} already exists for vehicle $vehicleId")
                throw MainteinanceDuplicate("This mainteinance on vehicle ${maintenance.vehicleId} already exists")
            }

            val savedMaintenance = maintenanceRepository.save(
                MaintenanceHistory(
                    maintenance.id,
                    vehicle,
                    maintenance.defect,
                    maintenance.completedMaintenance,
                    maintenance.date?.let { it1 -> LocalDate.parse(it1) }
                )
            )

            logger.info("Successfully added maintenance for vehicle $vehicleId")
            logger.debug("Added maintenance details: $savedMaintenance")

            return savedMaintenance.toDTO()
        }
    }

    // TESTING PURPOSES
    override fun deleteAll() {
        logger.info("Deleting all vehicles, maintenances, and notes")
        noteRepository.deleteAll()
        maintenanceRepository.deleteAll()
        vehicleRepository.deleteAll()
        logger.info("Successfully deleted all vehicles, maintenances, and notes")
    }
}