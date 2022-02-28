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

    val trialConfigProperty = SimpleObjectProperty<Trial>()
    var trialConfig by trialConfigProperty

    val outputDirectoryProperty = SimpleStringProperty()
    var outputDirectory by outputDirectoryProperty

    val deviceProperty = SimpleStringProperty()
    var device by deviceProperty

    val trainingIncludedProperty = SimpleBooleanProperty(true)
    var trainingIncluded by trainingIncludedProperty

    val filePathProperty = SimpleStringProperty(null)
    var filePath: String? by filePathProperty

    val interruptionTaskProperty = SimpleStringProperty(null)
    var interruptionTask: String? by interruptionTaskProperty

    val hololensCueTypeProperty = SimpleStringProperty(null)
    var hololensCueType: String? by hololensCueTypeProperty

    override fun updateModel(json: JsonObject) {
        with(json) {
            participantNumber = int("ParticipantNumber")!!
            device = string("Device")
            outputDirectory = string("OutputDirectory")
            interruptionTask = string("InterruptionTask")
            trainingIncluded = boolean("TrainingIncluded")!!
            hololensCueType = string("HololensCueType")
        }
    }


    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("ParticipantNumber", participantNumber)
            add("TrialsConfiguration", trialConfig)
            add("OutputDirectory", outputDirectory)
            add("InterruptionTask", interruptionTask)
            add("Device", device)
            add("TrainingIncluded", trainingIncluded)
            add("HololensCueType", hololensCueType)
        }
    }

    fun clear() {
        participantNumber = 0
        trialConfig = null
        outputDirectory = HMDLagUserPrefsManager.loadPrefs(
            HMDLagUserPrefsManager.OUTPUT_DIRECTORY_KEY,
            System.getProperty("user.home")
        )
        interruptionTask = null
        device = null
        trainingIncluded = true
        hololensCueType = null
    }

    override fun toString(): String = toJSON().toString()
}