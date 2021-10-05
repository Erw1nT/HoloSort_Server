package test

import models.EventType
import utils.ShortcodeParser

class TestShortcodeParser {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("P1V2 --> " + ShortcodeParser.checkForAPVPattern(EventType.SENSOR_FIX, "P1V2"))
            println("P1Vsw4r2 --> " + ShortcodeParser.checkForAPVPattern(EventType.SENSOR_FIX, "P1Vsw4r2"))
            println("P1V --> " + ShortcodeParser.checkForAPVPattern(EventType.SENSOR_FIX, "P1V"))
            println("PV --> " + ShortcodeParser.checkForAPVPattern(EventType.SENSOR_FIX, "PV"))
            println("PAV1 --> " + ShortcodeParser.checkForAPVPattern(EventType.SENSOR_FIX, "PAV1"))

            println("P1V2 VAL:12 --> " + ShortcodeParser.checkForAPVPattern(EventType.SENSOR_FAILURE, "P1V2 VAL:12"))
            println("P1V2 VAL12 --> " + ShortcodeParser.checkForAPVPattern(EventType.SENSOR_FAILURE, "P1V2 VAL12"))
            println("P1V2 VAL 12 --> " + ShortcodeParser.checkForAPVPattern(EventType.SENSOR_FAILURE, "P1V2 VAL 12"))
            println("P1V2 VAL 12. --> " + ShortcodeParser.checkForAPVPattern(EventType.SENSOR_FAILURE, "P1V2 VAL 12."))
            println("P1V2 VAL 12.23 --> " + ShortcodeParser.checkForAPVPattern(
                EventType.SENSOR_FAILURE,
                "P1V2 VAL 12.23"
            )
            )
            println("P1V2 VA 12.23 --> " + ShortcodeParser.checkForAPVPattern(
                EventType.SENSOR_FAILURE,
                "P1V2 VA 12.23"
            )
            )

            println("P1V2 --> " + ShortcodeParser.checkForAPVPattern(EventType.TREATMENT, "P1V2"))
            println("P1VA TIME 02 --> " + ShortcodeParser.checkForAPVPattern(EventType.TREATMENT, "P1VA TIME 02"))
            println("P1V --> " + ShortcodeParser.checkForAPVPattern(EventType.TREATMENT, "P1V"))
            println("PV --> " + ShortcodeParser.checkForAPVPattern(EventType.TREATMENT, "PV"))

            println("P1V2 --> " + ShortcodeParser.checkForAPVPattern(EventType.PERTURBATION, "P1V2"))
            println("P1VA VAL 100 TIME 100 --> " + ShortcodeParser.checkForAPVPattern(
                EventType.PERTURBATION,
                "P1VA VAL 100 TIME 100"
            )
            )
            println("P1V --> " + ShortcodeParser.checkForAPVPattern(EventType.PERTURBATION, "P1V"))
            println("PV --> " + ShortcodeParser.checkForAPVPattern(EventType.PERTURBATION, "PV"))
        }
    }
}