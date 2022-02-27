package config

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.*
import javafx.collections.ObservableList
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.text.Font
import javafx.stage.FileChooser
import javafx.stage.Stage
import logging.GlobalLogger
import logging.LogFormat
import models.*
import models.enums.HololensCueType
import java.util.*
import org.json.JSONObject
import publisher.Publisher
import tornadofx.*
import utils.HMDLagUserPrefsManager
import utils.UI
import utils.areYouSure
import java.io.*
import java.lang.Exception
import java.util.stream.Collectors
import publisher.Subscriber
import java.io.File
import java.text.SimpleDateFormat

class ExperimentRunner : AbstractTrialDesigner<Trial>(TrialsConfiguration(Trial::class.java)) {

    val expConfigProperty = SimpleObjectProperty(ExperimentConfiguration())
    var expConfiguration by expConfigProperty

    lateinit var statusArea: TextArea
    lateinit var startExpButton: Button

    private var caliField : Field? = null

    private var blockEditor: Stage? = null

    init {
        expConfiguration.outputDirectory = HMDLagUserPrefsManager.loadPrefs(
            HMDLagUserPrefsManager.OUTPUT_DIRECTORY_KEY,
            System.getProperty("user.home")
        )

        expConfiguration.deviceProperty.onChange {
            if (expConfiguration.device == "Tablet"){
                caliField?.setDisable(true)
            } else{
                caliField?.setDisable(false)
            }
        }

        expConfiguration.interruptionTask = "arithmetic"
        expConfiguration.deviceProperty.value = "Tablet"
    }

