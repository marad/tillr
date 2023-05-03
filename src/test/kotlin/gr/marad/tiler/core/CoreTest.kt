package gr.marad.tiler.core

import gh.marad.tiler.core.*
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.*
import org.junit.jupiter.api.Test

data class TestWindowId(val id: String): WindowId
class TestWindowManager(private val windows: List<Window>): WindowManager {
    override fun listWindows(): List<Window> = windows
}

class TestLayout : Layout {
    override fun updateSpace(space: LayoutSpace) {
        TODO("Not yet implemented")
    }

    override fun retile(windows: Windows): Windows {
        TODO("Not implemented")
    }
}

val testIdGen = Arb.uuid().map {
    val fullUuid = it.toString()
    fullUuid.substring(fullUuid.lastIndexOf('-') + 1)
}
val windowTitles = Arb.stringPattern("window-\\d{1,3}")
val classNames = Arb.stringPattern("class-\\d{1,3}")
val xGen = Arb.int(0, 1280)
val yGen = Arb.int(0, 720)
val wGen = Arb.int(100, 1000)
val hGen = Arb.int(100, 800)
val windowIdGen = arbitrary { rs: RandomSource ->
    TestWindowId(testIdGen.next(rs))
}
val posGen = arbitrary { rs: RandomSource ->
    WindowPosition(
        x = xGen.next(rs),
        y = yGen.next(rs),
        width = wGen.next(rs),
        height = hGen.next(rs)
    )
}

val windowGen = arbitrary { rs ->
    Window(windowIdGen.next(rs), windowTitles.next(rs), classNames.next(rs), posGen.next(rs))
}

fun window(title: String? = null, className: String? = null, position: WindowPosition? = null): Window {
    val finalTitle = title ?: windowTitles.next()
    val finalClassName = className ?: classNames.next()
    val finalPosition = position ?: posGen.next()
    return Window(windowIdGen.next(), finalTitle, finalClassName, finalPosition)
}

fun wmWith(vararg windows: Window): WindowManager = TestWindowManager(windows.toList())
fun wmWith(windows: List<Window>): WindowManager = TestWindowManager(windows)

class CoreTest {
    @Test
    fun `should generate commands for showing and hiding windows in view`() {
        // given
        val windowListGen = Arb.list(windowGen, 1..20)
        val windowsInView = windowListGen.next()
        val otherWindows = windowListGen.next()
        val allWindows = windowsInView + otherWindows

        // when
        val commands = activate(allWindows, windowsInView)

        // then
        for (window in windowsInView) {
            commands shouldContain SetWindowPosition(window.id, window.position)
        }

        for (window in otherWindows) {
            commands shouldContain MinimizeWindow(window.id)
        }
    }

    @Test
    fun `should set window position if it changes`() {
        // given
        val aWindow = windowGen.next()
        val updatedPosition = WindowPosition(x = 0, y = 0, width = 200, height = 100)

        // when
        val (command) = calcWindowMovements(listOf(aWindow), listOf(aWindow.copy(position = updatedPosition)))

        // then
        command shouldBe SetWindowPosition(aWindow.id, updatedPosition)

    }
}


class ViewTest {
    @Test
    fun `should add window`() {
        // given
        val aWindow = windowGen.next()
        val aView = View(layout = TestLayout())

        // when
        aView.addWindow(aWindow)

        // then
        aView shouldContain aWindow
    }

    @Test
    fun `should not add window twice`() {
        // given
        val aWindow = windowGen.next()
        val aView = View(layout = TestLayout())

        // when
        aView.addWindow(aWindow)
        aView.addWindow(aWindow)

        // then
        aView shouldHaveSize 1
    }

    @Test
    fun `should remove window by its ID`() {
        // given
        val aWindow = windowGen.next()
        val aView = View(mutableListOf(aWindow), TestLayout())

        // when
        aView.removeWindow(aWindow.id)

        // then
        aView shouldHaveSize 0
    }
}

class ViewManagerTest {
    @Test
    fun `should add window to current view`() {
        // given
        val aWindow = windowGen.next()
        val aViewManager = ViewManager(TestLayout())

        // when
        aViewManager.addWindow(aWindow)

        // then
        aViewManager.currentViewWindows() shouldContain aWindow
    }

    @Test
    fun `should allow removing the window by ID`() {
        // given
        val aWindow = windowGen.next()
        val aViewManager = ViewManager(TestLayout())
        aViewManager.addWindow(aWindow)

        // when
        aViewManager.removeWindow(aWindow.id)

        // then
        aViewManager.currentViewWindows() shouldHaveSize 0
    }

    @Test
    fun `should allow changing the view`() {
        // given
        val aWindow = windowGen.next()
        val aViewManager = ViewManager(TestLayout())

        // when
        aViewManager.addWindow(aWindow)
        aViewManager.changeCurrentView(1)

        // then
        aViewManager.currentViewWindows() shouldNotContain aWindow
        aViewManager.changeCurrentView(0)
        aViewManager.currentViewWindows() shouldContain aWindow
    }

    @Test
    fun `should allow adding one window to multiple views`() {
        // given
        val aWindow = windowGen.next()
        val aViewManager = ViewManager(TestLayout())

        // when
        aViewManager.addWindow(aWindow)
        aViewManager.changeCurrentView(1)
        aViewManager.addWindow(aWindow)

        // then
        aViewManager.currentViewWindows() shouldContain aWindow
        aViewManager.changeCurrentView(0)
        aViewManager.currentViewWindows() shouldContain aWindow
    }
}