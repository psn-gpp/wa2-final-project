package it.wa2.usermanagmentservice.repositories

import it.wa2.usermanagmentservice.entities.Employee
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface EmployeeRepository : JpaRepository<Employee, Long>{

    @Query(
        """
        SELECT e FROM Employee e
        WHERE (?1 IS NULL OR e.genericUserData.name = ?1)
        AND (?2 IS NULL OR e.genericUserData.surname = ?2) 
        AND (?3 IS NULL OR e.genericUserData.address = ?3) 
        AND (?4 IS NULL OR e.genericUserData.city = ?4) 
        AND (?5 IS NULL OR e.role.nameRole = ?5) 
        AND (?6 IS NULL OR e.salary = ?6)
     """
    )
    fun findWithFilters(
        pageable: Pageable,
        name: String?,
        surname: String?,
        address: String?,
        city: String?,
        nameRole: String?,
        salary: Double?
    ): Page<Employee>
}

