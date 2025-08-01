package it.wa2.reservationservice.services

import it.wa2.reservationservice.controllers.CarModelDuplicate
import it.wa2.reservationservice.controllers.CarModelIdInconsistent
import it.wa2.reservationservice.controllers.CarModelNotFound
import it.wa2.reservationservice.dtos.CarModelDTO
import it.wa2.reservationservice.dtos.CarModelFiltersDTO
//import it.wa2.reservationservice.dtos.toModel
import it.wa2.reservationservice.entities.CarModel
import it.wa2.reservationservice.entities.toDTO
import it.wa2.reservationservice.repositories.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated


@Service
@Primary
@Validated
class CarModelServiceImpl(
    private val carModelRepository: CarModelRepository,
    private val categoryRepository: CategoryRepository,
    private val engineRepository: EngineRepository,
    private val transmissionRepository: TransmissionRepository,
    private val drivetrainRepository: DrivetrainRepository,
    private val safetyFeaturesRepository: SafetyFeaturesRepository,
    private val infotainmentRepository: InfotainmentRepository
) : CarModelService {
    private val logger = LoggerFactory.getLogger(CarModelServiceImpl::class.java)

    private fun carModelDTOtoEntity(carModelDTO : CarModelDTO) = CarModel(
        id = carModelDTO.id,
        brand = carModelDTO.brand,
        model = carModelDTO.model,
        modelYear = carModelDTO.modelYear,
        segment = carModelDTO.segment,
        doorsNo = carModelDTO.doorsNo,
        seatingCapacity = carModelDTO.seatingCapacity,
        luggageCapacity = carModelDTO.luggageCapacity,
        manufacturer = carModelDTO.manufacturer,
        costPerDay = carModelDTO.costPerDay,
        motorDisplacement = carModelDTO.motorDisplacement,
        airConditioning = carModelDTO.airConditioning,
        category = categoryRepository.findByCategory(carModelDTO.category)
            ?: throw IllegalArgumentException("Category not found"),
        refEngine = engineRepository.findByType(carModelDTO.engine)
            ?: throw IllegalArgumentException("Engine type not found"),
        refTransmission = transmissionRepository.findByType(carModelDTO.transmission)
            ?: throw IllegalArgumentException("Transmission type not found"),
        refDrivetrain = drivetrainRepository.findByType(carModelDTO.drivetrain)
            ?: throw IllegalArgumentException("Drivetrain type not found"),
        safetyFeatures = carModelDTO.safetyFeatures.mapNotNull {
            safetyFeaturesRepository.findByFeature(it)
        }.toMutableSet(),
        infotainments = carModelDTO.infotainments.mapNotNull {
            infotainmentRepository.findByType(it)
        }.toMutableSet(),
    )

    override fun getFilters(): CarModelFiltersDTO {
        val categories = categoryRepository.findAll()
        val engines = engineRepository.findAll()
        val transmissions = transmissionRepository.findAll()
        val drivetrains = drivetrainRepository.findAll()
        val safetyFeatures = safetyFeaturesRepository.findAll()
        val infotainments = infotainmentRepository.findAll()
        if(categories.isEmpty() || engines.isEmpty() || transmissions.isEmpty() || drivetrains.isEmpty() || safetyFeatures.isEmpty() || infotainments.isEmpty()) {
            logger.debug("No filters found in DB")
        }
        return CarModelFiltersDTO(
            categories = categories.map { it.category }.toMutableList(),
            engines = engines.map { it.type }.toMutableList(),
            transmissions = transmissions.map { it.type }.toMutableList(),
            drivetrains = drivetrains.map { it.type }.toMutableList(),
            safetyFeatures = safetyFeatures.map { it.feature }.toMutableList(),
            infotainments = infotainments.map { it.type }.toMutableList()
        )
    }

    override fun getModels(
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
    ): Page<CarModelDTO> {
        logger.info("Get all models from DB invoked")
        val models = carModelRepository
            .findWithFilters(
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
            ).map { it.toDTO() }

        if(models.isEmpty()) {
            logger.debug("No models found in DB")
        } else {
        logger.debug("Found ${models.totalElements} models")
        }

        return models
    }

    override fun getModelById(id: Long): CarModelDTO {
        logger.info("Get a specific model by id from DB invoked. ID = $id")
        val model = carModelRepository.findByIdOrNull(id)
            ?: run {
                logger.warn("Car model with ID $id not found in DB")
                throw CarModelNotFound("Car model $id not found")
            }
        logger.debug("Found car model: {}", model)
        return model.toDTO().also {
            logger.debug("Converted car model to DTO: {}", it)
        }
    }

    /*
    * Ensure that a car with the same model name and manufacturer does not already exist.
    */
    override fun addModel(model: CarModelDTO): CarModelDTO {
        logger.debug("Attempting to add new car model: ${model.model}")
        if (carModelRepository.existsByModelAndManufacturer(model.model, model.manufacturer)) {
            logger.warn("Car model ${model.model} from ${model.manufacturer} already exists")
            throw CarModelDuplicate("Car model ${model.model} already exists")
        }
        val savedModel = carModelRepository.save(carModelDTOtoEntity(model)).toDTO()
        logger.info("Model ${model.model} created with ID: ${savedModel.id}")
        logger.debug("Created car model details: {}", savedModel)
        return savedModel
    }

    override fun modifyCarModel(carModelId: Long, model: CarModelDTO) {
        logger.debug("Attempting to modify car model with ID: $carModelId")
        logger.debug("New model data: {}", model)
        
        if (carModelId != model.id) {
            logger.warn("ID mismatch - Path ID: $carModelId, Model ID: ${model.id}")
            throw CarModelIdInconsistent("Inconsistent car model ID")
        }
        

        if (!carModelRepository.existsById(carModelId)) {
            logger.warn("Car model with ID: $carModelId not found")
            throw CarModelNotFound("Car model $carModelId does not exist")
        }

        if(carModelId != model.id) {
            throw CarModelIdInconsistent("Inconsistent car model ID")
        }

        val updatedModel = carModelRepository.save(carModelDTOtoEntity(model))
        logger.info("Model with ID: ${model.id} updated")
        logger.debug("Updated car model details: {}", updatedModel.toDTO())
    }

    override fun deleteModelById(carModelId: Long) {
        logger.debug("Attempting to delete car model with ID: $carModelId")
        if (!carModelRepository.existsById(carModelId)) {
            logger.warn("Car model with ID: $carModelId not found")
            throw CarModelNotFound("Car model $carModelId does not exist")
        }
        carModelRepository.deleteById(carModelId)
        logger.info("Model with ID: $carModelId deleted")
    }

    //aggiungere metodi per API
}