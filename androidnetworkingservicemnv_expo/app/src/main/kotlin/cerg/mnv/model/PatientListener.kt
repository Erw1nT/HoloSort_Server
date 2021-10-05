package cerg.mnv.model

interface PatientListener {
    fun notifyVitalSignChanged(patient: Patient, vitalSign: VitalSign)
    fun notifyLevelVitalSignChanged(patient: Patient, vitalSign: VitalSign)
}
