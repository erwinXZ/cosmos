package com.techne.cosmos.service.dto

import java.io.Serializable

/**
 * A DTO for the [com.techne.cosmos.domain.Menu] entity.
 */
data class MenuDTO(

    var id: Long? = null,

    var label: String? = null,

    var name: String? = null,

    var position: Int? = null,

    var level: Int? = null,

    var active: Boolean? = null

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MenuDTO) return false
        val menuDTO = other as MenuDTO
        if (menuDTO.id == null || id == null) {
            return false
        }
        return id == menuDTO.id
    }

    override fun hashCode() = id.hashCode()
}
