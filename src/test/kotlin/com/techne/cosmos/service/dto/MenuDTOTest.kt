package com.techne.cosmos.service.dto

import com.techne.cosmos.web.rest.equalsVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MenuDTOTest {

    @Test
    fun dtoEqualsVerifier() {
        equalsVerifier(MenuDTO::class)
        val menuDTO1 = MenuDTO()
        menuDTO1.id = 1L
        val menuDTO2 = MenuDTO()
        assertThat(menuDTO1).isNotEqualTo(menuDTO2)
        menuDTO2.id = menuDTO1.id
        assertThat(menuDTO1).isEqualTo(menuDTO2)
        menuDTO2.id = 2L
        assertThat(menuDTO1).isNotEqualTo(menuDTO2)
        menuDTO1.id = null
        assertThat(menuDTO1).isNotEqualTo(menuDTO2)
    }
}
