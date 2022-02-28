package models


import javafx.beans.property.*
import tornadofx.*
import javax.json.JsonObject

class Trial(id: Int = 1) : JsonModel {

    val idProperty = SimpleIntegerProperty(id)
    var id by idProperty

    val patientsProperty = SimpleListProperty<PillPatient>()
    var patientsList by patientsProperty

    init {
        patientsList = observableListOf<PillPatient>()
    }

    override fun updateModel(json: JsonObject) {
        with(json) {
            id = int("id")!!
//            patientsList = jsonArray("patients")!!.toList()
        }
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("id", id)
            add("patients", patientsList)
        }
    }

    override fun toString() = toJSON().toString()

}

abstract class TrialJsonModel<T : Trial>(val raaClazz: Class<T>) : JsonModel {
    fun <T> getClassInstance(clazz: Class<T>): T {
        return clazz.getConstructor().newInstance()
    }
}