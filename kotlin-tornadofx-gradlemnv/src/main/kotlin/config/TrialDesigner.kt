package config

import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import javafx.stage.FileChooser
import models.*

import tornadofx.*
import utils.CustomUIComponents
import java.io.BufferedReader
import java.io.FileReader
import java.io.PrintWriter
import java.util.stream.Collectors
import kotlin.random.Random


class TrialDesigner : AbstractTrialDesigner<Trial>(TrialsConfiguration(Trial::class.java)) {

    protected val amountTrialsProperty = SimpleIntegerProperty(0)
    private var amountTrials by amountTrialsProperty

    protected val amountControlTrialsProperty = SimpleIntegerProperty(0)
    private var amountControlTrials by amountControlTrialsProperty

    protected val amountInterruptionTrialsProperty = SimpleIntegerProperty(0)
    private var amountInterruptionTrials by amountInterruptionTrialsProperty

    protected val amountsichtbarkeitProperty = SimpleIntegerProperty(0)
    private var amountsichtbarkeit by amountsichtbarkeitProperty

    var interruptionTrialField: Label by singleAssign()

    var controlTrialField: Label by singleAssign()

    var totalTrialField: Label by singleAssign()

    lateinit var statusArea: TextArea

    private val interruptionLengths: ObservableList<Number> = FXCollections.observableArrayList<Number>()

    val patientIDs =  mutableListOf<Integer>()


    init {
        trialsConfig.trials.clear()

        //set interruption lengths for dropdowns
        interruptionLengths.add(0, 0)
        interruptionLengths.add(1, 15)
        interruptionLengths.add(2, 30)
        interruptionLengths.add(3, 45)

    }



    private val trialsTable = tableview(trialsConfig.trials) {
        column("Trial ID", Trial::idProperty).pctWidth(10)
        column("Interruption Trial", Trial::interruptionTrialProperty).useCheckbox().pctWidth(10)
        column("Visible Main Task", Trial::sichtbarkeitProperty).useCheckbox().pctWidth(10)
        column("Patient ID", Trial::patientProperty).pctWidth(20)
        column("Patient Information", Trial::patientInformationProperty).useComboBox(interruptionLengths)
            .pctWidth(20)
        column("Medication", Trial::medicationProperty).useComboBox(interruptionLengths).pctWidth(20)
        column("Respiratory", Trial::respiratoryProperty).useComboBox(interruptionLengths).pctWidth(20)
        column("Catheter", Trial::catheterProperty).useComboBox(interruptionLengths).pctWidth(20)
        column("Tube", Trial::tubeProperty).useComboBox(interruptionLengths).pctWidth(20)
        column("Positioning", Trial::positioningProperty).useComboBox(interruptionLengths).pctWidth(20)
        prefHeight = 300.0
        prefWidth = 750.0
        vgrow = Priority.ALWAYS
        smartResize()
    }


    //add trials with +
    private val crudForTrialsTable = CustomUIComponents.getCrudUiForTable(trialsTable) {
        Trial()
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


                hbox {
                    label("1. Configurate Trials") {
                        font = Font(15.0)
                        padding = insets(15, 0)
                    }

                    this += crudForTrialsTable
                }

                hbox {
                    this += trialsTable
                    separator { }
                }

                button("Load Trial Configuration") {
                    action {
                        loadTrialConfiguration()
                    }
                }

                hbox {
                    hgrow = Priority.ALWAYS
                    isFillWidth = true
                    label("2. Enter Random Patient Seed") {
                        font = Font(15.0)
                        padding = insets(15, 0)
                    }
                    spinner(0, 999, 0, 1, true, trialsConfig.patientSeedProperty, true) {
                        prefWidth = 60.0

                    }
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

                hbox(20) {
                    label("4. ") {
                        font = Font(15.0)
                        padding = insets(15, 0)
                    }
                    button("Save Trial Configuration").action {
                        saveTrialConfiguration()
                    }
                }
            }
        }
    }

    private fun verifyTrialConfiguration() {
        setTrialIds()
        randomizePatients()
        calculateAmountOfTrials()
        trialsTable.refresh()
        statusArea.text = ""

        System.out.println(trialsConfig.trials.toString())

        for (trial in trialsConfig.trials) {
            if (trial.interruptionTrial) {
                if (trial.patientInformation == 0 && trial.medication == 0 && trial.respiratory == 0 && trial.catheter == 0 && trial.tube == 0 && trial.positioning == 0) {
                    statusArea.text += "Trial " + trial.id + ": enter interruption lengths" + "\n"
                }
            } else {

                if (trial.patientInformation != 0 || trial.medication != 0 || trial.respiratory != 0 || trial.catheter != 0 || trial.tube != 0 || trial.positioning != 0) {
                    statusArea.text += "Trial " + trial.id + ": set interruption lengths to 0 " + "\n"
                }
            }
        }
    }


    fun randomizePatients() {
        val randomValues = patientIDs
        randomValues.shuffle(Random(trialsConfig.patientSeed))
        for (i in trialsConfig.trials) {
            if (randomValues.isNotEmpty()) {
                i.patient = randomValues.first().toInt()
                randomValues.removeAt(0)
            } else {
                statusArea.clear()
                statusArea.appendText("Too much Trials for available Patients")
            }
        }
    }

    private fun calculateAmountOfTrials() {
        amountTrials = trialsConfig.trials.size
        amountControlTrials = amountTrials

        for (trial in trialsConfig.trials) {
            if (trial.interruptionTrial) {
                amountControlTrials--
            }
        }
        amountInterruptionTrials = amountTrials - amountControlTrials
        interruptionTrialField.text = "Amount of interruption trials  " + amountInterruptionTrials
        totalTrialField.text = "Total amount of trials  " + amountTrials
        controlTrialField.text = "Amount of control trials  " + amountControlTrials

    }

    private fun setTrialIds() {
        var counter = 1
        patientIDs.clear()
        for (trial in trialsConfig.trials) {
            trial.id = counter
            counter++
        }
        for (counter in 1..18) {
            patientIDs.add(Integer(counter))
        }
    }


    fun loadTrialConfiguration() {
        val file = chooseFile(
            "Load config",
            filters = arrayOf(FileChooser.ExtensionFilter("HMDLag trial configuration files (*.json)", "*.json"))
        )
        if (file.isEmpty()) return
        val reader = BufferedReader(FileReader(file.first()))
        val jsonString = reader.lines().collect(Collectors.joining())
        trialsConfig.clear()
        trialsConfig.updateModel(loadJsonObject(jsonString))
        filePath = file.first().absolutePath

    }

    fun saveTrialConfiguration() {
        verifyTrialConfiguration()
        addTrainingsTrial()

        val file = chooseFile(
            "Save config",
            filters = arrayOf(FileChooser.ExtensionFilter("HMDLag trial configuration files (*.json)", "*.json")),
            mode = FileChooserMode.Save
        )
        if (file.isEmpty()) return
        PrintWriter(file.first()).use { writer ->
            filePath = file.first().absolutePath
            trialsConfig.logDir = filePath
            writer.write(trialsConfig.toJSON().toString())
        }

    }

    fun addTrainingsTrial() {
        var trainingsTrial = Trial()
        trainingsTrial.id = 100
        trainingsTrial.interruptionTrial = true
        trainingsTrial.sichtbarkeit = false
        trainingsTrial.patient = 100
        trainingsTrial.patientInformation = 15
        trainingsTrial.catheter = 45
        trainingsTrial.medication = 15
        trainingsTrial.positioning = 30

        trialsConfig.trainingsTrial = trainingsTrial
    }

}