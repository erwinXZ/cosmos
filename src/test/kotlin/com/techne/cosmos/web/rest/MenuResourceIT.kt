package com.techne.cosmos.web.rest

import com.techne.cosmos.CosmosApp
import com.techne.cosmos.domain.Menu
import com.techne.cosmos.repository.MenuRepository
import com.techne.cosmos.service.MenuQueryService
import com.techne.cosmos.service.MenuService
import com.techne.cosmos.service.mapper.MenuMapper
import com.techne.cosmos.web.rest.errors.ExceptionTranslator
import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Validator

/**
 * Integration tests for the [MenuResource] REST controller.
 *
 * @see MenuResource
 */
@SpringBootTest(classes = [CosmosApp::class])
class MenuResourceIT {

    @Autowired
    private lateinit var menuRepository: MenuRepository

    @Autowired
    private lateinit var menuMapper: MenuMapper

    @Autowired
    private lateinit var menuService: MenuService

    @Autowired
    private lateinit var menuQueryService: MenuQueryService

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var validator: Validator

    private lateinit var restMenuMockMvc: MockMvc

    private lateinit var menu: Menu

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val menuResource = MenuResource(menuService, menuQueryService)
        this.restMenuMockMvc = MockMvcBuilders.standaloneSetup(menuResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        menu = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createMenu() {
        val databaseSizeBeforeCreate = menuRepository.findAll().size

        // Create the Menu
        val menuDTO = menuMapper.toDto(menu)
        restMenuMockMvc.perform(
            post("/api/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(menuDTO))
        ).andExpect(status().isCreated)

        // Validate the Menu in the database
        val menuList = menuRepository.findAll()
        assertThat(menuList).hasSize(databaseSizeBeforeCreate + 1)
        val testMenu = menuList[menuList.size - 1]
        assertThat(testMenu.label).isEqualTo(DEFAULT_LABEL)
        assertThat(testMenu.name).isEqualTo(DEFAULT_NAME)
        assertThat(testMenu.position).isEqualTo(DEFAULT_POSITION)
        assertThat(testMenu.level).isEqualTo(DEFAULT_LEVEL)
        assertThat(testMenu.active).isEqualTo(DEFAULT_ACTIVE)
    }

    @Test
    @Transactional
    fun createMenuWithExistingId() {
        val databaseSizeBeforeCreate = menuRepository.findAll().size

        // Create the Menu with an existing ID
        menu.id = 1L
        val menuDTO = menuMapper.toDto(menu)

        // An entity with an existing ID cannot be created, so this API call must fail
        restMenuMockMvc.perform(
            post("/api/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(menuDTO))
        ).andExpect(status().isBadRequest)

        // Validate the Menu in the database
        val menuList = menuRepository.findAll()
        assertThat(menuList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    fun getAllMenus() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList
        restMenuMockMvc.perform(get("/api/menus?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(menu.id?.toInt())))
            .andExpect(jsonPath("$.[*].label").value(hasItem(DEFAULT_LABEL)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].position").value(hasItem(DEFAULT_POSITION)))
            .andExpect(jsonPath("$.[*].level").value(hasItem(DEFAULT_LEVEL)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))
    }

    @Test
    @Transactional
    fun getMenu() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        val id = menu.id
        assertNotNull(id)

        // Get the menu
        restMenuMockMvc.perform(get("/api/menus/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(id.toInt()))
            .andExpect(jsonPath("$.label").value(DEFAULT_LABEL))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.position").value(DEFAULT_POSITION))
            .andExpect(jsonPath("$.level").value(DEFAULT_LEVEL))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE))
    }

    @Test
    @Transactional
    fun getMenusByIdFiltering() {
      // Initialize the database
      menuRepository.saveAndFlush(menu)
      val id = menu.id

      defaultMenuShouldBeFound("id.equals=" + id)
      defaultMenuShouldNotBeFound("id.notEquals=" + id)

      defaultMenuShouldBeFound("id.greaterThanOrEqual=" + id)
      defaultMenuShouldNotBeFound("id.greaterThan=" + id)

      defaultMenuShouldBeFound("id.lessThanOrEqual=" + id)
      defaultMenuShouldNotBeFound("id.lessThan=" + id)
    }

    @Test
    @Transactional
    fun getAllMenusByLabelIsEqualToSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where label equals to DEFAULT_LABEL
        defaultMenuShouldBeFound("label.equals=$DEFAULT_LABEL")

        // Get all the menuList where label equals to UPDATED_LABEL
        defaultMenuShouldNotBeFound("label.equals=$UPDATED_LABEL")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByLabelIsNotEqualToSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where label not equals to DEFAULT_LABEL
        defaultMenuShouldNotBeFound("label.notEquals=" + DEFAULT_LABEL)

        // Get all the menuList where label not equals to UPDATED_LABEL
        defaultMenuShouldBeFound("label.notEquals=" + UPDATED_LABEL)
    }

    @Test
    @Transactional
    fun getAllMenusByLabelIsInShouldWork() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where label in DEFAULT_LABEL or UPDATED_LABEL
        defaultMenuShouldBeFound("label.in=$DEFAULT_LABEL,$UPDATED_LABEL")

        // Get all the menuList where label equals to UPDATED_LABEL
        defaultMenuShouldNotBeFound("label.in=$UPDATED_LABEL")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByLabelIsNullOrNotNull() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where label is not null
        defaultMenuShouldBeFound("label.specified=true")

        // Get all the menuList where label is null
        defaultMenuShouldNotBeFound("label.specified=false")
    }
                @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByLabelContainsSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where label contains DEFAULT_LABEL
        defaultMenuShouldBeFound("label.contains=" + DEFAULT_LABEL)

        // Get all the menuList where label contains UPDATED_LABEL
        defaultMenuShouldNotBeFound("label.contains=" + UPDATED_LABEL)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByLabelNotContainsSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where label does not contain DEFAULT_LABEL
        defaultMenuShouldNotBeFound("label.doesNotContain=" + DEFAULT_LABEL)

        // Get all the menuList where label does not contain UPDATED_LABEL
        defaultMenuShouldBeFound("label.doesNotContain=" + UPDATED_LABEL)
    }

    @Test
    @Transactional
    fun getAllMenusByNameIsEqualToSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where name equals to DEFAULT_NAME
        defaultMenuShouldBeFound("name.equals=$DEFAULT_NAME")

        // Get all the menuList where name equals to UPDATED_NAME
        defaultMenuShouldNotBeFound("name.equals=$UPDATED_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByNameIsNotEqualToSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where name not equals to DEFAULT_NAME
        defaultMenuShouldNotBeFound("name.notEquals=" + DEFAULT_NAME)

        // Get all the menuList where name not equals to UPDATED_NAME
        defaultMenuShouldBeFound("name.notEquals=" + UPDATED_NAME)
    }

    @Test
    @Transactional
    fun getAllMenusByNameIsInShouldWork() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where name in DEFAULT_NAME or UPDATED_NAME
        defaultMenuShouldBeFound("name.in=$DEFAULT_NAME,$UPDATED_NAME")

        // Get all the menuList where name equals to UPDATED_NAME
        defaultMenuShouldNotBeFound("name.in=$UPDATED_NAME")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByNameIsNullOrNotNull() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where name is not null
        defaultMenuShouldBeFound("name.specified=true")

        // Get all the menuList where name is null
        defaultMenuShouldNotBeFound("name.specified=false")
    }
                @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByNameContainsSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where name contains DEFAULT_NAME
        defaultMenuShouldBeFound("name.contains=" + DEFAULT_NAME)

        // Get all the menuList where name contains UPDATED_NAME
        defaultMenuShouldNotBeFound("name.contains=" + UPDATED_NAME)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByNameNotContainsSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where name does not contain DEFAULT_NAME
        defaultMenuShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME)

        // Get all the menuList where name does not contain UPDATED_NAME
        defaultMenuShouldBeFound("name.doesNotContain=" + UPDATED_NAME)
    }

