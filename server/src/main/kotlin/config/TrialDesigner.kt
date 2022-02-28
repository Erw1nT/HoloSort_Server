package config

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.*
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
        pillEventType.add("")
        pillEventType.add("yellow")
        pillEventType.add("orange")
        pillEventType.add("red")
        pillEventType.add("green")
        pillEventType.add("blue")
        pillEventType.add("purple")
        pillEventType.add("pink")

    }

    private val trialID = textfield(trial.idProperty)
    private var selectedPatient = SimpleObjectProperty<PillPatient>()

    private val patientTable = tableview(trial.patientsList) {
        column("ID", PillPatient::idProperty).pctWidth(20)
        column("Name", PillPatient::nameProperty).pctWidth(20)
        bindSelected(selectedPatient)

        prefHeight = 300.0
        prefWidth = 750.0
        vgrow = Priority.ALWAYS
        smartResize()
    }

    init {
        selectedPatient.onChange {
            monday.items = selectedPatient.value?.mondayProperty
            monday.refresh()
        }
    }

    private val monday = listview(selectedPatient.value?.mondayProperty) {
        enableWhen( selectedPatient.isNotNull )

        contextmenu {
            item("Type") {
                    vbox {

                        vbox {
                            button("yellow") {
                                action {
                                    val idx = this@listview.selectionModel.selectedIndex
                                    // ex. Index 0
                                    // Add new Item at 0, prev 0 ist now 1
                                    // Remove at 0+1
                                    selectedPatient.value.monday.add(idx, "yellow")
                                    selectedPatient.value.monday.removeAt(idx + 1)
                                }
                            }
                        }
                        vbox {
                            button("orange") {
                                action {
                                    val idx = this@listview.selectionModel.selectedIndex
                                    // ex. Index 0
                                    // Add new Item at 0, prev 0 ist now 1
                                    // Remove at 0+1
                                    selectedPatient.value.monday.add(idx, "orange")
                                    selectedPatient.value.monday.removeAt(idx + 1)
                                }
                            }
                        }
                    }
                }
        }

                prefHeight = 300.0
                prefWidth = 750.0
                vgrow = Priority.ALWAYS
            }


            //add trials with +
//    private val crudForTrialsTable = CustomUIComponents.getCrudUiForTable(trialsTable) {
////        Trial()
//    }

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


                hbox {
                    label("1. Configurate Trials") {
                        font = Font(15.0)
                        padding = insets(15, 0)
                    }

//                    this += crudForTrialsTable
                }

                hbox {
                    this += patientTable
                    this += monday
                    separator { }
                }

//                button("Load Trial Configuration") {
//                    action {
//                        loadTrialConfiguration()
//                    }
//                }

                hbox {
                    hgrow = Priority.ALWAYS
                    isFillWidth = true
                    label("2. Enter Random Patient Seed") {
                        font = Font(15.0)
                        padding = insets(15, 0)
                    }
//                    spinner(0, 999, 0, 1, true, trialsConfig.patientSeedProperty, true) {
//                        prefWidth = 60.0
//
//                    }
                }
                vbox {
                    hbox {
                        hgrow = Priority.ALWAYS
                        isFillWidth = true
                        label("3. Update and verify trial configuration") {
                            font = Font(15.0)
                            padding = insets(15, 0)
                        }
                        button("Go") {
                            action {
                                verifyTrialConfiguration()
                            }
                        }
                    }
                    hbox {
                        hgrow = Priority.ALWAYS
                        isFillWidth = true
                        totalTrialField = label("    Total amount of trials   " + amountTrials) {
                            font = Font(15.0)
                            padding = insets(15, 0)
                        }
                        controlTrialField = label("Amount of control trials  " + amountControlTrials) {
                            font = Font(15.0)
                            padding = insets(15, 0)
                        }
                        interruptionTrialField = label("Amount of interruption trials  " + amountInterruptionTrials) {
                            font = Font(15.0)
                            padding = insets(15, 0)
                        }
                    }
                    hbox(20) {
                        label("     ")
                        statusArea = textarea {
                            isEditable = false
                            prefRowCount = 4
                        }
                    }
                }

//                hbox(20) {
//                    label("4. ") {
//                        font = Font(15.0)
//                        padding = insets(15, 0)
//                    }
//                    button("Save Trial Configuration").action {
//                        saveTrialConfiguration()
//                    }
//                }
            }
        }
    }

    private fun verifyTrialConfiguration() {
//        setTrialIds()
//        randomizePatients()
//        calculateAmountOfTrials()
//        trialsTable.refresh()
        statusArea.text = ""

        }



//    fun randomizePatients() {
//        val randomValues = patientIDs
//        randomValues.shuffle(Random(trialsConfig.patientSeed))
//        for (i in trialsConfig.trials) {
//            if (randomValues.isNotEmpty()) {
//                i.patient = randomValues.first().toInt()
//                randomValues.removeAt(0)
//            } else {
//                statusArea.clear()
//                statusArea.appendText("Too much Trials for available Patients")
//            }
//        }
//    }
//
//    private fun calculateAmountOfTrials() {
//        amountTrials = trialsConfig.trials.size
//        amountControlTrials = amountTrials
//
//        for (trial in trialsConfig.trials) {
//            if (trial.interruptionTrial) {
//                amountControlTrials--
//            }
//        }
//        amountInterruptionTrials = amountTrials - amountControlTrials
//        interruptionTrialField.text = "Amount of interruption trials  " + amountInterruptionTrials
//        totalTrialField.text = "Total amount of trials  " + amountTrials
//        controlTrialField.text = "Amount of control trials  " + amountControlTrials
//
//    }
//
//    private fun setTrialIds() {
//        var counter = 1
//        patientIDs.clear()
//        for (trial in trialsConfig.trials) {
//            trial.id = counter
//            counter++
//        }
//        for (counter in 1..18) {
//            patientIDs.add(Integer(counter))
//        }
//    }
//
//
//    fun loadTrialConfiguration() {
//        val file = chooseFile(
//            "Load config",
//            filters = arrayOf(FileChooser.ExtensionFilter("HMDLag trial configuration files (*.json)", "*.json"))
//        )
//        if (file.isEmpty()) return
//        val reader = BufferedReader(FileReader(file.first()))
//        val jsonString = reader.lines().collect(Collectors.joining())
//        trialsConfig.clear()
//        trialsConfig.updateModel(loadJsonObject(jsonString))
//        filePath = file.first().absolutePath
//
//    }
//
//    fun saveTrialConfiguration() {
//        verifyTrialConfiguration()
//        addTrainingsTrial()
//
//        val file = chooseFile(
//            "Save config",
//            filters = arrayOf(FileChooser.ExtensionFilter("HMDLag trial configuration files (*.json)", "*.json")),
//            mode = FileChooserMode.Save
//        )
//        if (file.isEmpty()) return
//        PrintWriter(file.first()).use { writer ->
//            filePath = file.first().absolutePath
//            trialsConfig.logDir = filePath
//            writer.write(trialsConfig.toJSON().toString())
//        }
//
//    }

}