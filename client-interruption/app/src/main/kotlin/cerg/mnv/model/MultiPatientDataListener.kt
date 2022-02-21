package cerg.mnv.model

interface MultiPatientDataListener {
    fun patientChanged(patient: Patient)
    fun patientLevelChanged(patient: Patient)
    fun freezeOccured()
    fun unfreezeOccured()
    fun scenarioStarted()
    fun scenarioStopped()
}
