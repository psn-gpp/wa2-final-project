package it.wa2.reservationservice.dtos

data class VehicleFiltersDTO(
    val availabilities: MutableList<String> = mutableListOf()
)
