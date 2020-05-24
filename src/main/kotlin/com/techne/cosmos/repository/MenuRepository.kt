package com.techne.cosmos.repository

import com.techne.cosmos.domain.Menu
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [Menu] entity.
 */
@Suppress("unused")
@Repository
interface MenuRepository : JpaRepository<Menu, Long>, JpaSpecificationExecutor<Menu>
