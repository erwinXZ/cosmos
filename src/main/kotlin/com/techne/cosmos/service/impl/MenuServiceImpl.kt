package com.techne.cosmos.service.impl

import com.techne.cosmos.domain.Menu
import com.techne.cosmos.repository.MenuRepository
import com.techne.cosmos.service.MenuService
import com.techne.cosmos.service.dto.MenuDTO
import com.techne.cosmos.service.mapper.MenuMapper
import java.util.Optional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service Implementation for managing [Menu].
 */
@Service
@Transactional
class MenuServiceImpl(
    private val menuRepository: MenuRepository,
    private val menuMapper: MenuMapper
) : MenuService {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a menu.
     *
     * @param menuDTO the entity to save.
     * @return the persisted entity.
     */
    override fun save(menuDTO: MenuDTO): MenuDTO {
        log.debug("Request to save Menu : {}", menuDTO)

        var menu = menuMapper.toEntity(menuDTO)
        menu = menuRepository.save(menu)
        return menuMapper.toDto(menu)
    }

    /**
     * Get all the menus.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<MenuDTO> {
        log.debug("Request to get all Menus")
        return menuRepository.findAll(pageable)
            .map(menuMapper::toDto)
    }

    /**
     * Get one menu by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    override fun findOne(id: Long): Optional<MenuDTO> {
        log.debug("Request to get Menu : {}", id)
        return menuRepository.findById(id)
            .map(menuMapper::toDto)
    }

    /**
     * Delete the menu by id.
     *
     * @param id the id of the entity.
     */
    override fun delete(id: Long) {
        log.debug("Request to delete Menu : {}", id)

        menuRepository.deleteById(id)
    }
}