    //Menu bar
    protected val menu = menubar {
        menu("File...") {
            item("_New...") {
                action { if (areYouSure()) newConfig() }
                this.accelerator = KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN)
            }
            item("_Open...") {
                action { if (areYouSure()) loadConfigurations() }
                this.accelerator = KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN)
            }
            item("_Save") {
                action { saveConfig() }
                this.accelerator = KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN)
            }
            //not implemented
            item("Save _As...") {
                action { saveAs() }
                this.accelerator =
                    KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
            }
            separator()
            item("_Close") {
                action {
                    if (areYouSure()) close()
                }
                this.accelerator = KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN)
            }
        }
    }

    //structure
    override val root = vbox(20) {
        borderpane {
            top = menu
        }

        vbox(20) {
            paddingAll = 10
            label("Experiment Runner") {
                font = Font.font(20.0)
            }
            label("HoloLag version: 2021-12-21") {
                font = Font.font(12.0)
            }

            form {
                fieldset("Experiment Block") {
                    field("Enter Participant Number") {
                        spinner(0, 999, 0, 1, true, expConfiguration.participantNumProperty, true)
                    }

                    field("Load Trial Configuration") {
                        button("Load Trial File").action {
                            loadTrialConfig()
                        }

                        button("Create Trial File").action {
                            blockEditor = UI.openWindow(blockEditor, if (blockEditor == null) TrialDesigner() else null)
                        }
                    }

                    field("Output Directory") {
                        textfield(expConfiguration.outputDirectoryProperty) {
                            isEditable = false
                        }
                        button("Select Output Directory").action {

                            val previousFile = File(expConfiguration.outputDirectory)
                            val initialDir = if (previousFile.exists()) previousFile else null

                            val file = chooseDirectory("Choose output directory", initialDirectory = initialDir)
                            expConfiguration.outputDirectory = file?.absolutePath ?: expConfiguration.outputDirectory
                            HMDLagUserPrefsManager.savePrefs(
                                HMDLagUserPrefsManager.OUTPUT_DIRECTORY_KEY,
                                expConfiguration.outputDirectory
                            )
                            statusArea.clear()
                            statusArea.appendText(expConfiguration.outputDirectory)
                        }
                    }

                    field("Select Device") {

                        // to show that the Tablet condition is enabled by default this checkbox is shown
                        checkbox("Tablet") {
                            isSelected = (expConfiguration.deviceProperty.value == "Tablet")
                            isDisable = true
                        }
                    }

                    field("Select Interruption Task"){
                        vbox{
                            // to show that arithmetic is enabled by default it is not removed from the UI
                            checkbox("Arithmetic") {
                                isSelected = (expConfiguration.interruptionTask == "arithmetic")
                                isDisable = true
                            }
                        }
                    }

                    field("Select Hololens Cue Type"){
                        val toggleGroup = ToggleGroup()
                        vbox {
                            radiobutton("None", toggleGroup) {
                                action {
                                    expConfiguration.hololensCueType = HololensCueType.NONE.identifier
                                }
                            }
                            radiobutton("Automatic", toggleGroup) {
                                action {
                                    expConfiguration.hololensCueType = HololensCueType.AUTOMATIC.identifier
                                }
                            }
                            radiobutton("Manual", toggleGroup) {
                                action {
                                    expConfiguration.hololensCueType = HololensCueType.MANUAL.identifier
                                }
                            }
                        }
                    }

                }
            }

            hbox(20) {
                button("Verify config").action {
                    verifyConfig()
                }

                startExpButton = button("Start ▶") {
                    isDisable = true
                    action {
                        sendPatientAndTrialInformation()
                        createLogger()
                    }
                }
            }

            hbox(20) {
                label("Status:")
                statusArea = textarea {
                    isEditable = false
                    prefRowCount = 4
                }
            }
        }
    }

    fun createLogger() {
        GlobalLogger.resetExpLog()
        GlobalLogger.exp(File(expConfiguration.outputDirectory,
            "Log_P${expConfiguration.participantNumber}_${SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(Date())}.csv").
        absolutePath, LogFormat.CSV)

        GlobalLogger.exp().clearColumns()
        GlobalLogger.exp().addColumns(arrayOf("Participant Number", "Block", "Device", "Hololens Cue Type", "Interruption Trial", "Trial", "First Click In Module", "INTEGER: First Click In Module", "Wrong Click In Module After Interruption", "Patient ID", "Module", "Error Wrong Module", "Error Input", "Error Empty Module", "Error Count Interruption", "Interruption Length", "Click on OK", "INTEGER: Click on OK", "Start Time Interruption", "INTEGER: Start Time Interruption", "End Time Interruption", "INTEGER: End Time Interruption", "Cue Set Duration", "First Focus", "First Focus Int"))
        GlobalLogger.exp().writerHeader()

    }

    //functions
    private fun sendPatientAndTrialInformation() {
        createFixedPatients()

        if (trialsConfig.trials.size > patients.size) {
            statusArea.clear()
            statusArea.appendText("Please create more patients manually")
        }

        if (!Publisher.isNetworkingActiveAndRunning()) {
            statusArea.clear()
            statusArea.appendText("Network is not running")
        }

        val subscribers = Publisher.getSubscribers()

        if (subscribers.size == 1) {
            statusArea.clear()
            statusArea.appendText("No subscriber connected")
        }
        else
        {
            val frontend = subscribers.values.singleOrNull{ it.name == "frontend" }
            if(frontend !== null)
            {
                val objFrontend = JSONObject()
                objFrontend.put("type", "expDataHMD")
                objFrontend.put("training", expConfiguration.trainingIncluded)
                objFrontend.put("interruptionTask", expConfiguration.interruptionTask)

                Publisher.sendMessage(objFrontend, frontend)
            }

            val lens = subscribers.values.singleOrNull { it.name == "lens" }
            if (lens !== null)
            {
                val jsonObj = JSONObject()
                jsonObj.put("type", "expData")
                jsonObj.put("hololensCueType", expConfiguration.hololensCueType)
                jsonObj.put("patients", patients)

                Publisher.sendMessage(jsonObj, lens)
            }

            statusArea.clear()
            statusArea.appendText("Experiment data was sent successfully")
        }

    }

    fun loadTrialConfig() {
        return

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
        expConfiguration.trialsConfig = trialsConfig

        statusArea.clear()
        statusArea.appendText("Loaded trialConfig!")

        if (expConfiguration.trialsConfig.logDir != file.first().absolutePath) {
            statusArea.clear()
            statusArea.appendText("[!] ${expConfiguration.trialsConfig.patientSeed}: File location in config is different to actual file location... updating location to:\n")
            expConfiguration.trialsConfig.logDir = file.first().absolutePath
            statusArea.appendText(file.first().absolutePath)
        }

    }

    //TODO save should not open file manager when saving it for the second time
    fun saveConfig() {
        statusArea.clear()
        val file = chooseFile(
            "Save config",
            filters = arrayOf(FileChooser.ExtensionFilter("HMDLag Experiment config files (*.json)", "*.json")),
            mode = FileChooserMode.Save
        )
        if (file.isEmpty()) return
        val writer = PrintWriter(file.first())
        writer.write(expConfiguration.toJSON().toString())
        writer.close()
        statusArea.appendText("Saved!")
    }

    fun saveAs() {
        val file = chooseFile(
            "Save config",
            filters = arrayOf(FileChooser.ExtensionFilter("HMDLag Experiment config files (*.json)", "*.json")),
            mode = FileChooserMode.Save
        )
        if (file.isEmpty()) return
        PrintWriter(file.first()).use { writer ->
            expConfiguration.filePath = file.first().absolutePath
            writer.write(expConfiguration.toJSON().toString())
        }
        statusArea.appendText("Saved!")
    }

    fun loadConfigurations() {
        statusArea.clear()
        val file = chooseFile(
            "Load config",
            filters = arrayOf(FileChooser.ExtensionFilter("HMDLag Experiment config files (*.json)", "*.json"))
        )
        if (file.isEmpty()) return
        val reader = BufferedReader(FileReader(file.first()))
        val jsonString = reader.lines().collect(Collectors.joining())
        expConfiguration.updateModel(loadJsonObject(jsonString))

    }

    fun newConfig() {
        this.form().clear()
        expConfiguration.clear()
    }

    fun verifyConfig() {
        statusArea.clear()
        var enabled = true
        if (expConfiguration.participantNumber == 0) {
            enabled = false
            statusArea.text += "ParticipantNumber cannot be 0 " + "\n"
        }
        if (expConfiguration.deviceProperty.isNull.get()) {
            enabled = false
            statusArea.text += "No Device selected" + "\n"
        }
        if (expConfiguration.interruptionTask.isNullOrEmpty()) {
            enabled = false
            statusArea.text += "No Task selected" + "\n"
        }
        if (expConfiguration.outputDirectory.isEmpty()) {
            enabled = false
            statusArea.text += "No Output Directory selected" + "\n"
        }
//        if (expConfiguration.trialsConfig == null) {
//            enabled = false
//            statusArea.text += "No Trials Config selected" + "\n"
//        }
        if (expConfiguration.hololensCueType == null) {
            enabled = false
            statusArea.text += "No Hololens Cue Type selected" + "\n"
        }

        startExpButton.isDisable = !enabled

    }

    //patients
    private val patients: ObservableList<PillPatient> = FXCollections.observableArrayList()

    private fun createFixedPatients() {
        patients.clear()

        patients.add(PillPatient(1, "Jens Busen",
            Monday= observableListOf(yellow, red, green, blue),
            Tuesday= observableListOf(red, short, green, pink),
            Wednesday = observableListOf(purple, green, long, orange),
            Thursday = observableListOf(yellow, short, green, blue),
            Friday= observableListOf(blue, green, long, blue),
            Saturday= observableListOf(short, yellow, yellow, red),
            Sunday= observableListOf(blue, pink, red, red),
            )
        )
    }

    private val yellow = "yellow"
    private val orange = "orange"
    private val red = "red"
    private val green = "green"
    private val blue = "blue"
    private val purple = "purple"
    private val pink = "pink"
    private val short = "15"
    private val long = "45"

}