package it.wa2.reservationservice.dtos

data class CarModelFiltersDTO (
    val categories: MutableList<String> = mutableListOf(),
    val engines: MutableList<String> = mutableListOf(),
    val transmissions: MutableList<String> = mutableListOf(),
    val drivetrains: MutableList<String> = mutableListOf(),
    val safetyFeatures : MutableList<String> = mutableListOf(),
    val infotainments: MutableList<String> = mutableListOf(),
)