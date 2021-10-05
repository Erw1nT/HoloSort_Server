package test

import tornadofx.View
import tornadofx.vbox
import views.MonitorView

class TestMonitorView: View() {
    val m = MonitorView()

    init {
        m.vitalSignLabels = arrayOf("BP", "HR")
        m.patientName = "P-0"
        m.gridPane.minHeight = 400.0
    }

    override val root = vbox {
        add(m)
    }
}