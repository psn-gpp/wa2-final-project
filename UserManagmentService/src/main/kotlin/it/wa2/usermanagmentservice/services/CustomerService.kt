package it.wa2.usermanagmentservice.services

import it.wa2.usermanagmentservice.dtos.CustomerDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

interface CustomerService {
    fun getCustomers(
        pageable: Pageable,
        name: String?,
        surname: String?,
        address: String?,
        city: String?,
        dateOfBirth: Date?,
        reliabilityScores: Int?,
        drivingLicence: String?,
        expirationDate: Date?,
    ): Page<CustomerDTO>
    fun getCustomerById(userId: Long): CustomerDTO
    fun addCustomer(customer: CustomerDTO): CustomerDTO
    fun updateCustomer(userId: Long, customerDTO: CustomerDTO): CustomerDTO
    fun getEligibilityById(userId: Long): Boolean
    fun deleteCustomerById(userId: Long)
}

