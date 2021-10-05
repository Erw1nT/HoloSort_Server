package utils

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.util.StringConverter
import logging.GlobalLogger


class ObservableListStringConverter : StringConverter<ObservableList<Number>>()
{
    override fun fromString(string: String?): ObservableList<Number> {
        try {
            var s = string?.removePrefix("[")
            s = s?.removeSuffix("]")
            val tokens = s?.split(",")
            val list = FXCollections.observableArrayList<Number>()
            for (i in 0..(tokens!!.size-1)) {
                try {
                    list.add(tokens[i].toDouble())
                } catch (e2: Exception) {
                    list.add(0.0)
                    GlobalLogger.app().logError(e2.toString())
                }
            }
            return list
        } catch (e: Exception) {
            GlobalLogger.app().logError(e.toString())
            return FXCollections.observableArrayList()
        }
    }

    override fun toString(list: ObservableList<Number>?): String {
        return list.toString()
    }
}

/*class ObservableListVitalSignAlarmLevelConverter(var isDescending: Boolean = false) : StringConverter<ObservableList<VitalSignAlarmLevel>>()
{
    override fun fromString(string: String?): ObservableList<VitalSignAlarmLevel> {
        val list = FXCollections.observableArrayList<VitalSignAlarmLevel>()
        try {
            if (string == null || string.isBlank()) {
                return list
            }
            var s = string!!
            while (s.contains("["))
                s = s.replace("[", "")

            if (s.isBlank()) {
                return list
            }

            while (s.contains("]"))
                s = s.replace("]", "")

            if (s.isBlank()) {
                return list
            }
            val tokens = s.split(",", " ")
            for (i in 0 until tokens.size) {
                try {
                    if (tokens[i].isNotBlank()) {
                        val d = tokens[i].toDouble()
                        val l = VitalSignAlarmLevel(d)
                        list.add(l)
                    }
                } catch (e2: Exception) {
                    GlobalLogger.app().logError("ObservableListVitalSignAlarmLevelConverter:: fromString:: e2: double cast exception")
                }
            }
            return if (isDescending) FXCollections.observableArrayList(list.sortedByDescending { it.level })
            else FXCollections.observableArrayList(list.sortedBy { it.level })
        } catch (e: Exception) {
            GlobalLogger.app().logError("ObservableListVitalSignAlarmLevelConverter:: fromString:: e: outer exception")
            return list
        }
    }

    override fun toString(list: ObservableList<VitalSignAlarmLevel>?): String {
        return list.toString()
    }*/
