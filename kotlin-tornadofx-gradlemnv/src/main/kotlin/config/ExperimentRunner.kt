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

    val expConfigProperty = SimpleObjectProperty<ExperimentConfiguration>(ExperimentConfiguration())
    var expConfiguration by expConfigProperty

    lateinit var statusArea: TextArea
    lateinit var startExpButton: Button

    private var caliField : Field? = null
    private var frontendSubs : Subscriber? = null
    private var webClientSubs : Subscriber? = null

    private var blockEditor: Stage? = null

    init {
        expConfiguration.outputDirectory = HMDLagUserPrefsManager.loadPrefs(
            HMDLagUserPrefsManager.OUTPUT_DIRECTORY_KEY,
            System.getProperty("user.home")
        )

        expConfiguration.deviceProperty.onChange {
            if (expConfiguration.device == "Tablet"){
                caliField?.setDisable(true)
                expConfiguration.calibrationIncluded =  false
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
            label("HMDLag version: 2021-10-20") {
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
                            val file =
                                chooseDirectory("Choose output directory", File(expConfiguration.outputDirectory))
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

                    var txtfield: TextField? = null

                    field("Select Hololens Cue Type"){
                        val toggleGroup = ToggleGroup()
                        vbox {
                            radiobutton("None", toggleGroup) {
                                action {
                                    expConfiguration.hololensCueType = HololensCueType.NONE.identifier
                                    txtfield?.text = "0"
                                    txtfield?.isEditable = false

                                }
                            }
                            radiobutton("Automatic", toggleGroup) {
                                action {
                                    expConfiguration.hololensCueType = HololensCueType.AUTOMATIC.identifier
                                    txtfield?.text = "0"
                                    txtfield?.isEditable = false
                                }
                            }
                            radiobutton("Manual", toggleGroup) {
                                action {
                                    expConfiguration.hololensCueType = HololensCueType.MANUAL.identifier
                                    txtfield?.text = "5"
                                    txtfield?.isEditable = true
                                }
                            }
                        }
                    }


                    field("Training") {
                        checkbox("Include Training", expConfiguration.trainingIncludedProperty)
                        {
                            isSelected = true
                        }
                    }

                    caliField = field("Calibration") {
                        checkbox("Include Calibration", expConfiguration.calibrationIncludedProperty)
                        {
                            isSelected = false
                            isDisable = true
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
        GlobalLogger.exp().addColumns(arrayOf("Participant Number", "Block", "Device", "Hololens Cue Type", "Interruption Trial", "Trial", "First Click In Module", "INTEGER: First Click In Module", "Wrong Click In Module After Interruption", "Patient ID", "Module", "Error Wrong Module", "Error Input", "Error Empty Module", "Error Count Interruption", "Interruption Length", "Click on OK", "INTEGER: Click on OK", "Start Time Interruption", "INTEGER: Start Time Interruption", "End Time Interruption", "INTEGER: End Time Interruption"))
        GlobalLogger.exp().writerHeader()

    }

    //functions
    private fun sendPatientAndTrialInformation() {
        //createPatients()
        createFixedPatients()

        if (trialsConfig.trials.size > patients.size) {
            statusArea.clear()
            statusArea.appendText("Please create more patients manually")
        }

        if (!Publisher.isNetworkingActiveAndRunning()) {
            statusArea.clear()
            statusArea.appendText("Network is not running")
        }

        val subscriber = Publisher.getSubscribers()
        if (subscriber.size == 1) {
            statusArea.clear()
            statusArea.appendText("No subscriber connected")
        } else {
            val obj = JSONObject()
            //TODO Publisher.sendMessage to subscriber

            val objFrontend = JSONObject()
            objFrontend.put("type", "expDataHMD")
            objFrontend.put("training", expConfiguration.trainingIncluded)
            objFrontend.put("calibration", expConfiguration.calibrationIncluded)
            objFrontend.put("interruptionTask", expConfiguration.interruptionTask)

            // who did that? why? just why?
            Publisher.getSubscribers().forEach {
                if (it.value.name == "frontend") {
                    frontendSubs = it.value
                }
                else if(it.value.name == "web client") {
                    webClientSubs = it.value
                }
            }

            if(frontendSubs !== null){
                println("there is a frontend")
                Publisher.sendMessage(objFrontend, frontendSubs!!)
            }

            obj.put("type", "expData")
            obj.put("metaInfo", expConfiguration)
            obj.put("trialsConfig", trialsConfig)
            obj.put("list", patients)
            obj.put("hololensCueType", expConfiguration.hololensCueType)

            if(webClientSubs !== null){
                print("there is a webclient")
                Publisher.sendMessage(obj, webClientSubs!!)
            }

            val lens = Publisher.getSubscribers().values.singleOrNull { it.name == "lens" }
            if (lens !== null)
            {
                // More information are not needed on the lens' side
                val jsonObj = JSONObject()
                jsonObj.put("type", "expData")
                jsonObj.put("hololensCueType", expConfiguration.hololensCueType)

                Publisher.sendMessage(jsonObj, lens)
            }

            //Publisher.publish(obj)
            statusArea.clear()
            statusArea.appendText("Experiment data was sent successfully")
        }

    }

    fun loadTrialConfig() {
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
        if (expConfiguration.trialsConfig == null) {
            enabled = false
            statusArea.text += "No Trials Config selected" + "\n"
        }
        if (expConfiguration.hololensCueType == null) {
            enabled = false
            statusArea.text += "No Hololens Cue Type selected" + "\n"
        }

        startExpButton.isDisable = !enabled

    }

    override fun onUndock() {
        //this.blockRunner?.stop()
    }

    //patients
    val patients: ObservableList<Patient> = FXCollections.observableArrayList<Patient>()

    val names = listOf("Mike Mayer", "John Smith", "Jon Doe")

    val weight = listOf(60, 70, 80)

    val height = listOf(160, 170, 180, 190)

    val dateOfBirth = listOf("29/05/1993", "14/10/1980", "01/03/1946")

    val allAllergies = listOf("Nuts", "Pollen", "Latex", "No allergies", "Dogs")

    val times =
        listOf(listOf("13:05", "12:52", "13:10"), listOf("10:16", "10:17", "10:20"), listOf("08:47", "08:16", "09:06"))

    val allergies: ObservableList<String> = FXCollections.observableArrayList<String>()

    val sterofundinDosis = listOf(400.0, 450.0, 500.0, 550.0)
    val fentanylDosis = listOf(0.10, 0.15, 0.20, 0.25)
    val propofolDosis = listOf(140.0, 150.0, 160.0, 170.0)

    val respiratoryDescription = listOf("endotracheal", "Beispiel2")
    val respiratoryType = listOf("HVLP", "Beispiel2")
    val respiratorySize = listOf("5 mm", "8 mm", "9 mm", "10 mm", "20 mm")

    val positionMain = listOf("Supine position", "Torso high", "Torso low", "Stomach")
    val positionHead = listOf("In foam shell", "On gel ring")
    val positionTrunk = listOf("Spine orthograde", "Shoulder support")

    val catheterType = listOf("desc1", "desc2")
    val catheterDescription = listOf("Arterial", "CVC", "PVC")
    val catheterPosition = listOf("left", "right")


    val tubeDescription = listOf("Stomach tube", "TEE-tube", "Temperature tube")
    val tubeType = listOf("oral", "rectal", "nasal")

    fun createPatients() {
        patients.clear()
        var timerange = times.random()
        for (trial in trialsConfig.trials) {
            allergies.clear()
            allergies.add(allAllergies.random())
            allergies.add(allAllergies.random())
            patients.add(
                Patient(
                    id = trial.id,
                    name = names.random(),
                    dateOfBrith = dateOfBirth.random(),
                    height = height.random(),
                    weight = weight.random(),
                    allergies = allergies,
                    drugOneDosis = sterofundinDosis.random(),
                    drugTwoDosis = fentanylDosis.random(),
                    drugThreeDosis = propofolDosis.random(),
                    drugOneTime = timerange.random(),
                    drugTwoTime = timerange.random(),
                    drugThreeTime = timerange.random(),
                    respiratoryDescription = respiratoryDescription.random(),
                    respiratorySize = respiratorySize.random(),
                    respiratoryType = respiratoryType.random(),
                    positioningHead = positionHead.random(),
                    positioningMain = positionMain.random(),
                    positioningTrunk = positionTrunk.random(),
                    catheterType = catheterType.random(),
                    catheterDescription = catheterDescription.random(),
                    catheterPosition = catheterPosition.random(),
                    tubeType = tubeType.random(),
                    tubeDescription = tubeDescription.random(),
                    tubeTime = timerange.random()
                )
            )
        }

    }

    fun createFixedPatients() {
        patients.clear();
        patients.add(
            Patient(
                id = 1,
                name = "Catherine Jacka",
                dateOfBrith = "10/06/1989",
                height = 165,
                weight = 47,
                allergies = FXCollections.observableArrayList<String>("No allergies"),
                drugOneDosis = 350.0,
                drugTwoDosis = 0.15,
                drugThreeDosis = 100.0,
                drugOneTime = "13:04",
                drugTwoTime = "13:09",
                drugThreeTime = "13:11",
                respiratoryDescription = "Endotracheal tube",
                respiratorySize = "7 mm",
                respiratoryType = "cuffed",
                positioningHead = "In foam shell",
                positioningMain = "Supine position",
                positioningTrunk = "Spine orthograde",
                catheterType = "Arterial",
                catheterDescription = "radial artery",
                catheterPosition = "left",
                tubeType = "oral",
                tubeDescription = "Stomach tube",
                tubeTime = "13:13"
            )
        )
        patients.add(
            Patient(
                id = 2,
                name = "Lukas Keen",
                dateOfBrith = "20/10/1958",
                height = 192,
                weight = 102,
                allergies = FXCollections.observableArrayList<String>("Latex"),
                drugOneDosis = 700.0,
                drugTwoDosis = 0.30,
                drugThreeDosis = 220.0,
                drugOneTime = "06:22",
                drugTwoTime = "06:26",
                drugThreeTime = "06:27",
                respiratoryDescription = "Endotracheal tube",
                respiratorySize = "9 mm",
                respiratoryType = "cuffed",
                positioningHead = "On gel ring",
                positioningMain = "Torso high",
                positioningTrunk = "Shoulder support",
                catheterType = "CVC",
                catheterDescription = "internal jugular vein",
                catheterPosition = "right",
                tubeType = "oral",
                tubeDescription = "Temperature tube",
                tubeTime = "06:30"
            )
        )
        patients.add(
            Patient(
                id = 3,
                name = "Oscar Batt",
                dateOfBrith = "06/01/1974",
                height = 179,
                weight = 81,
                allergies = FXCollections.observableArrayList<String>("Pollen"),
                drugOneDosis = 550.0,
                drugTwoDosis = 0.25,
                drugThreeDosis = 180.0,
                drugOneTime = "08:45",
                drugTwoTime = "08:57",
                drugThreeTime = "09:01",
                respiratoryDescription = "Tracheostomy tube",
                respiratorySize = "8 mm",
                respiratoryType = "cuffed",
                positioningHead = "On gel ring",
                positioningMain = "Supine position",
                positioningTrunk = "Shoulder support",
                catheterType = "PVC",
                catheterDescription = "back of hand",
                catheterPosition = "left",
                tubeType = "oral",
                tubeDescription = "TEE-tube",
                tubeTime = "09:02"
            )
        )
        patients.add(
            Patient(
                id = 4,
                name = "Jamie Swain",
                dateOfBrith = "18/07/1975",
                height = 189,
                weight = 133,
                allergies = FXCollections.observableArrayList<String>("No allergies"),
                drugOneDosis = 900.0,
                drugTwoDosis = 0.40,
                drugThreeDosis = 220.0,
                drugOneTime = "10:30",
                drugTwoTime = "10:35",
                drugThreeTime = "10:36",
                respiratoryDescription = "Endotracheal tube",
                respiratorySize = "10 mm",
                respiratoryType = "uncuffed",
                positioningHead = "On gel ring",
                positioningMain = "Stomach",
                positioningTrunk = "Shoulder support",
                catheterType = "Arterial",
                catheterDescription = "brachial artery",
                catheterPosition = "right",
                tubeType = "left nasal",
                tubeDescription = "Stomach tube",
                tubeTime = "10:37"
            )
        )
        patients.add(
            Patient(
                id = 5,
                name = "Carol Warner",
                dateOfBrith = "23/01/1968",
                height = 157,
                weight = 41,
                allergies = FXCollections.observableArrayList<String>("Latex"),
                drugOneDosis = 300.0,
                drugTwoDosis = 0.10,
                drugThreeDosis = 100.0,
                drugOneTime = "11:46",
                drugTwoTime = "11:50",
                drugThreeTime = "11:52",
                respiratoryDescription = "Endotracheal tube",
                respiratorySize = "7 mm",
                respiratoryType = "cuffed",
                positioningHead = "In foam shell",
                positioningMain = "Supine position",
                positioningTrunk = "Spine orthograde",
                catheterType = "CVC",
                catheterDescription = "subclavian vein",
                catheterPosition = "left",
                tubeType = "rectal",
                tubeDescription = "Temperature tube",
                tubeTime = "11:54"
            )
        )
        patients.add(
            Patient(
                id = 6,
                name = "Claire FitzRoy",
                dateOfBrith = "11/11/1944",
                height = 170,
                weight = 64,
                allergies = FXCollections.observableArrayList<String>("Lactose"),
                drugOneDosis = 450.0,
                drugTwoDosis = 0.20,
                drugThreeDosis = 140.0,
                drugOneTime = "12:13",
                drugTwoTime = "12:15",
                drugThreeTime = "12:16",
                respiratoryDescription = "Tracheostomy tube",
                respiratorySize = "8 mm",
                respiratoryType = "uncuffed",
                positioningHead = "On gel ring",
                positioningMain = "Torso high",
                positioningTrunk = "Shoulder support",
                catheterType = "PVC",
                catheterDescription = "back of hand",
                catheterPosition = "right",
                tubeType = "rectal",
                tubeDescription = "TEE-tube",
                tubeTime = "12:17"
            )
        )
        patients.add(
            Patient(
                id = 7,
                name = "Valerie Deamer",
                dateOfBrith = "22/10/1936",
                height = 160,
                weight = 50,
                allergies = FXCollections.observableArrayList<String>("Soy"),
                drugOneDosis = 350.0,
                drugTwoDosis = 0.15,
                drugThreeDosis = 120.0,
                drugOneTime = "12:52",
                drugTwoTime = "13:05",
                drugThreeTime = "13:07",
                respiratoryDescription = "Endotracheal tube",
                respiratorySize = "7 mm",
                respiratoryType = "cuffed",
                positioningHead = "In foam shell",
                positioningMain = "Torso low",
                positioningTrunk = "Spine orthograde",
                catheterType = "Arterial",
                catheterDescription = "femoral artery",
                catheterPosition = "left",
                tubeType = "right nasal",
                tubeDescription = "Stomach tube",
                tubeTime = "13:07"
            )
        )
        patients.add(
            Patient(
                id = 8,
                name = "Sam Ingam",
                dateOfBrith = "06/12/1961",
                height = 200,
                weight = 120,
                allergies = FXCollections.observableArrayList<String>("No allergies"),
                drugOneDosis = 900.0,
                drugTwoDosis = 0.40,
                drugThreeDosis = 200.0,
                drugOneTime = "14:23",
                drugTwoTime = "14:32",
                drugThreeTime = "14:33",
                respiratoryDescription = "Endotracheal tube",
                respiratorySize = "10 mm",
                respiratoryType = "uncuffed",
                positioningHead = "On gel ring",
                positioningMain = "Stomach",
                positioningTrunk = "Shoulder support",
                catheterType = "CVC",
                catheterDescription = "axillary vein",
                catheterPosition = "right",
                tubeType = "oral",
                tubeDescription = "Temperature tube",
                tubeTime = "14:33"
            )
        )
        patients.add(
            Patient(
                id = 9,
                name = "Vanessa Farr",
                dateOfBrith = "08/03/1997",
                height = 148,
                weight = 49,
                allergies = FXCollections.observableArrayList<String>("Latex"),
                drugOneDosis = 350.0,
                drugTwoDosis = 0.15,
                drugThreeDosis = 100.0,
                drugOneTime = "15:36",
                drugTwoTime = "15:40",
                drugThreeTime = "15:41",
                respiratoryDescription = "Tracheostomy tube",
                respiratorySize = "7 mm",
                respiratoryType = "cuffed",
                positioningHead = "In foam shell",
                positioningMain = "Supine position",
                positioningTrunk = "Spine orthograde",
                catheterType = "PVC",
                catheterDescription = "back of hand",
                catheterPosition = "left",
                tubeType = "oral",
                tubeDescription = "TEE-tube",
                tubeTime = "15:42"
            )
        )
        patients.add(
            Patient(
                id = 10,
                name = "Carl Newling",
                dateOfBrith = "18/05/1981",
                height = 163,
                weight = 55,
                allergies = FXCollections.observableArrayList<String>("Pollen"),
                drugOneDosis = 400.0,
                drugTwoDosis = 0.20,
                drugThreeDosis = 120.0,
                drugOneTime = "16:09",
                drugTwoTime = "16:14",
                drugThreeTime = "16:15",
                respiratoryDescription = "Endotracheal tube",
                respiratorySize = "8 mm",
                respiratoryType = "uncuffed",
                positioningHead = "On gel ring",
                positioningMain = "Torso high",
                positioningTrunk = "Shoulder support",
                catheterType = "Arterial",
                catheterDescription = "radial artery",
                catheterPosition = "right",
                tubeType = "oral",
                tubeDescription = "Stomach tube",
                tubeTime = "16:16"
            )
        )
        patients.add(
            Patient(
                id = 11,
                name = "Sandra Ord",
                dateOfBrith = "23/03/1950",
                height = 176,
                weight = 61,
                allergies = FXCollections.observableArrayList<String>("Soy"),
                drugOneDosis = 400.0,
                drugTwoDosis = 0.20,
                drugThreeDosis = 140.0,
                drugOneTime = "17:20",
                drugTwoTime = "17:25",
                drugThreeTime = "17:26",
                respiratoryDescription = "Endotracheal tube",
                respiratorySize = "8 mm",
                respiratoryType = "cuffed",
                positioningHead = "In foam shell",
                positioningMain = "Torso low",
                positioningTrunk = "Spine orthograde",
                catheterType = "CVC",
                catheterDescription = "femoral vein",
                catheterPosition = "left",
                tubeType = "rectal",
                tubeDescription = "Temperature tube",
                tubeTime = "17:27"
            )
        )
        patients.add(
            Patient(
                id = 12,
                name = "Emily Taplin",
                dateOfBrith = "08/07/1983",
                height = 167,
                weight = 70,
                allergies = FXCollections.observableArrayList<String>("No allergies"),
                drugOneDosis = 500.0,
                drugTwoDosis = 0.20,
                drugThreeDosis = 160.0,
                drugOneTime = "18:47",
                drugTwoTime = "18:48",
                drugThreeTime = "18:50",
                respiratoryDescription = "Tracheostomy tube",
                respiratorySize = "8 mm",
                respiratoryType = "uncuffed",
                positioningHead = "On gel ring",
                positioningMain = "Stomach",
                positioningTrunk = "Shoulder support",
                catheterType = "PVC",
                catheterDescription = "back of hand",
                catheterPosition = "right",
                tubeType = "rectal",
                tubeDescription = "TEE-tube",
                tubeTime = "18:51"
            )
        )
        patients.add(
            Patient(
                id = 13,
                name = "Evie Jones",
                dateOfBrith = "23/02/1957",
                height = 168,
                weight = 53,
                allergies = FXCollections.observableArrayList<String>("No allergies"),
                drugOneDosis = 450.0,
                drugTwoDosis = 0.20,
                drugThreeDosis = 180.0,
                drugOneTime = "11:24",
                drugTwoTime = "11:27",
                drugThreeTime = "11:36",
                respiratoryDescription = "Endotracheal tube",
                respiratorySize = "10 mm",
                respiratoryType = "uncuffed",
                positioningHead = "In foam shell",
                positioningMain = "Stomach",
                positioningTrunk = "Shoulder support",
                catheterType = "CVC",
                catheterDescription = "internal jugular vein",
                catheterPosition = "left",
                tubeType = "rectal",
                tubeDescription = "TEE-tube",
                tubeTime = "11:37"
            )
        )
        patients.add(
            Patient(
                id = 14,
                name = "Ethan Williams",
                dateOfBrith = "05/07/1989",
                height = 161,
                weight = 65,
                allergies = FXCollections.observableArrayList<String>("Soy"),
                drugOneDosis = 500.0,
                drugTwoDosis = 0.25,
                drugThreeDosis = 180.0,
                drugOneTime = "12:01",
                drugTwoTime = "12:02",
                drugThreeTime = "12:08",
                respiratoryDescription = "Tracheostomy tube",
                respiratorySize = "7 mm",
                respiratoryType = "cuffed",
                positioningHead = "On gel ring",
                positioningMain = "Stomach",
                positioningTrunk = "Spine orthograde",
                catheterType = "CVC",
                catheterDescription = "femoral vein",
                catheterPosition = "left",
                tubeType = "left nasal",
                tubeDescription = "Stomach tube",
                tubeTime = "12:08"
            )
        )
        patients.add(
            Patient(
                id = 15,
                name = "Olivia Brown",
                dateOfBrith = "06/02/1991",
                height = 178,
                weight = 92,
                allergies = FXCollections.observableArrayList<String>("No allergies"),
                drugOneDosis = 600.0,
                drugTwoDosis = 0.35,
                drugThreeDosis = 140.0,
                drugOneTime = "08:10",
                drugTwoTime = "08:11",
                drugThreeTime = "08:14",
                respiratoryDescription = "Tracheostomy tube",
                respiratorySize = "8 mm",
                respiratoryType = "uncuffed",
                positioningHead = "In foam shell",
                positioningMain = "Torso High",
                positioningTrunk = "Shoulder support",
                catheterType = "Arterial",
                catheterDescription = "back of hand",
                catheterPosition = "right",
                tubeType = "rectal",
                tubeDescription = "Temperature tube",
                tubeTime = "08:17"
            )
        )
        patients.add(
            Patient(
                id = 16,
                name = "Sienna Anderson",
                dateOfBrith = "13/11/1967",
                height = 167,
                weight = 74,
                allergies = FXCollections.observableArrayList<String>("Pollen"),
                drugOneDosis = 550.0,
                drugTwoDosis = 0.15,
                drugThreeDosis = 180.0,
                drugOneTime = "15:15",
                drugTwoTime = "15:24",
                drugThreeTime = "15:25",
                respiratoryDescription = "Tracheostomy tube",
                respiratorySize = "7 mm",
                respiratoryType = "cuffed",
                positioningHead = "In foam shell",
                positioningMain = "Torso low",
                positioningTrunk = "Shoulder support",
                catheterType = "Arterial",
                catheterDescription = "back of hand",
                catheterPosition = "right",
                tubeType = "oral",
                tubeDescription = "TEE-tube",
                tubeTime = "15:26"
            )
        )
        patients.add(
            Patient(
                id = 17,
                name = "Liam Kelly",
                dateOfBrith = "10/10/78",
                height = 174,
                weight = 94,
                allergies = FXCollections.observableArrayList<String>("Latex"),
                drugOneDosis = 700.0,
                drugTwoDosis = 0.25,
                drugThreeDosis = 200.0,
                drugOneTime = "16:29",
                drugTwoTime = "16:32",
                drugThreeTime = "16:34",
                respiratoryDescription = "Endotracheal tube",
                respiratorySize = "9 mm",
                respiratoryType = "cuffed",
                positioningHead = "On gel ring",
                positioningMain = "Supine position",
                positioningTrunk = "Spine orthograde",
                catheterType = "PVC",
                catheterDescription = "back of hand",
                catheterPosition = "right",
                tubeType = "oral",
                tubeDescription = "Temperature tube",
                tubeTime = "16:50"
            )
        )
        patients.add(
            Patient(
                id = 18,
                name = "Harvey Martin",
                dateOfBrith = "06/04/1984",
                height = 184,
                weight = 107,
                allergies = FXCollections.observableArrayList<String>("Pollen"),
                drugOneDosis = 700.0,
                drugTwoDosis = 0.35,
                drugThreeDosis = 120.0,
                drugOneTime = "12:59",
                drugTwoTime = "13:01",
                drugThreeTime = "13:08",
                respiratoryDescription = "Endotracheal tube",
                respiratorySize = "9 mm",
                respiratoryType = "uncuffed",
                positioningHead = "On gel ring",
                positioningMain = "Torso low",
                positioningTrunk = "Spine orthograde",
                catheterType = "PVC",
                catheterDescription = "back of hand",
                catheterPosition = "left",
                tubeType = "right nasal",
                tubeDescription = "Stomach tube",
                tubeTime = "13:08"
            )
        )

        patients.add(
            Patient(
                id = 100,
                name = "Sam Macghey",
                dateOfBrith = "02/04/1996",
                height = 180,
                weight = 66,
                allergies = FXCollections.observableArrayList<String>("No allergies"),
                drugOneDosis = 450.0,
                drugTwoDosis = 0.20,
                drugThreeDosis = 140.0,
                drugOneTime = "12:57",
                drugTwoTime = "13:08",
                drugThreeTime = "13:10",
                respiratoryDescription = "Endotracheal tube",
                respiratorySize = "8 mm",
                respiratoryType = "uncuffed",
                positioningHead = "On gel ring",
                positioningMain = "Stomach",
                positioningTrunk = "Shoulder support",
                catheterType = "CVC",
                catheterDescription = "axillary vein",
                catheterPosition = "right",
                tubeType = "oral",
                tubeDescription = "Temperature tube",
                tubeTime = "13:11"

            )
        )
        patients.add(
            Patient(
                id = 101,
                name = "Mary Hampton",
                dateOfBrith = "13/12/1940",
                height = 154,
                weight = 51,
                allergies = FXCollections.observableArrayList<String>("Nuts"),
                drugOneDosis = 350.0,
                drugTwoDosis = 0.15,
                drugThreeDosis = 120.0,
                drugOneTime = "08:53",
                drugTwoTime = "09:01",
                drugThreeTime = "09:02",
                respiratoryDescription = "Endotracheal tube",
                respiratorySize = "7 mm",
                respiratoryType = "uncuffed",
                positioningHead = "On gel ring",
                positioningMain = "Stomach",
                positioningTrunk = "Shoulder support",
                catheterType = "Arterial",
                catheterDescription = "femoral artery",
                catheterPosition = "right",
                tubeType = "right nasal",
                tubeDescription = "Stomach tube",
                tubeTime = "09:04"
            )
        )
    }
}