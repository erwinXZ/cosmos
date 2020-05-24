package com.techne.cosmos.service.mapper

import com.techne.cosmos.domain.Menu
import com.techne.cosmos.service.dto.MenuDTO
import org.mapstruct.Mapper

/**
 * Mapper for the entity [Menu] and its DTO [MenuDTO].
 */
@Mapper(componentModel = "spring", uses = [])
interface MenuMapper :
    EntityMapper<MenuDTO, Menu> {

    override fun toEntity(menuDTO: MenuDTO): Menu

    @JvmDefault
    fun fromId(id: Long?) = id?.let {
        val menu = Menu()
        menu.id = id
        menu
    }
}
