package com.techne.cosmos

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class ArchTest {

    @Test
    fun servicesAndRepositoriesShouldNotDependOnWebLayer() {

        val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.techne.cosmos")

        noClasses()
            .that()
                .resideInAnyPackage("com.techne.cosmos.service..")
            .or()
                .resideInAnyPackage("com.techne.cosmos.repository..")
            .should().dependOnClassesThat()
                .resideInAnyPackage("..com.techne.cosmos.web..")
        .because("Services and repositories should not depend on web layer")
        .check(importedClasses)
    }
}
