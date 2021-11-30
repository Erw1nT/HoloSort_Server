package models

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import utils.HMDLagUserPrefsManager
import javax.json.JsonObject

class ExperimentConfiguration : JsonModel {

    val participantNumProperty = SimpleIntegerProperty(0)
    var participantNumber by participantNumProperty

    //Pfad als String oder Json
    /*val trialsConfigProperty = SimpleObjectProperty<TrialsConfiguration<Trial>>()
    var trialsConfig by trialsConfigProperty

     */
    val trialsConfigProperty = SimpleObjectProperty<TrialsConfiguration<Trial>>()
    var trialsConfig by trialsConfigProperty

    val outputDirectoryProperty = SimpleStringProperty()
    var outputDirectory by outputDirectoryProperty

    val deviceProperty = SimpleStringProperty()
    var device by deviceProperty

    val trainingIncludedProperty = SimpleBooleanProperty(true)
    var trainingIncluded by trainingIncludedProperty

    val calibrationIncludedProperty = SimpleBooleanProperty(false)
    var calibrationIncluded by calibrationIncludedProperty

    val filePathProperty = SimpleStringProperty(null)
    var filePath: String? by filePathProperty

    val interruptionTaskProperty = SimpleStringProperty(null)
    var interruptionTask: String? by interruptionTaskProperty

    val hololensCueTypeProperty = SimpleStringProperty(null)
    var hololensCueType: String? by hololensCueTypeProperty

    val hololensCueSettingDurationProperty = SimpleIntegerProperty(5)
    var hololensCueSettingDuration by hololensCueSettingDurationProperty

    override fun updateModel(json: JsonObject) {
        with(json) {
            participantNumber = int("ParticipantNumber")!!
            device = string("Device")
            outputDirectory = string("OutputDirectory")
            interruptionTask = string("InterruptionTask")
            trainingIncluded = boolean("TrainingIncluded")!!
            calibrationIncluded = boolean("CalibrationIncluded")!!
            hololensCueType = string("HololensCueType")
            hololensCueSettingDuration = int("HololensCueSettingDuration")!!
        }
    }


    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("ParticipantNumber", participantNumber)
            add("TrialsConfiguration", trialsConfig)
            add("OutputDirectory", outputDirectory)
            add("InterruptionTask", interruptionTask)
            add("Device", device)
            add("TrainingIncluded", trainingIncluded)
            add("CalibrationIncluded", calibrationIncluded)
            add("HololensCueType", hololensCueType)
            add("HololensCueSettingDuration", hololensCueSettingDuration)
        }
    }

    fun clear() {
        participantNumber = 0
        trialsConfig = null
        outputDirectory = HMDLagUserPrefsManager.loadPrefs(
            HMDLagUserPrefsManager.OUTPUT_DIRECTORY_KEY,
            System.getProperty("user.home")
        )
        interruptionTask = null
        device = null
        trainingIncluded = true
        calibrationIncluded = true
        hololensCueType = null
        hololensCueSettingDuration = 5
    }

    override fun toString(): String = toJSON().toString()
}