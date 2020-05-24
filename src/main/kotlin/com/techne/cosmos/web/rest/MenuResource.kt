package com.techne.cosmos.web.rest

import com.techne.cosmos.service.MenuQueryService
import com.techne.cosmos.service.MenuService
import com.techne.cosmos.service.dto.MenuCriteria
import com.techne.cosmos.service.dto.MenuDTO
import com.techne.cosmos.web.rest.errors.BadRequestAlertException
import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.PaginationUtil
import io.github.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

private const val ENTITY_NAME = "menu"
/**
 * REST controller for managing [com.techne.cosmos.domain.Menu].
 */
@RestController
@RequestMapping("/api")
class MenuResource(
    private val menuService: MenuService,
    private val menuQueryService: MenuQueryService
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /menus` : Create a new menu.
     *
     * @param menuDTO the menuDTO to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new menuDTO, or with status `400 (Bad Request)` if the menu has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/menus")
    fun createMenu(@RequestBody menuDTO: MenuDTO): ResponseEntity<MenuDTO> {
        log.debug("REST request to save Menu : {}", menuDTO)
        if (menuDTO.id != null) {
            throw BadRequestAlertException(
                "A new menu cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = menuService.save(menuDTO)
        return ResponseEntity.created(URI("/api/menus/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /menus` : Updates an existing menu.
     *
     * @param menuDTO the menuDTO to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated menuDTO,
     * or with status `400 (Bad Request)` if the menuDTO is not valid,
     * or with status `500 (Internal Server Error)` if the menuDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/menus")
    fun updateMenu(@RequestBody menuDTO: MenuDTO): ResponseEntity<MenuDTO> {
        log.debug("REST request to update Menu : {}", menuDTO)
        if (menuDTO.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = menuService.save(menuDTO)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                     menuDTO.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /menus` : get all the menus.
     *
     * @param pageable the pagination information.

     * @param criteria the criteria which the requested entities should match.
     * @return the [ResponseEntity] with status `200 (OK)` and the list of menus in body.
     */
    @GetMapping("/menus") fun getAllMenus(
        criteria: MenuCriteria,
        pageable: Pageable

    ): ResponseEntity<MutableList<MenuDTO>> {
        log.debug("REST request to get Menus by criteria: {}", criteria)
        val page = menuQueryService.findByCriteria(criteria, pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /menus/count}` : count all the menus.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the [ResponseEntity] with status `200 (OK)` and the count in body.
     */
    @GetMapping("/menus/count")
    fun countMenus(criteria: MenuCriteria): ResponseEntity<Long> {
        log.debug("REST request to count Menus by criteria: {}", criteria)
        return ResponseEntity.ok().body(menuQueryService.countByCriteria(criteria))
    }

    /**
     * `GET  /menus/:id` : get the "id" menu.
     *
     * @param id the id of the menuDTO to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the menuDTO, or with status `404 (Not Found)`.
     */
    @GetMapping("/menus/{id}")
    fun getMenu(@PathVariable id: Long): ResponseEntity<MenuDTO> {
        log.debug("REST request to get Menu : {}", id)
        val menuDTO = menuService.findOne(id)
        return ResponseUtil.wrapOrNotFound(menuDTO)
    }
    /**
     *  `DELETE  /menus/:id` : delete the "id" menu.
     *
     * @param id the id of the menuDTO to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/menus/{id}")
    fun deleteMenu(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Menu : {}", id)
        menuService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
