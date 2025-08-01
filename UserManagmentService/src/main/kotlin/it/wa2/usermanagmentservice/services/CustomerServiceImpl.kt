package it.wa2.usermanagmentservice.services

import it.wa2.usermanagmentservice.advices.CustomerDuplicate
import it.wa2.usermanagmentservice.advices.CustomerIdInconsistent
import it.wa2.usermanagmentservice.advices.CustomerNotFound
import it.wa2.usermanagmentservice.dtos.CustomerDTO
import it.wa2.usermanagmentservice.entities.Customer
import it.wa2.usermanagmentservice.repositories.CustomerRepository
import it.wa2.usermanagmentservice.repositories.GenericUserRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.*


/**
 * [getCustomers]: return all customers
 *
 * [getCustomerById]: return the customer with a specified id
 *
 * [addCustomer]: Add a customer and return this
 *
 * [updateCustomer]: Performs the update of specify customer and return the updated one
 *
 * [getEligibilityById]: Performs a check about the eligibility of the specified customer
 *
 * [deleteCustomerById]: Performs delete of specify customer
 */
@Service
@Primary
@Validated
@Transactional
class CustomerServiceImpl(private val customerRepository: CustomerRepository, private val genericUserRepository: GenericUserRepository) : CustomerService {

    private val logger = LoggerFactory.getLogger(CustomerServiceImpl::class.java)


    /**
     * Verify duplication about: driving licence, email and phone.
     *
     * If a customer with one of this attribute is already present, throw an [CustomerDuplicate] exception
     */
    private fun checkDuplicate(customerDTO: CustomerDTO) {
        logger.debug("Checking for duplicates: ${customerDTO.genericUserData.email}")
        val currentCustomer = customerRepository.findByIdOrNull(customerDTO.genericUserData.id)

        if(currentCustomer?.drivingLicense != customerDTO.drivingLicence && customerRepository.existsByDrivingLicense(customerDTO.drivingLicence)) {
            logger.warn("Duplicate driving license found: ${customerDTO.drivingLicence}")
            throw CustomerDuplicate("A customer with driving license number ${customerDTO.drivingLicence} already exists.")
        }
        if(currentCustomer?.genericUserData?.email != customerDTO.genericUserData.email && genericUserRepository.existsByEmail(customerDTO.genericUserData.email)) {
            logger.warn("Duplicate email found: ${customerDTO.genericUserData.email}")
            throw CustomerDuplicate("A customer with email ${customerDTO.genericUserData.email} already exists.")
        }
/*        if(currentCustomer?.genericUserData?.phone != customerDTO.genericUserData.phone && genericUserRepository.existsByPhone(customerDTO.genericUserData.phone)) {
            logger.warn("Duplicate phone found: ${customerDTO.genericUserData.phone}")
            throw CustomerDuplicate("A customer with phone ${customerDTO.genericUserData.phone} already exists.")
        }*/
    }

    /**
     * Verify if the age is bigger than 18 yo
     */
    private fun isAdult(dateOfBirth: Date): Boolean {
        logger.debug("Checking for age")
        val calendar = Calendar.getInstance()
        calendar.time = dateOfBirth
        calendar.add(Calendar.YEAR, 18)
        return calendar.time.before(Date()) || calendar.time == Date()
    }

    /**
     * Verify that the customer is of legal age, the expiration date is after the current date and his score is higher than a threshold
     */
    private fun isEligible(customer: Customer): Boolean{
        logger.debug("Checking for eligibility: isAdult=${isAdult(customer.dateOfBirth)}, expirationDate=${customer.expirationDate.after(Date())}, reliabilityScores=${customer.reliabilityScores > 3}")
        return isAdult(customer.dateOfBirth) && customer.expirationDate.after(Date()) && customer.reliabilityScores > 3
    }

    /**
     * Return all customers
     */
    override fun getCustomers(
        pageable: Pageable,
        name: String?,
        surname: String?,
        address: String?,
        city: String?,
        dateOfBirth: Date?,
        reliabilityScores: Int?,
        drivingLicence: String?,
        expirationDate: Date?,
    ): Page<CustomerDTO> {
        logger.debug("Fetching customers with filters")
        return customerRepository.findWithFilters(
            pageable,
            name,
            surname,
            address,
            city,
            dateOfBirth,
            reliabilityScores,
            drivingLicence,
            expirationDate
        ).map { it.toDTO() }
    }

    /**
     *  Return the customer with a specified id or throw [CustomerNotFound] exception
     */
    override fun getCustomerById(userId: Long): CustomerDTO {
        logger.debug("Fetching customer with id: $userId")
        return customerRepository.findByIdOrNull(userId)?.toDTO()
            ?: run {
                logger.warn("Customer not found with id: $userId")
                throw CustomerNotFound("Customer with $userId not found")
            }
    }

    /**
     * Add a customer and return this or throw [CustomerDuplicate] exception if a customer with the same drivingLicence or email or phone number is already present
     */
    override fun addCustomer(customer: CustomerDTO): CustomerDTO {
        logger.info("Adding new customer: ${customer.genericUserData.email}")
        checkDuplicate(customer)
        val savedCustomer = customerRepository.save(customer.toEntity()).toDTO()
        logger.info("Customer added successfully with ID: ${savedCustomer.genericUserData.id}")
        return savedCustomer
    }

    /**
     * Performs the update of specify customer and return the updated one
     *
     * If [customerDTO] has an id differt to [userId] throw [CustomerIdInconsistent] exception
     *
     * If [userId] is not present throw [CustomerNotFound] exception
     *
     */
    override fun updateCustomer(userId: Long, customerDTO: CustomerDTO): CustomerDTO {
        logger.info("Updating customer with id: $userId")
        if (userId != customerDTO.genericUserData.id) {
            logger.error("id mismatch: path id=$userId, body id=${customerDTO.genericUserData.id}")
            throw CustomerIdInconsistent("Inconsistent customer id ${customerDTO.id}")
        }

        checkDuplicate(customerDTO)

        val customer = customerRepository.findByIdOrNull(userId) ?: run {
            logger.warn("Customer not found with id: $userId")
            throw CustomerNotFound("Customer with $userId not found")
        }
        customer.update(customerDTO.toEntity())
        val updatedCustomer = customerRepository.save(customer).toDTO()
        logger.info("Customer $userId updated successfully")
        return updatedCustomer
    }

    /**
     * Performs a check about the eligibility of the custermer with [userId]
     */

    override fun getEligibilityById(userId: Long): Boolean {
        val customer = customerRepository.findByIdOrNull(userId) ?: throw CustomerNotFound("Customer with $userId not found")
        return isEligible(customer)
    }

    /**
     * Performs delete of specify customer
     *
     * if [userId] is not present throw [CustomerNotFound] exception
     */
    override fun deleteCustomerById(userId: Long) {
        logger.info("Attempting to delete customer with id: $userId")
        if (!customerRepository.existsById(userId)) {
            logger.warn("Cannot delete - customer not found with id: $userId")
            throw CustomerNotFound("Customer with $userId not found")
        }
        customerRepository.deleteById(userId)
        logger.info("Customer $userId deleted successfully")
    }
}

