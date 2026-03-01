package org.kurron.hard.parts.order

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class ArchitectureFitnessTests {

    @Test
    fun `order package does not depend directly on payment package`() {
        val imported = ClassFileImporter().importPackages("org.kurron.hard.parts")

        noClasses()
            .that().resideInAPackage("org.kurron.hard.parts.order..")
            .should().dependOnClassesThat().resideInAPackage("org.kurron.hard.parts.payment..")
            .check(imported)
    }
}
