package it.wa2.reservationservice.repositories

import it.wa2.reservationservice.entities.*
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByCategory(category: String): Category?
}

interface EngineRepository : JpaRepository<Engine, Long> {
    fun findByType(type: String): Engine?
}

interface TransmissionRepository : JpaRepository<Transmission, Long> {
    fun findByType(type: String): Transmission?
}

interface DrivetrainRepository : JpaRepository<Drivetrain, Long> {
    fun findByType(type: String): Drivetrain?
}

interface SafetyFeaturesRepository : JpaRepository<SafetyFeatures, Long> {
    fun findByFeature(feature: String): SafetyFeatures?
}

interface InfotainmentRepository : JpaRepository<Infotainment, Long> {
    fun findByType(type: String): Infotainment?
}