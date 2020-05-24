package com.techne.cosmos.service
import com.techne.cosmos.service.dto.MenuDTO
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Service Interface for managing [com.techne.cosmos.domain.Menu].
 */
interface MenuService {

    /**
     * Save a menu.
     *
     * @param menuDTO the entity to save.
     * @return the persisted entity.
     */
    fun save(menuDTO: MenuDTO): MenuDTO

    /**
     * Get all the menus.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    fun findAll(pageable: Pageable): Page<MenuDTO>

    /**
     * Get the "id" menu.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: Long): Optional<MenuDTO>

    /**
     * Delete the "id" menu.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long)
}
