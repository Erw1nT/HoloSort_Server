package models


import javafx.beans.property.*
import org.json.JSONPropertyIgnore
import tornadofx.*
import javax.json.JsonObject
import javax.json.JsonValue

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
            patientsList = jsonArray("patients")?.toList()?.map { it.toPatient() }?.toObservable()
        }
    }

    private fun JsonValue.toPatient() :PillPatient {
        // neuen Patienten erstellen und die Json-Daten übergeben
        // hat man davon, wenn man keine coole Json Library nutzt...
        val pp = PillPatient(1,"")
        pp.updateModel(this as JsonObject)
        return pp
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("id", id)
            add("patients", patientsList)
        }
    }

    override fun toString() = toJSON().toString()

}