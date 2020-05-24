package com.techne.cosmos.service.dto

import io.github.jhipster.service.Criteria
import io.github.jhipster.service.filter.BooleanFilter
import io.github.jhipster.service.filter.IntegerFilter
import io.github.jhipster.service.filter.LongFilter
import io.github.jhipster.service.filter.StringFilter
import java.io.Serializable

/**
 * Criteria class for the [com.techne.cosmos.domain.Menu] entity. This class is used in
 * [com.techne.cosmos.web.rest.MenuResource] to receive all the possible filtering options from the
 * Http GET request parameters.
 * For example the following could be a valid request:
 * ```/menus?id.greaterThan=5&attr1.contains=something&attr2.specified=false```
 * As Spring is unable to properly convert the types, unless specific [Filter] class are used, we need to use
 * fix type specific filters.
 */
data class MenuCriteria(

    var id: LongFilter? = null,

    var label: StringFilter? = null,

    var name: StringFilter? = null,

    var position: IntegerFilter? = null,

    var level: IntegerFilter? = null,

    var active: BooleanFilter? = null
) : Serializable, Criteria {

    constructor(other: MenuCriteria) :
        this(
            other.id?.copy(),
            other.label?.copy(),
            other.name?.copy(),
            other.position?.copy(),
            other.level?.copy(),
            other.active?.copy()
        )

    override fun copy() = MenuCriteria(this)

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
