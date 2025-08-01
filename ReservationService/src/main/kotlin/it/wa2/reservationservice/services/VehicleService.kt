package it.wa2.reservationservice.services

import com.github.fge.jsonpatch.JsonPatch
import it.wa2.reservationservice.dtos.CarModelDTO
import it.wa2.reservationservice.dtos.MaintenanceDTO
import it.wa2.reservationservice.dtos.NoteDTO
import it.wa2.reservationservice.dtos.VehicleDTO
import it.wa2.reservationservice.dtos.VehicleFiltersDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import java.time.LocalDate

interface VehicleService {
    fun modifyVehicleMaintenance(vehicleId: Long, maintenanceDTO: MaintenanceDTO)
    fun getVehicleNotes(vehicleId:Long,
                        pageable: Pageable,
                        startDate: LocalDate?,
                        endDate: LocalDate?,
                        author:String?): Page<NoteDTO>
    fun addVehicleNote(vehicleId:Long, noteDTO:NoteDTO) : NoteDTO

    fun getVehicles(pageable:Pageable, refCarModel:Long?, availability:String?, licencePlate:String?, vin:String?, kilometers:Float?, pendingCleaning:Boolean?, pendingMaintenance:Boolean? ) : Page<VehicleDTO>
    fun getVehicleById(vehicleId: Long): VehicleDTO
    fun addVehicle(vehicleDTO: VehicleDTO): VehicleDTO
    fun modifyVehicle(vehicleId: Long, vehicleDTO: VehicleDTO)
    fun deleteVehicleById(vehicleId: Long)
    fun modifyVehicleNote(vehicleId: Long, noteId: Long, noteDTO: NoteDTO)
    fun deleteVehicleNote(vehicleId: Long, noteId: Long)
    fun patchVehicleById(carId : Long, patch : JsonPatch): ResponseEntity<VehicleDTO>
    fun getVehicleMaintenances(
        pageable: Pageable,
        vehicleId: Long,
        vehicleLicencePlate: String?,
        defect: String?,
        completedMaintenance: Boolean?,
        date: String?
    ): Page<MaintenanceDTO>
    fun getVehicleMaintenanceById(vehicleId: Long, maintenanceId : Long): MaintenanceDTO
    fun addVehicleMaintenance(vehicleId : Long, maintenance : MaintenanceDTO):MaintenanceDTO

    fun getFilters(): VehicleFiltersDTO

    fun deleteAll()     // testing purpose
}