package views

import generators.MONITOR_COLOR_MAPPING
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.GridPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import generators.MONITOR_MAIN_FONT_SIZE
import generators.MONITOR_SIDE_FONT_SIZE
import javafx.beans.property.SimpleIntegerProperty
import kotlin.math.min
import kotlin.properties.Delegates
import tornadofx.*

class MonitorView : Parent()
{
    val gridPane = GridPane()

    var vitalSignLabels : Array<String> by Delegates.observable(emptyArray()) { _, _, newValue ->
        initView(newValue)
    }

    val vitalSignData = mutableListOf<Label>()
    val vitalSignUpper = mutableListOf<Label>()
    val vitalSignLower = mutableListOf<Label>()

    var uppers: Array<Double> = emptyArray()
    var lowers: Array<Double> = emptyArray()

    val alarmLevelProperty = SimpleIntegerProperty(0)
    var alarmLevel by alarmLevelProperty

    val patientNameLabel = Label("P-0")
    var patientName : String by Delegates.observable("P-0")
    {
        _, _, newValue ->
        patientNameLabel.text = newValue
    }

    init {
        this.children.addAll(gridPane)
        gridPane.background = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))
        gridPane.setPrefSize(160.0,(vitalSignLabels.size+1) * 25.0)
    }

    fun initView(labels : Array<String>)
    {
        this.gridPane.children.clear()
        vitalSignData.clear()
        vitalSignUpper.clear()
        vitalSignLower.clear()
        gridPane.add(patientNameLabel, 0, 0, 3, 2)
        patientNameLabel.textFill = Color.WHITE
        patientNameLabel.font = Font(MONITOR_MAIN_FONT_SIZE)
        patientNameLabel.alignment = Pos.CENTER
        patientNameLabel.textAlignment = TextAlignment.CENTER
        patientNameLabel.setPrefSize(150.0, 24.0)

        var i = 2
        labels.forEach {
            val textColour = MONITOR_COLOR_MAPPING.getOrDefault(it, Color.RED)
            val lbl = Label(it)
            lbl.font = Font(MONITOR_MAIN_FONT_SIZE)
            lbl.textFill = textColour
            lbl.setPrefSize(75.0, 24.0)
            gridPane.add(lbl, 0, i, 1, 2)
            val v = Label("999")
            v.font = Font(MONITOR_MAIN_FONT_SIZE)
            v.textFill = textColour
            v.setPrefSize(45.0, 24.0)
            gridPane.add(v, 1, i, 1, 2)
            vitalSignData.add(v)
            val u = Label("999")
            u.font = Font(MONITOR_SIDE_FONT_SIZE)
            u.textFill = textColour
            gridPane.add(u, 2, i++, 1, 1)
            vitalSignUpper.add(u)
            val l = Label("999")
            l.font = Font(MONITOR_SIDE_FONT_SIZE)
            l.textFill = textColour
            gridPane.add(l, 2, i++, 1, 1)
            vitalSignLower.add(l)
        }
    }

    fun setValue(id: Int, value: Double)
    {
        Platform.runLater {
            vitalSignData[id].text = "${value.toInt()}"
            val invParams = shouldInvertAndIsUpper(id, value)
            invertColours(id, invParams.first, invParams.second)
        }
    }

    fun setAlarms(uppers: Array<Double> = emptyArray(), lowers: Array<Double> = emptyArray())
    {
        this.uppers = uppers
        this.lowers = lowers

        for (i in 0 until min(uppers.size, vitalSignUpper.size))
            vitalSignUpper[i].text = "${uppers[i].toInt()}"

        for (i in 0 until min(lowers.size, vitalSignLower.size))
            vitalSignLower[i].text = "${lowers[i].toInt()}"
    }

    fun shouldInvertAndIsUpper(id: Int, value: Double) : Pair<Boolean, Boolean> // shouldInvert, isUpper
    {
        if (id >= lowers.size || id >= uppers.size || id < 0) return Pair(false, false)
        if (value < lowers[id]) return Pair(true, false)
        if (value > uppers[id]) return Pair(true, true)
        return Pair(false, false)
    }

    fun invertColours(id: Int, isAlarm: Boolean, isUpper: Boolean)
    {
        if (id < 0 || id >= vitalSignLabels.size) return
        val usualTextColor = MONITOR_COLOR_MAPPING.getOrDefault(vitalSignLabels[id], Color.RED)
        val usualBgColor = Color.BLACK

        val textColor = if (isAlarm) {usualBgColor} else {usualTextColor}
        val bgColor = if (isAlarm) {usualTextColor} else {usualBgColor}

        vitalSignData[id].textFill = textColor
        vitalSignData[id].background = Background(BackgroundFill(bgColor, CornerRadii.EMPTY, Insets.EMPTY))

        val limitLabel = if (isUpper) {vitalSignUpper[id]} else {vitalSignLower[id]}
        limitLabel.textFill = textColor
        limitLabel.background = Background(BackgroundFill(bgColor, CornerRadii.EMPTY, Insets.EMPTY))

    }
}





























