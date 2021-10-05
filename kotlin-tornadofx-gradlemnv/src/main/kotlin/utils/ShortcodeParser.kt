package utils

/*import models.EventType
import models.VitalSign

class ShortcodeParser {
    companion object {
        fun parseEventItem(event: Event, experimentBlock: ExperimentBlock) : ParseResult {
            val parseResult = ParseResult()
            //TODO: need to add parsing for freeze
            if (event.eventType == EventType.START || event.eventType == EventType.END ||
                event.eventType == EventType.SOUNDS || event.eventType == EventType.TIMER ||
                event.eventType == EventType.EMPTY || event.eventType == EventType.FREEZE) {
                    parseResult.isError = false
                    parseResult.message = "No detailed parsing required."
                    return parseResult
            }

            val apvPattern = checkForAPVPattern(event.eventType, event.customArgs)
            if (!apvPattern.isWellFormed) {
                parseResult.isError = true
                parseResult.message = apvPattern.message
                return parseResult
            }

            try {
                when(Pair(apvPattern.isAllPatients, apvPattern.isAllVitalSigns)) {
                    Pair(true, true) -> { experimentBlock.patients.forEach { p -> parseResult.vitalSignsAffected.addAll(p.vitalSigns) } }
                    Pair(true, false) -> { experimentBlock.patients.forEach { p -> parseResult.vitalSignsAffected.add(p.vitalSigns[apvPattern.vitalSignNumber]) } }
                    Pair(false, true) -> { parseResult.vitalSignsAffected.addAll(experimentBlock.patients[apvPattern.patientNumber].vitalSigns) }
                    Pair(false, false) -> { parseResult.vitalSignsAffected.add(experimentBlock.patients[apvPattern.patientNumber].vitalSigns[apvPattern.vitalSignNumber]) }
                }

                parseResult.dataValue = apvPattern.dataValue
                parseResult.trendTime = apvPattern.trendTime
            } catch (ex : Exception) {
                parseResult.isError = true
                parseResult.message = "Exception thrown: " + ex.toString()
            }

            return parseResult
        }

        /**
         * A - all, P - patient #, V - vital sign #
         * VAL ### - the value of the sensor failure, etc
         * Format: P#V# VAL# TIME#
         * - PAVA - all patients, all vital signs
         * - PAV1 - all patients, vital sign 1
         * - P1VA - patient 1, all vital signs
         * - P1V1 - patient 1, vital sign 1
         *
         * For SENSOR_FIX - only need P#V#
         * For SENSOR_FAILURE - need P#V# VAL#
         * For TREATMENT - need P#V# TIME#
         * For PERTURBATION - need P#V# VAL# TIME#
         */
        fun checkForAPVPattern(eventType: EventType, eventArgs: String?) : APVPattern {
            val result = APVPattern()
            if (eventArgs == null || eventArgs.isBlank()) {
                result.message = "Event arguments are empty."
                return result
            }
            var regexString = "P(\\s)?(A|\\d+)(\\s)?V(\\s)?(A|\\d+)"

            if (eventType == EventType.SENSOR_FAILURE || eventType == EventType.PERTURBATION) {
                regexString += "(\\s)?VAL(\\s)?\\d+(\\.\\d+)?"
            }
            if (eventType == EventType.TREATMENT || eventType == EventType.PERTURBATION) {
                regexString += "(\\s)?TIME(\\s)?\\d+"
            }

            val regex = regexString.toRegex()

            if (regex.matches(eventArgs)) {
                result.isWellFormed = true
                val patientData = getPatientData(eventArgs)
                result.isAllPatients = patientData.first
                result.patientNumber = patientData.second - 1

                val vitalSignData = getVitalSignData(eventArgs)
                result.isAllVitalSigns = vitalSignData.first
                result.vitalSignNumber = vitalSignData.second - 1

                if (eventType == EventType.SENSOR_FAILURE || eventType == EventType.PERTURBATION) {
                    result.dataValue = getValue(eventArgs)
                }

                if (eventType == EventType.TREATMENT || eventType == EventType.PERTURBATION) {
                    result.trendTime = getTime(eventArgs)
                }

                result.message = "Pattern parsed without errors."

            } else {
                result.message = "Pattern does not contain valid Patient or Vital Sign information."
            }

            return result
        }

        fun getPatientData (eventArgs: String) : Pair<Boolean, Int> {
            val regex = "P(\\s)?(A|\\d+)".toRegex()
            val match = regex.find(eventArgs)
            val patientNumberString = match!!.value.substring(1).trim()
            return if (patientNumberString == "A") Pair(true, -1)
            else Pair(false, patientNumberString.toInt())
        }

        fun getVitalSignData (eventArgs: String) : Pair<Boolean, Int> {
            val regex = "V(\\s)?(A|\\d+)(?!L)".toRegex() // avoid VAL keyword: (?!L)
            val match = regex.find(eventArgs)
            val vitalSignNumberString = match!!.value.substring(1).trim()
            return if (vitalSignNumberString == "A") Pair(true, -1)
            else Pair(false, vitalSignNumberString.toInt())
        }

        fun getValue (eventArgs: String) : Double {
            val regex = "VAL(\\s)?\\d+(\\.\\d+)?".toRegex()
            val match = regex.find(eventArgs)
            val valueString = match!!.value.substring(3).trim()
            return valueString.toDouble()
        }

        fun getTime (eventArgs: String) : Int {
            val regex = "TIME(\\s)?\\d+".toRegex()
            val match = regex.find(eventArgs)
            val timeString = match!!.value.substring(4).trim()
            return timeString.toInt()
        }

    }
}

data class ParseResult (var isError: Boolean = false, var message: String = "",
                        var vitalSignsAffected: MutableList<VitalSign> = mutableListOf(),
                        var dataValue: Double = 0.0, var trendTime: Int = 0)

data class APVPattern(var isWellFormed : Boolean = false, var isAllPatients : Boolean = false,
                      var isAllVitalSigns : Boolean = false, var patientNumber : Int = -1,
                      var vitalSignNumber : Int = -1, var dataValue : Double = 0.0,
                      var trendTime : Int = 0, var message : String = "")


 */
