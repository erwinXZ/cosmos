package com.techne.cosmos.domain

import java.io.Serializable
import javax.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

/**
 * A Menu.
 */
@Entity
@Table(name = "menu")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
data class Menu(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null,
    @Column(name = "label")
    var label: String? = null,

    @Column(name = "name")
    var name: String? = null,

    @Column(name = "position")
    var position: Int? = null,

    @Column(name = "level")
    var level: Int? = null,

    @Column(name = "active")
    var active: Boolean? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Menu) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Menu{" +
        "id=$id" +
        ", label='$label'" +
        ", name='$name'" +
        ", position=$position" +
        ", level=$level" +
        ", active='$active'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
