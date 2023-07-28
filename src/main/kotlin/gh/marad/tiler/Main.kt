package gh.marad.tiler

import gh.marad.tiler.actions.ActionsFacade
import gh.marad.tiler.app.AppFacade
import gh.marad.tiler.config.ConfigFacade
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.tiler.TilerFacade

// TODO ignore admin windows https://github.com/marad/tillr/issues/1 (https://stackoverflow.com/a/24144277)
// TODO GH Actions CI/CD
// TODO installation script
// TODO handle fullscreen windows
// TODO window showing registered hotkeys
// TODO status toolbar showing current desktop
// TODO [maybe] widgets for status toolbar
// TODO default view assignments for windows (ie. WhatsApp -> view 1)


/**
 * Notatki o wykorzystaniu KScript
 * Aktualne API wydaje się być trochę bałaganiarskie. Tworzenie poszczególnych elementów
 * jest zagmatwane i nieliniowe.
 *
 * Chciałbym aby użytkownik API mógł w prosty sposób skonfigurować to co dla niego istotne:
 * - jakie okna mają być ignorowane/zarządzane (reguły filtrowania)
 * - skróty klawiszowe
 * - używany layout - idealnie jeśli użytkownik mógłby użyć jakiś swój
 * - potencjalnie mógłby też chcieć zapiąć się na eventy okien lub z tilera
 *
 * Wydaje się, że żeby to umożliwić trzeba utworzyć jakieś jedno spójne API,
 * które będzie interfejsem dla użytkownika do konfiguracji tych wszystkich rzeczy.
 */

fun main() {
    val config = ConfigFacade.createConfig()
    val os = OsFacade.createWindowsFacade()
    val filteringRules = config.getFilteringRules()
    val tiler = TilerFacade.createTiler(config, filteringRules, os)
    val actions = ActionsFacade.createActions()
    val app = AppFacade.createWindowsApp(config, os, tiler, actions)
    app.start(filteringRules)
}
