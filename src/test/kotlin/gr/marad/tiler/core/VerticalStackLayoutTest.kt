package gr.marad.tiler.core

import gh.marad.tiler.core.layout.LayoutSpace
import gh.marad.tiler.core.layout.VerticalStackLayout
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import org.junit.jupiter.api.Test

class VerticalStackLayoutTest {
    @Test
    fun `should stack windows vertically`() {
        // given
        val windowListGen = Arb.list(windowGen, 1..10)
        val someWindows = windowListGen.next()
        val aVerticalStackLayout = VerticalStackLayout()

        // when
        val positionedWindows = aVerticalStackLayout.retile(someWindows, LayoutSpace(0, 0, 800, 600))

        // then all positioned windows should have the same dimensions
        positionedWindows.forEach {
            it.position.x shouldBeExactly 0
            it.position.width shouldBeExactly 800
            it.position.height shouldBeExactly (600 / someWindows.size)
        }

        // and each window should be lower than the previous window
        positionedWindows.windowed(2, 1) {(previousWindow, window) ->
            previousWindow.position.y shouldBeLessThanOrEqual window.position.y
        }
    }
}