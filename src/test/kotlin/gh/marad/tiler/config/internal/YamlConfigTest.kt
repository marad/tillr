package gh.marad.tiler.config.internal

import gh.marad.tiler.actions.SwitchView
import gh.marad.tiler.common.Window
import gh.marad.tiler.common.WindowId
import gh.marad.tiler.common.WindowPosition
import gh.marad.tiler.common.layout.GapLayoutDecorator
import gh.marad.tiler.common.layout.TwoColumnLayout
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class YamlConfigTest {
    @Test
    fun `should load layout config from yaml`() {
        val config = YamlConfig()
        config.loadConfig { this::class.java.getResourceAsStream("/sampleConfig.yaml") }

        val layout = config.createLayout()

        layout.shouldBeTypeOf<GapLayoutDecorator>().should {
            it.gapSize shouldBe 20
            it.wrappedLayout.shouldBeTypeOf<TwoColumnLayout>().should {
                it.getRatio() shouldBe 0.55f
            }
        }
    }

    @Test
    fun `should load filtering rules from yaml`() {
        val config = YamlConfig()
        config.loadConfig { this::class.java.getResourceAsStream("/sampleConfig.yaml") }

        val rules = config.getFilteringRules()

        rules.shouldManage(sampleWindow(title = "WhatsApp")).shouldBeTrue()
        rules.shouldManage(sampleWindow(title = "Microsoft To Do", className = "ApplicationFrameWindow")).shouldBeTrue()
        rules.shouldManage(sampleWindow(className = "ApplicationFrameTitleBarWindow")).shouldBeFalse()
    }


    @Test
    fun `should load hotkeys from yaml`() {
        val config = YamlConfig()
        config.loadConfig { this::class.java.getResourceAsStream("/sampleConfig.yaml") }

        val hotkeys = config.getHotkeys()

        hotkeys.size shouldBe 15
        hotkeys[0].should {
            it.action.shouldBeTypeOf<SwitchView>().should {
                it.viewId shouldBe 0
            }
            it.key shouldBe "S-A-C-U"
        }
    }

    object TestWindowId : WindowId
    fun sampleWindow(title: String = "sample name", className: String = "sample class",
                     exePath: String = "c:\\sample\\path.exe") = Window(
        id = TestWindowId,
        className = className,
        windowName = title,
        exePath = exePath,
        position = WindowPosition(0, 0, 0, 0),
        isMinimized = false,
        isPopup = false,
        isMaximized = false
    )
}