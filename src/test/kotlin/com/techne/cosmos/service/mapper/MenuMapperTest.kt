package com.techne.cosmos.service.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MenuMapperTest {

    private lateinit var menuMapper: MenuMapper

    @BeforeEach
    fun setUp() {
        menuMapper = MenuMapperImpl()
    }

    @Test
    fun testEntityFromId() {
        val id = 1L
        assertThat(menuMapper.fromId(id)?.id).isEqualTo(id)
        assertThat(menuMapper.fromId(null)).isNull()
    }
}
