package gr.marad.tiler.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaPackage
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.*
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all
import com.tngtech.archunit.library.GeneralCodingRules
import kotlin.jvm.optionals.getOrNull

@AnalyzeClasses(packages = ["gh.marad.tiler.."])
class ArchitectureTests {
    private val blocks = BlockTransformer()
    private val beFreeOfCycles = BlocksFreeOfCyclesCondition()
    private val restrictAccessToInternalClasses = BlocksRestrictAccessToInternalClassesCondition()

    @ArchTest
    val blocksRestrictAccessToInternalClasses: ArchRule = all(blocks).should(restrictAccessToInternalClasses)

    @ArchTest
    val blocksShouldBeFreeOfCycles: ArchRule = all(blocks).should(beFreeOfCycles)

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

class BlocksFreeOfCyclesCondition : ArchCondition<Block>("be free of cycles") {
    override fun check(block: Block, events: ConditionEvents) {
        val queue = ArrayDeque<List<Block>>()
        queue.addFirst(listOf(block))

        while (true) {
            val currentPath = queue.removeFirstOrNull() ?: break

            if (currentPath.size > currentPath.distinctBy { it.javaPackage.name }.size) {
                events.add(SimpleConditionEvent.violated(currentPath, "dependency cycle detected: ${currentPath.map { it.javaPackage.name }}"))
                break
            }

            currentPath.last().dependenciesToThisBlock().forEach {
                queue.add(currentPath.plus(it.origin))
            }
        }
    }
}

class BlocksRestrictAccessToInternalClassesCondition : ArchCondition<Block>("restrict access to internal classes") {
    override fun check(item: Block, events: ConditionEvents) {
        val blockClasses = item.javaPackage.classes
        item.internalPackage().classDependenciesToThisPackageTree.forEach {
            if(!blockClasses.contains(it.originClass)) {
                events.add(SimpleConditionEvent.violated(it, it.description))
            }
        }
    }

}

data class Block(val javaPackage: JavaPackage) {
    val blockParentPackage = javaPackage.parent.get()

    fun internalClasses(): Set<JavaClass> = internalPackage().classesInPackageTree

    fun internalPackage() = println(javaPackage).let { javaPackage.getPackage("internal") }

    fun dependenciesFromThisBlock(): Set<BlockDependency> {
        return javaPackage.classDependenciesFromThisPackageTree
            .asSequence()
            .mapNotNull { findBlockPackage(it.targetClass.`package`) }.distinct()
            .map { BlockDependency(this, Block(it)) }
            .toSet()
    }

    fun dependenciesToThisBlock(): Set<BlockDependency> {
        return javaPackage.classDependenciesToThisPackageTree
            .asSequence()
            .mapNotNull { findBlockPackage(it.originClass.`package`) }.distinct()
            .map { BlockDependency(Block(it), this) }
            .toSet()
    }

    private fun isMainBlockPackage(javaPackage: JavaPackage): Boolean =
        javaPackage.subpackages.any { it.relativeName == "internal" }
                && javaPackage.classes.any { it.simpleName.endsWith("Facade") }

    /// Returns block root package or null if given package does not belong to a block.
    fun findBlockPackage(javaPackage: JavaPackage): JavaPackage? {
        if (isMainBlockPackage(javaPackage)) return javaPackage
        if (!javaPackage.name.contains(".internal")) return null
        var currentPackage = javaPackage
        while(true) {
            if (currentPackage.name.endsWith(".internal")) break
            currentPackage = currentPackage.parent.getOrNull() ?: return null
        }
        val potentialBlockPackage = currentPackage.parent.getOrNull() ?: return null
        if (isMainBlockPackage(potentialBlockPackage)) return potentialBlockPackage
        return null
    }
}

data class BlockDependency(val origin: Block, val target: Block)

class BlockTransformer : AbstractClassesTransformer<Block>("blocks") {
    private val areInternalClasses = object : DescribedPredicate<JavaClass>("are internal classes") {
        override fun test(t: JavaClass): Boolean = t.packageName.endsWith(".internal")
    }

    override fun doTransform(classes: JavaClasses): MutableIterable<Block> {
        val blockPackages = classes.that(areInternalClasses)
            .map { it.`package`.parent.get() }.distinct().map { it.name }

        return blockPackages.map {
             Block(classes.getPackage(it))
        }.toMutableSet()
    }
}