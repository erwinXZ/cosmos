package com.techne.cosmos.service

import com.techne.cosmos.domain.Menu
import com.techne.cosmos.domain.Menu_
import com.techne.cosmos.repository.MenuRepository
import com.techne.cosmos.service.dto.MenuCriteria
import com.techne.cosmos.service.dto.MenuDTO
import com.techne.cosmos.service.mapper.MenuMapper
import io.github.jhipster.service.QueryService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for executing complex queries for [Menu] entities in the database.
 * The main input is a [MenuCriteria] which gets converted to [Specification],
 * in a way that all the filters must apply.
 * It returns a [MutableList] of [MenuDTO] or a [Page] of [MenuDTO] which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
class MenuQueryService(
    private val menuRepository: MenuRepository,
    private val menuMapper: MenuMapper
) : QueryService<Menu>() {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Return a [MutableList] of [MenuDTO] which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    fun findByCriteria(criteria: MenuCriteria?): MutableList<MenuDTO> {
        log.debug("find by criteria : {}", criteria)
        val specification = createSpecification(criteria)
        return menuMapper.toDto(menuRepository.findAll(specification))
    }

    /**
     * Return a [Page] of [MenuDTO] which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    fun findByCriteria(criteria: MenuCriteria?, page: Pageable): Page<MenuDTO> {
        log.debug("find by criteria : {}, page: {}", criteria, page)
        val specification = createSpecification(criteria)
        return menuRepository.findAll(specification, page)
            .map(menuMapper::toDto)
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    fun countByCriteria(criteria: MenuCriteria?): Long {
        log.debug("count by criteria : {}", criteria)
        val specification = createSpecification(criteria)
        return menuRepository.count(specification)
    }

    /**
     * Function to convert [MenuCriteria] to a [Specification].
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching [Specification] of the entity.
     */
    protected fun createSpecification(criteria: MenuCriteria?): Specification<Menu?> {
        var specification: Specification<Menu?> = Specification.where(null)
        if (criteria != null) {
            if (criteria.id != null) {
                specification = specification.and(buildRangeSpecification(criteria.id, Menu_.id))
            }
            if (criteria.label != null) {
                specification = specification.and(buildStringSpecification(criteria.label, Menu_.label))
            }
            if (criteria.name != null) {
                specification = specification.and(buildStringSpecification(criteria.name, Menu_.name))
            }
            if (criteria.position != null) {
                specification = specification.and(buildRangeSpecification(criteria.position, Menu_.position))
            }
            if (criteria.level != null) {
                specification = specification.and(buildRangeSpecification(criteria.level, Menu_.level))
            }
            if (criteria.active != null) {
                specification = specification.and(buildSpecification(criteria.active, Menu_.active))
            }
        }
        return specification
    }
}
