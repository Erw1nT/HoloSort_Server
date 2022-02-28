package models

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import tornadofx.*
import javax.json.JsonObject

/**
 The day-properties of a patient are lists of strings, which contain either the color of the next pill or the duration of the interruption.
 */
class PillPatient(
    ID: Int,
    Name: String,
    Monday: ObservableList<String>,
    Tuesday: ObservableList<String>,
    Wednesday: ObservableList<String>,
    Thursday: ObservableList<String>,
    Friday: ObservableList<String>,
    Saturday: ObservableList<String>,
    Sunday: ObservableList<String>
    ) : JsonModel {

    constructor(ID: Int, Name: String) : this(ID, Name,
        observableListOf("1`", "ad", "3", "4", "6"),
        observableListOf("1`", "ad", "3", "4", "6"),
        observableListOf("1`", "ad", "3", "4", "6"),
        observableListOf("1`", "ad", "3", "4", "6"),
        observableListOf("1`", "ad", "3", "4", "6"),
        observableListOf("1`", "ad", "3", "4", "6"),
        observableListOf("1`", "ad", "3", "4", "6")) {}

    val idProperty = SimpleIntegerProperty(ID)
    var id by idProperty

    val nameProperty = SimpleStringProperty(Name)
    var name by nameProperty

    val mondayProperty = SimpleListProperty(Monday)
    var monday by mondayProperty

    val tuesdayProperty = SimpleListProperty(Tuesday)
    var tuesday by tuesdayProperty

    val wednesdayProperty = SimpleListProperty(Wednesday)
    var wednesday by wednesdayProperty

    val thursdayProperty = SimpleListProperty(Thursday)
    var thursday by thursdayProperty

    val fridayProperty = SimpleListProperty(Friday)
    var friday by fridayProperty

    val saturdayProperty = SimpleListProperty(Saturday)
    var saturday by saturdayProperty

    val sundayProperty = SimpleListProperty(Sunday)
    var sunday by sundayProperty

    override fun updateModel(json: JsonObject) {
        with(json) {
            id = int("id")!!
            name = string("name")!!
            monday = observableListOf("monday")
            tuesday = observableListOf("tuesday")
            wednesday = observableListOf("wednesday")
            thursday = observableListOf("thursday")
            friday = observableListOf("friday")
            saturday = observableListOf("saturday")
            sunday = observableListOf("sunday")
        }
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("id", id)
            add("name", name)
            add("monday", monday)
            add("tuesday", tuesday)
            add("wednesday", wednesday)
            add("thursday", thursday)
            add("friday", friday)
            add("saturday", saturday)
            add("sunday", sunday)
        }
    }

    override fun toString(): String = toJSON().toString()

}