    @Test
    @Transactional
    fun getAllMenusByPositionIsEqualToSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where position equals to DEFAULT_POSITION
        defaultMenuShouldBeFound("position.equals=$DEFAULT_POSITION")

        // Get all the menuList where position equals to UPDATED_POSITION
        defaultMenuShouldNotBeFound("position.equals=$UPDATED_POSITION")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByPositionIsNotEqualToSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where position not equals to DEFAULT_POSITION
        defaultMenuShouldNotBeFound("position.notEquals=" + DEFAULT_POSITION)

        // Get all the menuList where position not equals to UPDATED_POSITION
        defaultMenuShouldBeFound("position.notEquals=" + UPDATED_POSITION)
    }

    @Test
    @Transactional
    fun getAllMenusByPositionIsInShouldWork() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where position in DEFAULT_POSITION or UPDATED_POSITION
        defaultMenuShouldBeFound("position.in=$DEFAULT_POSITION,$UPDATED_POSITION")

        // Get all the menuList where position equals to UPDATED_POSITION
        defaultMenuShouldNotBeFound("position.in=$UPDATED_POSITION")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByPositionIsNullOrNotNull() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where position is not null
        defaultMenuShouldBeFound("position.specified=true")

        // Get all the menuList where position is null
        defaultMenuShouldNotBeFound("position.specified=false")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByPositionIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where position is greater than or equal to DEFAULT_POSITION
        defaultMenuShouldBeFound("position.greaterThanOrEqual=$DEFAULT_POSITION")

        // Get all the menuList where position is greater than or equal to UPDATED_POSITION
        defaultMenuShouldNotBeFound("position.greaterThanOrEqual=$UPDATED_POSITION")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByPositionIsLessThanOrEqualToSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where position is less than or equal to DEFAULT_POSITION
        defaultMenuShouldBeFound("position.lessThanOrEqual=$DEFAULT_POSITION")

        // Get all the menuList where position is less than or equal to SMALLER_POSITION
        defaultMenuShouldNotBeFound("position.lessThanOrEqual=$SMALLER_POSITION")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByPositionIsLessThanSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where position is less than DEFAULT_POSITION
        defaultMenuShouldNotBeFound("position.lessThan=$DEFAULT_POSITION")

        // Get all the menuList where position is less than UPDATED_POSITION
        defaultMenuShouldBeFound("position.lessThan=$UPDATED_POSITION")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByPositionIsGreaterThanSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where position is greater than DEFAULT_POSITION
        defaultMenuShouldNotBeFound("position.greaterThan=$DEFAULT_POSITION")

        // Get all the menuList where position is greater than SMALLER_POSITION
        defaultMenuShouldBeFound("position.greaterThan=$SMALLER_POSITION")
    }

    @Test
    @Transactional
    fun getAllMenusByLevelIsEqualToSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where level equals to DEFAULT_LEVEL
        defaultMenuShouldBeFound("level.equals=$DEFAULT_LEVEL")

        // Get all the menuList where level equals to UPDATED_LEVEL
        defaultMenuShouldNotBeFound("level.equals=$UPDATED_LEVEL")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByLevelIsNotEqualToSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where level not equals to DEFAULT_LEVEL
        defaultMenuShouldNotBeFound("level.notEquals=" + DEFAULT_LEVEL)

        // Get all the menuList where level not equals to UPDATED_LEVEL
        defaultMenuShouldBeFound("level.notEquals=" + UPDATED_LEVEL)
    }

    @Test
    @Transactional
    fun getAllMenusByLevelIsInShouldWork() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where level in DEFAULT_LEVEL or UPDATED_LEVEL
        defaultMenuShouldBeFound("level.in=$DEFAULT_LEVEL,$UPDATED_LEVEL")

        // Get all the menuList where level equals to UPDATED_LEVEL
        defaultMenuShouldNotBeFound("level.in=$UPDATED_LEVEL")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByLevelIsNullOrNotNull() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where level is not null
        defaultMenuShouldBeFound("level.specified=true")

        // Get all the menuList where level is null
        defaultMenuShouldNotBeFound("level.specified=false")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByLevelIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where level is greater than or equal to DEFAULT_LEVEL
        defaultMenuShouldBeFound("level.greaterThanOrEqual=$DEFAULT_LEVEL")

        // Get all the menuList where level is greater than or equal to UPDATED_LEVEL
        defaultMenuShouldNotBeFound("level.greaterThanOrEqual=$UPDATED_LEVEL")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByLevelIsLessThanOrEqualToSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where level is less than or equal to DEFAULT_LEVEL
        defaultMenuShouldBeFound("level.lessThanOrEqual=$DEFAULT_LEVEL")

        // Get all the menuList where level is less than or equal to SMALLER_LEVEL
        defaultMenuShouldNotBeFound("level.lessThanOrEqual=$SMALLER_LEVEL")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByLevelIsLessThanSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where level is less than DEFAULT_LEVEL
        defaultMenuShouldNotBeFound("level.lessThan=$DEFAULT_LEVEL")

        // Get all the menuList where level is less than UPDATED_LEVEL
        defaultMenuShouldBeFound("level.lessThan=$UPDATED_LEVEL")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByLevelIsGreaterThanSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where level is greater than DEFAULT_LEVEL
        defaultMenuShouldNotBeFound("level.greaterThan=$DEFAULT_LEVEL")

        // Get all the menuList where level is greater than SMALLER_LEVEL
        defaultMenuShouldBeFound("level.greaterThan=$SMALLER_LEVEL")
    }

    @Test
    @Transactional
    fun getAllMenusByActiveIsEqualToSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where active equals to DEFAULT_ACTIVE
        defaultMenuShouldBeFound("active.equals=$DEFAULT_ACTIVE")

        // Get all the menuList where active equals to UPDATED_ACTIVE
        defaultMenuShouldNotBeFound("active.equals=$UPDATED_ACTIVE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByActiveIsNotEqualToSomething() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where active not equals to DEFAULT_ACTIVE
        defaultMenuShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE)

        // Get all the menuList where active not equals to UPDATED_ACTIVE
        defaultMenuShouldBeFound("active.notEquals=" + UPDATED_ACTIVE)
    }

    @Test
    @Transactional
    fun getAllMenusByActiveIsInShouldWork() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultMenuShouldBeFound("active.in=$DEFAULT_ACTIVE,$UPDATED_ACTIVE")

        // Get all the menuList where active equals to UPDATED_ACTIVE
        defaultMenuShouldNotBeFound("active.in=$UPDATED_ACTIVE")
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllMenusByActiveIsNullOrNotNull() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        // Get all the menuList where active is not null
        defaultMenuShouldBeFound("active.specified=true")

        // Get all the menuList where active is null
        defaultMenuShouldNotBeFound("active.specified=false")
    }

    /**
     * Executes the search, and checks that the default entity is returned
     */
    private fun defaultMenuShouldBeFound(filter: String) {
        restMenuMockMvc.perform(get("/api/menus?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(menu.id?.toInt())))
            .andExpect(jsonPath("$.[*].label").value(hasItem(DEFAULT_LABEL)))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].position").value(hasItem(DEFAULT_POSITION)))
            .andExpect(jsonPath("$.[*].level").value(hasItem(DEFAULT_LEVEL)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))

        // Check, that the count call also returns 1
        restMenuMockMvc.perform(get("/api/menus/count?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"))
    }

    /**
     * Executes the search, and checks that the default entity is not returned
     */
    private fun defaultMenuShouldNotBeFound(filter: String) {
        restMenuMockMvc.perform(get("/api/menus?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)

        // Check, that the count call also returns 0
        restMenuMockMvc.perform(get("/api/menus/count?sort=id,desc&$filter"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"))
    }

    @Test
    @Transactional
    fun getNonExistingMenu() {
        // Get the menu
        restMenuMockMvc.perform(get("/api/menus/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateMenu() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        val databaseSizeBeforeUpdate = menuRepository.findAll().size

        // Update the menu
        val id = menu.id
        assertNotNull(id)
        val updatedMenu = menuRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedMenu are not directly saved in db
        em.detach(updatedMenu)
        updatedMenu.label = UPDATED_LABEL
        updatedMenu.name = UPDATED_NAME
        updatedMenu.position = UPDATED_POSITION
        updatedMenu.level = UPDATED_LEVEL
        updatedMenu.active = UPDATED_ACTIVE
        val menuDTO = menuMapper.toDto(updatedMenu)

        restMenuMockMvc.perform(
            put("/api/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(menuDTO))
        ).andExpect(status().isOk)

        // Validate the Menu in the database
        val menuList = menuRepository.findAll()
        assertThat(menuList).hasSize(databaseSizeBeforeUpdate)
        val testMenu = menuList[menuList.size - 1]
        assertThat(testMenu.label).isEqualTo(UPDATED_LABEL)
        assertThat(testMenu.name).isEqualTo(UPDATED_NAME)
        assertThat(testMenu.position).isEqualTo(UPDATED_POSITION)
        assertThat(testMenu.level).isEqualTo(UPDATED_LEVEL)
        assertThat(testMenu.active).isEqualTo(UPDATED_ACTIVE)
    }

    @Test
    @Transactional
    fun updateNonExistingMenu() {
        val databaseSizeBeforeUpdate = menuRepository.findAll().size

        // Create the Menu
        val menuDTO = menuMapper.toDto(menu)

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMenuMockMvc.perform(
            put("/api/menus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(menuDTO))
        ).andExpect(status().isBadRequest)

        // Validate the Menu in the database
        val menuList = menuRepository.findAll()
        assertThat(menuList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    fun deleteMenu() {
        // Initialize the database
        menuRepository.saveAndFlush(menu)

        val databaseSizeBeforeDelete = menuRepository.findAll().size

        val id = menu.id
        assertNotNull(id)

        // Delete the menu
        restMenuMockMvc.perform(
            delete("/api/menus/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val menuList = menuRepository.findAll()
        assertThat(menuList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_LABEL = "AAAAAAAAAA"
        private const val UPDATED_LABEL = "BBBBBBBBBB"

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"

        private const val DEFAULT_POSITION: Int = 1
        private const val UPDATED_POSITION: Int = 2
        private const val SMALLER_POSITION: Int = 1 - 1

        private const val DEFAULT_LEVEL: Int = 1
        private const val UPDATED_LEVEL: Int = 2
        private const val SMALLER_LEVEL: Int = 1 - 1

        private const val DEFAULT_ACTIVE: Boolean = false
        private const val UPDATED_ACTIVE: Boolean = true

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Menu {
            val menu = Menu(
                label = DEFAULT_LABEL,
                name = DEFAULT_NAME,
                position = DEFAULT_POSITION,
                level = DEFAULT_LEVEL,
                active = DEFAULT_ACTIVE
            )

            return menu
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Menu {
            val menu = Menu(
                label = UPDATED_LABEL,
                name = UPDATED_NAME,
                position = UPDATED_POSITION,
                level = UPDATED_LEVEL,
                active = UPDATED_ACTIVE
            )

            return menu
        }
    }
}
