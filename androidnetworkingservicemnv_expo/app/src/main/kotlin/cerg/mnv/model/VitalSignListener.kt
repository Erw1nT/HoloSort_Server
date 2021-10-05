package cerg.mnv.model

interface VitalSignListener {
    fun vitalSignChanged(vitalSign: VitalSign)
    fun vitalSignLevelChanged(vitalSign: VitalSign)
}
