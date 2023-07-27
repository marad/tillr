package gr.marad.tiler.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaPackage
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes

@AnalyzeClasses(packages = ["gh.marad.tiler.."])
class TestModule {

    @ArchTest
    fun `should not include internal classes of other modules`(classes: JavaClasses) {
        getModules(classes).forEach { modulePackage ->
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