package com.techne.cosmos.domain

import com.techne.cosmos.web.rest.equalsVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MenuTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Menu::class)
        val menu1 = Menu()
        menu1.id = 1L
        val menu2 = Menu()
        menu2.id = menu1.id
        assertThat(menu1).isEqualTo(menu2)
        menu2.id = 2L
        assertThat(menu1).isNotEqualTo(menu2)
        menu1.id = null
        assertThat(menu1).isNotEqualTo(menu2)
    }
}
