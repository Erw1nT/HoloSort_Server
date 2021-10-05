package networking

import models.Patient
import mpmgame.*
import org.json.JSONObject
import publisher.MESSAGE_TYPE
import publisher.Subscriber
import transfer.SoundInitializationRequest
import transfer.SoundToInitialize
import utils.SoundVal
import java.io.File

object Converter {
    fun createPatientValuesMessage(
        patients : List<Patient>, subscriber: Subscriber? = null) : JSONObject {
        val multiPatients = MultiPatient()
        /*patients.forEach {
            val p = mpmgame.Patient(it.id, it.name)
            it.vitalSigns.forEach { vital ->
                val v = VitalSign(vital.id, vital.name)
                v.value = vital.displayVal
                vital.lowerAlarmLevels.forEach { lowerAlarms ->
                    v.lowerAlarmLevels.add(lowerAlarms.level)
                }
                vital.upperAlarmLevels.forEach { upperAlarms ->
                    v.upperAlarmLevels.add(upperAlarms.level)
                }
                p.addVitalSign(v)
            }
            // skip the ones that are not accepted by the optional filter
            if (subscriber == null || subscriber.acceptsPatient(p.id)) {
                multiPatients.addPatient(p)
            }
        }*/

        //should we have no patients we return an empty json
        if (multiPatients.getNumberOfPatients() == 0) return JSONObject()

        return createMpmGameJson(multiPatients.toJSONArray(), MpmGameMessageTypes.PATIENT_VALUES_INIT)
    }

    fun createPatientValueChangedMessage(patientId : Int, vitalSignId: Int, value: Double) : JSONObject {
        return createMpmGameJson(
            VitalSignChange(patientId, vitalSignId, value).toJSONObject(), MpmGameMessageTypes.PATIENT_VALUE_CHANGED)
    }

    fun createPlaySoundMessage(sound: Sound) : JSONObject {
        return createMpmGameJson(sound.toJSONObject(), MpmGameMessageTypes.PLAY_SOUND)
    }

    fun createSoundsInitializationMessage(soundsToInitialize: Iterable<SoundVal>) : JSONObject {
        val soundInitialization = SoundInitializationRequest()
        soundsToInitialize.forEach {
            val f = File(it.file)
            if (f.exists() && f.isFile) {
                val l = f.length()
                val n = it.name ?: f.name
                soundInitialization.addSoundToIninitalize(SoundToInitialize(
                    it.file, n, l, CheckSums.calculate(CheckSumType.MD5, it.file) ?: ""))
            }
        }
        return createMpmGameJson(soundInitialization.toJSONArray(), MpmGameMessageTypes.SOUNDS_INIT)    }

    fun createSimulationStateChangedMessage(simulationState: SimulationState) : JSONObject {
        return createMpmGameJson(
            SimulationStateChange(simulationState).toJSONObject(), MpmGameMessageTypes.SIMULATION_STATE_CHANGE)
    }

    fun createSensorChangedMessage(sensorState: SensorState) : JSONObject {
        return createMpmGameJson(SensorChange(sensorState).toJSONObject(), MpmGameMessageTypes.SENSOR_CHANGE)
    }

    fun createUpdateSubscriberMessage(metaData: MutableSet<MutableMap.MutableEntry<String, String?>>) : JSONObject {
        val subscriberUpdateMessage = JSONObject(utils.createJsonForGivenType(MESSAGE_TYPE.SUBSCRIBER, ""))
        val metaDataJson = JSONObject("{}")
        metaData.forEach {
            metaDataJson.put(it.key, it.value ?: JSONObject.NULL)
        }
        subscriberUpdateMessage.put("content", metaDataJson)
        return subscriberUpdateMessage
    }
}