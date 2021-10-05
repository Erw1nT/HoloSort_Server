package test
import tornadofx.*

class TestingSplitPane : View() {
    override val root = splitpane {
        scrollpane {
            pane {
                minWidth = 400.0
                label("Left Pane")
            }
        }
        scrollpane {
            pane {
                minWidth = 400.0
                label("Right Pane")
            }
        }
    }
}

class TestingSplitPaneApp: App(TestingSplitPane::class)

fun main(args: Array<String>) {
    launch<TestingSplitPaneApp>()
}