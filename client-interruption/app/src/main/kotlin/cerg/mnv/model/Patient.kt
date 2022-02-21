package cerg.mnv.model


import mpmgame.Patient
import java.util.ArrayList

/**
 * This class represents a patient and all its related data
 */

class Patient(val id : Int, var name: String) : VitalSignListener {
    constructor(patient : Patient) : this(patient.id, patient.name) {
        patient.getVitalSigns().forEach {
            val newVitalSign = VitalSign(it.id, it.name)
            newVitalSign.value = it.value.toInt()
            var counter = it.lowerAlarmLevels.size
            if (it.lowerAlarmLevels.isNotEmpty()) {
                newVitalSign.lowAlarm = it.lowerAlarmLevels[--counter].toInt()
                if (counter > 0) newVitalSign.lowWarning = it.lowerAlarmLevels[--counter].toInt()
            }
            counter = it.upperAlarmLevels.size
            if (it.upperAlarmLevels.isNotEmpty()) {
                newVitalSign.highAlarm = it.upperAlarmLevels[--counter].toInt()
                if (counter > 0) newVitalSign.highWarning = it.upperAlarmLevels[--counter].toInt()
            }
            this.addVitalSign(newVitalSign)
        }
    }

    private val vitalSigns = ArrayList<VitalSign>()

    //Listener
    private val listeners = ArrayList<PatientListener>()

    // return immediately - no need to continue looping
    // Only upgrade to WARNING if current state is NORMAL - no downgrade from CRITICAL to warning (prevent overwriting status with lower one)
    val patientLevel: PatientLevel
        get() {
            var patientLevel = PatientLevel.NORMAL

            for (v in this.vitalSigns) {
                if (v.level == VitalSign.Level.VERY_HIGH || v.level == VitalSign.Level.VERY_LOW) return PatientLevel.CRITICAL

                if ((v.level ==VitalSign.Level.HIGH || v.level == VitalSign.Level.LOW) &&
                        patientLevel == PatientLevel.NORMAL) {
                            patientLevel = PatientLevel.WARNING
                }
            }

            return patientLevel
        }

    enum class PatientLevel {
        NORMAL, WARNING, CRITICAL
    }

    fun addListener(listener: PatientListener) {
        this.listeners.add(listener)
    }

    fun removeListener(listener: PatientListener): Boolean {
        return this.listeners.remove(listener)
    }

    fun notifyVitalSignChanged(vitalSign: VitalSign) {
        for (listener in this.listeners) {
            listener.notifyVitalSignChanged(this, vitalSign)
        }
    }

    fun notifyLevelVitalSignChanged(vitalSign: VitalSign) {
        for (listener in this.listeners) {
            listener.notifyLevelVitalSignChanged(this, vitalSign)
        }
    }

    override fun vitalSignChanged(vitalSign: VitalSign) {
        notifyVitalSignChanged(vitalSign)
    }

    override fun vitalSignLevelChanged(vitalSign: VitalSign) {
        notifyLevelVitalSignChanged(vitalSign)
    }

    // VitalSigns
    fun addVitalSign(vitalSign: VitalSign) {
        this.vitalSigns.add(vitalSign)
        vitalSign.addListener(this)
    }

    fun getVitalSign(index: Int): VitalSign? {
        for (v in this.vitalSigns) {
            if (v.id == index) return v
        }
        return null
    }

    fun clearVitalSigns() {
        for (vitalSign in this.vitalSigns) {
            vitalSign.removeListener(this)
        }
        vitalSigns.clear()
    }

    fun getIndexForVitalSignName(name: String): Int {
        var i = 0
        for (vitalSign in this.vitalSigns) {
            if (vitalSign.name == name) {
                return i
            }
            i++
        }
        return -1
    }

    fun getVitalSigns(): List<VitalSign> {
        return this.vitalSigns.toMutableList()
    }
}