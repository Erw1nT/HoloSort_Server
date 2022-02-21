package cerg.mnv.model

import java.util.ArrayList

/**
 * Represents a single VitalSign
 */
class VitalSign(val id : Int, var name: String?) {
    var value = 0
        set(value) {
            if (this.value != value) {
                field = value
                notifyVitalSignChanged()
                level = determinePatientLevel()
            }
        }
    var lowWarning = 0
        set(lowWarning) {
            field = lowWarning
            level = determinePatientLevel()
        }
    var highWarning = 0
        set(highWarning) {
            field = highWarning
            level = determinePatientLevel()
        }
    var lowAlarm = 0
        set(lowAlarm) {
            field = lowAlarm
            level = determinePatientLevel()
        }
    var highAlarm = 0
        set(highAlarm) {
            field = highAlarm
            level = determinePatientLevel()
        }
    //Log.d(TAG, "setLevel:" + this.getName() + " changed");
    var level = Level.NOT_VALID
        set(level) {
            val oldLevel = this.level
            field = level

            if (oldLevel != level) {
                notifyVitalSignLevelChanged()
            }
        }

    // Listener
    private val listeners = ArrayList<VitalSignListener>()

    enum class Level {
        VERY_LOW, LOW, NORMAL, HIGH, VERY_HIGH, NOT_VALID, UNINITIALIZED
    }

    fun addListener(listener: VitalSignListener) {
        this.listeners.add(listener)
    }

    fun removeListener(listener: VitalSignListener): Boolean {
        return this.listeners.remove(listener)
    }

    private fun notifyVitalSignChanged() {
        for (listener in this.listeners) {
            listener.vitalSignChanged(this)
        }
    }

    private fun notifyVitalSignLevelChanged() {
        for (listener in this.listeners) {
            listener.vitalSignLevelChanged(this)
        }
    }


    /**
     * Determine the level of the vital sign
     *
     * @return the level
     */
    fun determinePatientLevel(): Level {
        // uninitialized
        if (this.value == -1) return Level.UNINITIALIZED

        // Check if all thresholds are given
        if (this.lowWarning == 0 || this.highWarning == 0 || this.lowAlarm == 0 || this.highAlarm == 0) {
            return Level.NOT_VALID
        }

        // Check if thresholds are plausible
        if (this.lowWarning < this.lowAlarm) {
            return Level.NOT_VALID
        }
        if (this.highWarning > this.highAlarm) {
            return Level.NOT_VALID
        }
        if (this.lowWarning > this.highWarning) {
            return Level.NOT_VALID
        }

        // Determine state of vital sign
        return when {
            this.value > this.highAlarm -> Level.VERY_HIGH
            this.value > this.highWarning -> Level.HIGH
            this.value < this.lowAlarm -> Level.VERY_LOW
            this.value < this.lowWarning -> Level.LOW
            else -> Level.NORMAL
        }
    }
}
