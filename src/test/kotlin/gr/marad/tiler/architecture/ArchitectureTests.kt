package gr.marad.tiler.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaPackage
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.jupiter.api.Test

class ArchitectureTests {
    val classes = ClassFileImporter().importPackages("gh.marad.tiler..")
    val modules = getModules(classes)

    @Test
    fun `should not include internal classes of other modules`() {
        modules.forEach { modulePackage ->
            val rule = classes().that().resideInAPackage("${modulePackage.name}.internal..")
                .should().onlyBeAccessed().byClassesThat().resideInAPackage("${modulePackage.name}..")

            rule.check(classes)
        }
    }

    private fun getModules(classes: JavaClasses): List<JavaPackage> {
        val areInternalClasses = object : DescribedPredicate<JavaClass>("are internal classes") {
            override fun test(t: JavaClass): Boolean = t.packageName.endsWith(".internal")
        }
        return classes.that(areInternalClasses).map { it.`package`.parent.get() }.distinct()
    }
}