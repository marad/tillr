package gr.marad.tiler.core

import gh.marad.tiler.common.layout.Layout
import gh.marad.tiler.common.layout.LayoutSpace
import gh.marad.tiler.tiler.internal.views.View
import gh.marad.tiler.tiler.internal.views.ViewManager
import gh.marad.tiler.common.*
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.RandomSource
import io.kotest.property.arbitrary.*
import org.junit.jupiter.api.Test

data class TestWindowId(val id: String): WindowId

class TestLayout : Layout {
    override fun retile(windows: Windows, space: LayoutSpace): Windows {
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
    Window(windowIdGen.next(rs), windowTitles.next(rs), classNames.next(rs), "exe_path", posGen.next(rs),
        isMinimized = false, isMaximized = false, isPopup = false, isActive = false)
}

//class CoreTest {
//    @Test
//    fun `should set window position if it changes`() {
//        // given
//        val aWindow = windowGen.next()
//        val updatedPosition = WindowPosition(x = 0, y = 0, width = 200, height = 100)
//
//        // when
//        val (command) = createPositionCommands(listOf(aWindow), listOf(aWindow.copy(position = updatedPosition)))
//
//        // then
//        command shouldBe SetWindowPosition(aWindow.id, updatedPosition)
//
//    }
//}


class ViewTest {
    @Test
    fun `should add window`() {
        // given
        val window = windowGen.next()
        val aView = View(layout = TestLayout())

        // when
        aView.addWindow(window.id)

        // then
        aView.hasWindow(window.id) shouldBe true
    }

    @Test
    fun `should not add window twice`() {
        // given
        val aWindow = windowGen.next()
        val aView = View(layout = TestLayout())

        // when
        aView.addWindow(aWindow.id)
        aView.addWindow(aWindow.id)

        // then
        aView.hasWindow(aWindow.id) shouldBe true
    }

    @Test
    fun `should remove window by its ID`() {
        // given
        val aWindow = windowGen.next()
        val aView = View(mutableListOf(aWindow.id), TestLayout())

        // when
        aView.removeWindow(aWindow.id)

        // then
        aView.hasWindow(aWindow.id) shouldBe false
    }
}

class ViewManagerTest {
    @Test
    fun `should allow changing the view`() {
        // given
        val aWindow = windowGen.next()
        val aViewManager = ViewManager { TestLayout() }
        val defaultView = aViewManager.currentView()

        // when
        aViewManager.currentView().addWindow(aWindow.id)
        val secondView = aViewManager.changeCurrentView(1)

        // then
        defaultView.hasWindow(aWindow.id) shouldBe true
        secondView.hasWindow(aWindow.id) shouldBe false
    }

    @Test
    fun `should allow adding one window to multiple views`() {
        // given
        val aWindow = windowGen.next()
        val aViewManager = ViewManager { TestLayout() }
        val defaultView = aViewManager.currentView()


        // when
        aViewManager.currentView().addWindow(aWindow.id)
        val secondView = aViewManager.changeCurrentView(1)
        aViewManager.currentView().addWindow(aWindow.id)

        // then
        defaultView.hasWindow(aWindow.id) shouldBe true
        secondView.hasWindow(aWindow.id) shouldBe true
        defaultView shouldNotBeEqual secondView
    }
}