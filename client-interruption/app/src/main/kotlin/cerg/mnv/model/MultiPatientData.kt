package cerg.mnv.model

import android.util.Log
import mpmgame.MultiPatient

import java.util.ArrayList

/**
 * Contains all information fore all patients
 */
object MultiPatientData : PatientListener {
    // Debug Tag
    private const val TAG = "MultiPatientData"

    private val patients = ArrayList<Patient>()

    // Listener
    private val listeners = ArrayList<MultiPatientDataListener>()

    var isAlarmsEnabled = false
    var isVisualEnabled = true

    // Display related data
    var isStarted = false
        set(started) {
            if (this.isStarted == started) return
            if (started) {
                notifyScenarioStarted()
            } else {
                notifyScenarioStopped()
            }
            field = started
        }
    var isFreezed = false
        set(freezed) {
            if (this.isFreezed == freezed) return
            field = freezed
            if (freezed) {
                notifyFreezeOccured()
            } else {
                notifyUnfreezeOccured()
            }
        }

    fun addListener(listener: MultiPatientDataListener) {
        this.listeners.add(listener)
    }

    fun removeListener(listener: MultiPatientDataListener): Boolean {
        return this.listeners.remove(listener)
    }

    private fun notifyPatientLevelChanged(patient: Patient) {
        for (listener in this.listeners) {
            listener.patientLevelChanged(patient)
        }
    }

    private fun notifyFreezeOccured() {
        for (listener in this.listeners) {
            listener.freezeOccured()
        }
    }

    private fun notifyUnfreezeOccured() {
        for (listener in this.listeners) {
            listener.unfreezeOccured()
        }
    }

    private fun notifyScenarioStarted() {
        for (listener in this.listeners) {
            listener.scenarioStarted()
        }
    }

    private fun notifyScenarioStopped() {
        for (listener in this.listeners) {
            listener.scenarioStopped()
        }
    }


    override fun notifyVitalSignChanged(patient: Patient, vitalSign: VitalSign) {

    }

    override fun notifyLevelVitalSignChanged(patient: Patient, vitalSign: VitalSign) {
        Log.d(TAG, vitalSign.name + " from " + patient.name + " has changed to " + vitalSign.level)
        this.notifyPatientLevelChanged(patient)
    }

    /**
     * Get list of all patients
     *
     * @return
     */
    fun getPatients(): List<Patient> {
        return this.patients.toMutableList()
    }

    fun getPatient(number: Int): Patient? {
        for (p in this.patients) {
            if (p.id == number) return p
        }
        return null
    }

    fun addPatient(patient: Patient) {
        patients.add(patient)
        patient.addListener(this)
    }

    @Synchronized
    fun clearPatients() {
        for (patient in this.patients) {
            patient.removeListener(this)
            patient.clearVitalSigns()
        }
        this.patients.clear()
    }

    @Synchronized
    fun fromMpmGameMultiPatient(multiPatient : MultiPatient) {
        this.clearPatients()

        multiPatient.getPatients().forEach {
            this.addPatient(Patient(it))
        }
    }
}
