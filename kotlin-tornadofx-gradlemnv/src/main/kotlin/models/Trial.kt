package models


import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import javax.json.JsonObject

open class Trial(id: Int = 1, interruptionTrial: Boolean = false, sichtbarkeit: Boolean = false) : JsonModel {

    val idProperty = SimpleIntegerProperty(id)
    var id by idProperty

    val interruptionTrialProperty = SimpleBooleanProperty(interruptionTrial)
    var interruptionTrial by interruptionTrialProperty

    val sichtbarkeitProperty = SimpleBooleanProperty(sichtbarkeit)
    var sichtbarkeit by sichtbarkeitProperty

    val patientProperty = SimpleIntegerProperty()
    var patient by patientProperty

    val patientInformationProperty = SimpleIntegerProperty(0)
    var patientInformation by patientInformationProperty

    val medicationProperty = SimpleIntegerProperty(0)
    var medication by medicationProperty

    val respiratoryProperty = SimpleIntegerProperty(0)
    var respiratory by respiratoryProperty

    val catheterProperty = SimpleIntegerProperty(0)
    var catheter by catheterProperty

    val tubeProperty = SimpleIntegerProperty(0)
    var tube by tubeProperty

    val positioningProperty = SimpleIntegerProperty(0)
    var positioning by positioningProperty


    val customArgsProperty = SimpleStringProperty("")
    var customArgs by customArgsProperty

    val filePathProperty = SimpleStringProperty("")
    var filePath by filePathProperty

    override fun updateModel(json: JsonObject) {
        with(json) {
            id = int("id")!!
            interruptionTrial = boolean("interruptionTrial")!!
            sichtbarkeit = boolean("sichtbarkeit")!!
            patient = int("patient")!!
            patientInformation = int("patientInformation")!!
            medication = int("medication")!!
            respiratory = int("respiratory")!!
            catheter = int("catheter")!!
            tube = int("tube")!!
            positioning = int("positioning")!!
            customArgs = string("customArgs")
            filePath = string("filePath")
        }
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("id", id)
            add("interruptionTrial", interruptionTrial)
            add("sichtbarkeit", sichtbarkeit)
            add("patient", patient)
            add("patientInformation", patientInformation)
            add("medication", medication)
            add("respiratory", respiratory)
            add("catheter", catheter)
            add("tube", tube)
            add("positioning", positioning)
            add("customArgs", customArgs)
            add("filePath", filePath)
        }
    }

    override fun toString() = toJSON().toString()

}

abstract class TrialJsonModel<T : Trial>(val raaClazz: Class<T>) : JsonModel {
    fun <T> getClassInstance(clazz: Class<T>): T {
        return clazz.getConstructor().newInstance()
    }
}