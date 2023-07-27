package gr.marad.tiler.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaPackage
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.library.GeneralCodingRules

@AnalyzeClasses(packages = ["gh.marad.tiler.."])
class ArchitectureTests {

    @ArchTest
    fun `should not include internal classes of other modules`(classes: JavaClasses) {
        getModules(classes).forEach { modulePackage ->
            val rule = classes().that().resideInAPackage("${modulePackage.name}.internal..")
                .should().onlyBeAccessed().byClassesThat().resideInAPackage("${modulePackage.name}..")

            rule.check(classes)
        }
    }

    @ArchTest
    val noGenericExceptions: ArchRule = GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS

    @ArchTest
    val noJavaUtilLogging: ArchRule = GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING

    @ArchTest
    val noJodaTime: ArchRule = GeneralCodingRules.NO_CLASSES_SHOULD_USE_JODATIME

    @ArchTest
    val noFieldInjection: ArchRule = GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION

    @ArchTest
    val noUsageOfStdStreams: ArchRule = GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS


    private fun getModules(classes: JavaClasses): List<JavaPackage> {
        val areInternalClasses = object : DescribedPredicate<JavaClass>("are internal classes") {
            override fun test(t: JavaClass): Boolean = t.packageName.endsWith(".internal")
        }
        return classes.that(areInternalClasses).map { it.`package`.parent.get() }.distinct()
    }
}