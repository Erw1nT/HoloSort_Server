package config

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import javafx.stage.FileChooser
import models.*
import models.enums.HololensCueType
import mpmgame.PatientFilter

import tornadofx.*
import utils.CustomUIComponents
import java.io.BufferedReader
import java.io.FileReader
import java.io.PrintWriter
import java.util.stream.Collectors
import kotlin.random.Random


class TrialDesigner : AbstractTrialDesigner<Trial>(Trial()) {


    protected val amountTrialsProperty = SimpleIntegerProperty(0)
    private var amountTrials by amountTrialsProperty

    protected val amountControlTrialsProperty = SimpleIntegerProperty(0)
    private var amountControlTrials by amountControlTrialsProperty

    protected val amountInterruptionTrialsProperty = SimpleIntegerProperty(0)
    private var amountInterruptionTrials by amountInterruptionTrialsProperty

    var interruptionTrialField: Label by singleAssign()

    var controlTrialField: Label by singleAssign()

    var totalTrialField: Label by singleAssign()

    lateinit var statusArea: TextArea

    private val pillEventType: ObservableList<String> = FXCollections.observableArrayList<String>()

    init {

        trial = Trial()
        trial.patientsList.add(PillPatient(1, "JB"))
        trial.patientsList.add(PillPatient(2, "LT"))
        trial.patientsList.add(PillPatient(3, "KS"))

        //set interruption lengths for dropdowns
        pillEventType.add("yellow")
        pillEventType.add("orange")
        pillEventType.add("red")
        pillEventType.add("green")
        pillEventType.add("blue")
        pillEventType.add("purple")
        pillEventType.add("pink")
        pillEventType.add("short")
        pillEventType.add("long")

    }


    private var selectedPatient = SimpleObjectProperty<PillPatient>()

    private val patientTable = tableview(trial.patientsList) {
        column("ID", PillPatient::idProperty).pctWidth(20).makeEditable()
        column("Name", PillPatient::nameProperty).pctWidth(20).makeEditable()
        bindSelected(selectedPatient)

        prefHeight = 300.0
        prefWidth = 500.0
        vgrow = Priority.ALWAYS
        smartResize()
    }

    private val listViews: MutableList<ListView<String>> = arrayListOf()

    private fun createListViewOfDay(prop: SimpleListProperty<String>?): ListView<String> {
        val lw = listview(prop) {
            enableWhen(selectedPatient.isNotNull)

            contextmenu {
                item("Type") {
                    vbox() {
                        for (pillType in pillEventType) {
                            button(pillType) {
                                maxWidth = 50.0
                                useMaxWidth = true

                                action {
                                    val idx = this@listview.selectionModel.selectedIndex
                                    // ex. Index 0
                                    // Add new Item at 0, prev 0 ist now 1
                                    // Remove at 0+1

                                    this@listview.items.add(idx, pillType)
                                    this@listview.items.removeAt(idx + 1)
                                }

                            }
                        }
                    }
                }
            }

            prefHeight = 300.0
            prefWidth = 75.0
            vgrow = Priority.ALWAYS
        }

        return lw
    }

    //add trials with +
    private val crudForTrialsTable = CustomUIComponents.getCrudUiForTable(patientTable) {
        PillPatient(0, "Name")
    }

    private fun getCrudForListView(lw: ListView<String>): HBox {
        return CustomUIComponents.getCrudUiForList(lw) { pillEventType.first() }
    }

    private fun getDayLabelByIndex(index: Int): Label {
        val result = when (index) {
            0 -> "Monday"
            1 -> "Tuesday"
            2 -> "Wednesday"
            3 -> "Thursday"
            4 -> "Friday"
            5 -> "Saturday"
            6 -> "Sunday"
            else -> ""
        }

        return label(result)
    }

    override val root = borderpane() {
        title = "Trial Designer"
        vgrow = Priority.ALWAYS
        minWidth = 800.0

        center = scrollpane {
            isFitToWidth = true
            hgrow = Priority.ALWAYS
            prefHeight = 680.0
            vbox(20) {
                hgrow = Priority.ALWAYS
                isFillWidth = true
                paddingAll = 10

                vbox{
                    label("1. Configurate Trial") {
                        font = Font(15.0)
                        padding = insets(15, 0)
                    }

                    label("Trial ID")
                    textfield(trial.idProperty) {
                        maxWidth = 30.0
                        useMaxWidth = true
                    }

                }

                this += crudForTrialsTable
                label ( "Patients" )

                hbox {
                    this += patientTable
                    separator { }

                    val days = getDayProperties()

                    for (idx in days.indices) {
                        // die days-Liste ist am Anfang 7xnull, aber fürs Initialisieren reichts :D
                        val day = days[idx]

                        vbox {
                            val lw = createListViewOfDay(day)
                            this += getDayLabelByIndex(idx)

                            this += getCrudForListView(lw)
                            this += lw
                            separator { }
                            listViews.add(lw)

                        }
                    }
                }

                hbox(20) {
                    label("2. Save & Load") {
                        font = Font(15.0)
                        padding = insets(15, 0)
                    }
                    button("Save Trial Configuration").action {
                        saveTrialConfiguration()
                    }

                    button("Load Trial Configuration") {
                        action {
                            loadTrialConfiguration()
                        }
                    }
                }
            }
        }
    }

    private fun getDayProperties(): List<SimpleListProperty<String>?> {
        return listOf(
            selectedPatient.value?.mondayProperty,
            selectedPatient.value?.tuesdayProperty,
            selectedPatient.value?.wednesdayProperty,
            selectedPatient.value?.thursdayProperty,
            selectedPatient.value?.fridayProperty,
            selectedPatient.value?.saturdayProperty,
            selectedPatient.value?.sundayProperty
        )
    }

    init {
        selectedPatient.onChange {

            val days = getDayProperties()

            for (day in days.indices) {
                listViews[day].items = days[day]
            }

        }
    }

    private fun loadTrialConfiguration() {
        val file = chooseFile(
            "Load config",
            filters = arrayOf(FileChooser.ExtensionFilter("Pill-Exp trial files (*.json)", "*.json"))
        )
        if (file.isEmpty()) return

        val reader = BufferedReader(FileReader(file.first()))
        val jsonString = reader.lines().collect(Collectors.joining())

        trial.updateModel(loadJsonObject(jsonString))
        patientTable.items = trial.patientsList
        selectedPatient.markDirty()

        filePath = file.first().absolutePath

    }

    private fun saveTrialConfiguration() {

        val file = chooseFile(
            "Save config",
            filters = arrayOf(FileChooser.ExtensionFilter("Pill-Exp trial files (*.json)", "*.json")),
            mode = FileChooserMode.Save
        )

        if (file.isEmpty()) return

        PrintWriter(file.first()).use { writer ->
            filePath = file.first().absolutePath
            //trialsConfig.logDir = filePath
            writer.write(trial.toJSON().toString())
        }

    }

}