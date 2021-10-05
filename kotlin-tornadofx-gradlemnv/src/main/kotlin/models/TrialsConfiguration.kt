package models

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import javax.json.JsonObject

class TrialsConfiguration<T : Trial>(raaClazz: Class<T>) : TrialJsonModel<T>(raaClazz) {

    val trials: ObservableList<T> = FXCollections.observableArrayList<T>()
    var trainingsTrial: Trial = TrialTemplate()
    val patientSeedProperty = SimpleIntegerProperty(0)
    var patientSeed by patientSeedProperty

    val logDirProperty = SimpleStringProperty("")
    var logDir by logDirProperty

    fun clear() {
        trials.clear()
    }

    override fun updateModel(json: JsonObject) {
        with(json) {
            val trialJson = getJsonArray("trials")
            if (trialJson != null) {
                trials.clear()
                trialJson.forEach {
                    val modelObject = getClassInstance(raaClazz)
                    modelObject.updateModel(it.asJsonObject())
                    trials.add(modelObject)
                }
            } else trials.clear()
            val modelObject = getClassInstance(raaClazz)
            modelObject.updateModel(getJsonObject("trainingsTrial"))
            trainingsTrial = modelObject
            patientSeed = int("patientSeed")!!
            logDir = string("logDir") ?: "."
        }
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("trials", trials.toJSON())
            add("trainingsTrial", trainingsTrial)
            add("patientSeed", patientSeed)
            add("logDir", logDir)
        }
    }

    override fun toString(): String = toJSON().toString()
}