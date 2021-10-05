package test

import models.VitalSignDatum
import config.*
import javafx.application.Platform
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.paint.Color
import models.VitalSign
import tornadofx.*
import java.util.*
import kotlin.concurrent.timerTask

class TestDraggableVitalSignChart : View(), VitalSignChartChangedInterface {
    val vitalSignChart = DraggableVitalSignChart()
    val vs = VitalSign()
    val status = Label()

    override fun onDataChanged(vitalSignData: ObservableList<VitalSignDatum>) {
        val v = vitalSignData.sortedBy { it.time }
        status.text = v.toString()
    }

    init {
        vs.data.addAll(VitalSignDatum(0,10.0), VitalSignDatum(10, 100.0))
        vitalSignChart.vitalSignData = vs.data
        vitalSignChart.vitalSignChartChangedInterface = this
        vitalSignChart.updateChartFromData()

        val t = Timer(true)
        t.schedule(timerTask {
            Platform.runLater {
                vitalSignChart.addAlarmLevelAnnotation("Alarm", Color.RED, 45.0)
            }
        }, 1000)


    }

    override val root = borderpane {
        top = vbox {
            button("Add Vital Sign") {
                action {
                    vitalSignChart.vitalSignData?.add(VitalSignDatum(vitalSignChart.vitalSignData.size, vitalSignChart.vitalSignData.size.toDouble()))
                    status.text = vitalSignChart.vitalSignData.toString()
                }
            }

            this += status
            label(vitalSignChart.vitalSignDataProperty)
        }
        center = vitalSignChart
    }

}