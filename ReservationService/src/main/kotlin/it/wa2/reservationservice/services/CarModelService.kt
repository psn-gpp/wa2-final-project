package it.wa2.reservationservice.services

import it.wa2.reservationservice.dtos.CarModelDTO
import it.wa2.reservationservice.dtos.CarModelFiltersDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CarModelService {
    fun getModels(
        pageable: Pageable,
        brand: String?,
        model: String?,
        modelYear: Int?,
        segment: String?,
        doorsNo: Int?,
        seatingCapacity: Int?,
        luggageCapacity: Float?,
        category: String?,
        manufacturer: String?,
        engine: String?,
        transmission: String?,
        drivetrain: String?,
        safetyFeatures: List<String>?,
        infotainments: List<String>?
    ): Page<CarModelDTO>
    fun getModelById(id: Long): CarModelDTO
    fun addModel(model: CarModelDTO): CarModelDTO
    fun modifyCarModel(carModelId: Long, model: CarModelDTO)
    fun deleteModelById(carModelId: Long)

    fun getFilters() : CarModelFiltersDTO
}