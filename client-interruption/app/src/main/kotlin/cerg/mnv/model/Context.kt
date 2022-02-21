package cerg.mnv.model

import transfer.SoundInitializationRequest

object Context {
    var patientsInitialized = false
    var lastPatientsInitializationRequest = 0L
    var soundsInitialized = false
    var lastSoundsInitializationRequest = 0L
    var latestSoundInitalizationRequest : SoundInitializationRequest? = null
}