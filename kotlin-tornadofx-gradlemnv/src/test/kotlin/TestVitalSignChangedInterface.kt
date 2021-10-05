package test

import generators.DEFAULT_LOWER_ALARM_LEVELS
import generators.DEFAULT_UPPER_ALARM_LEVELS
import models.VitalSign
import models.VitalSignChangedInterface
import models.VitalSignDatum

class TestVitalSignChangedInterface : VitalSignChangedInterface {
    override fun onVitalSignChanged(id: Int, oldValue: Double, newValue: Double, time: Int) {
        printMethod("onVitalSignChanged", id, oldValue, newValue, time)
    }

    override fun onLevelChanged(id: Int, oldValue: Int, newValue: Int, time: Int) {
        printMethod("onLevelChanged", id, oldValue, newValue, time)
    }

    override fun onSensorFailure(id: Int, oldValue: Boolean, newValue: Boolean, time: Int) {
        printMethod("onSensorFailure", id, oldValue, newValue, time)
    }

    fun printMethod(funName: String, id: Int, oldValue: Any, newValue: Any, time: Int) {
        System.err.println("$funName:: $id: $oldValue -> $newValue (@$time)")
    }
}

fun main(args: Array<String>) {
    val testVitalSignChangedInterface = TestVitalSignChangedInterface()

    val vitalSign = VitalSign()
    vitalSign.lowerAlarmLevels = DEFAULT_LOWER_ALARM_LEVELS
    vitalSign.upperAlarmLevels = DEFAULT_UPPER_ALARM_LEVELS
    val data = mutableListOf(VitalSignDatum(0), VitalSignDatum(10, 20.0), VitalSignDatum(20, 100.0))
    vitalSign.data.setAll(data)

    vitalSign.vitalSignChangedInterface = testVitalSignChangedInterface

    for (i in 0..100) {
        vitalSign.step()
        if (i == 80) {
            vitalSign.sensorFailureVal = 25.0
            vitalSign.isSensorFailure = true
        }
        if (i == 90) vitalSign.isSensorFailure = false
    }
}

/** Output
 *
onVitalSignChanged:: 0: 50.0 -> 47.0 (@1)
onVitalSignChanged:: 0: 47.0 -> 44.0 (@2)
onVitalSignChanged:: 0: 44.0 -> 41.0 (@3)
onVitalSignChanged:: 0: 41.0 -> 38.0 (@4)
onLevelChanged:: 0: 0 -> 1 (@4)
onVitalSignChanged:: 0: 38.0 -> 35.0 (@5)
onVitalSignChanged:: 0: 35.0 -> 32.0 (@6)
onVitalSignChanged:: 0: 32.0 -> 29.0 (@7)
onLevelChanged:: 0: 1 -> 2 (@7)
onVitalSignChanged:: 0: 29.0 -> 26.0 (@8)
onVitalSignChanged:: 0: 26.0 -> 23.0 (@9)
onVitalSignChanged:: 0: 23.0 -> 20.0 (@10)
onVitalSignChanged:: 0: 20.0 -> 28.0 (@11)
onVitalSignChanged:: 0: 28.0 -> 36.0 (@12)
onLevelChanged:: 0: 2 -> 1 (@12)
onVitalSignChanged:: 0: 36.0 -> 44.0 (@13)
onLevelChanged:: 0: 1 -> 0 (@13)
onVitalSignChanged:: 0: 44.0 -> 52.0 (@14)
onVitalSignChanged:: 0: 52.0 -> 60.0 (@15)
onVitalSignChanged:: 0: 60.0 -> 68.0 (@16)
onLevelChanged:: 0: 0 -> 1 (@16)
onVitalSignChanged:: 0: 68.0 -> 76.0 (@17)
onLevelChanged:: 0: 1 -> 2 (@17)
onVitalSignChanged:: 0: 76.0 -> 84.0 (@18)
onLevelChanged:: 0: 2 -> 3 (@18)
onVitalSignChanged:: 0: 84.0 -> 92.0 (@19)
onVitalSignChanged:: 0: 92.0 -> 100.0 (@20)
onVitalSignChanged:: 0: 100.0 -> 108.0 (@21)
onVitalSignChanged:: 0: 108.0 -> 116.0 (@22)
onVitalSignChanged:: 0: 116.0 -> 124.0 (@23)
onVitalSignChanged:: 0: 124.0 -> 132.0 (@24)
onVitalSignChanged:: 0: 132.0 -> 140.0 (@25)
onVitalSignChanged:: 0: 140.0 -> 148.0 (@26)
onVitalSignChanged:: 0: 148.0 -> 156.0 (@27)
onVitalSignChanged:: 0: 156.0 -> 164.0 (@28)
onVitalSignChanged:: 0: 164.0 -> 172.0 (@29)
onVitalSignChanged:: 0: 172.0 -> 180.0 (@30)
onVitalSignChanged:: 0: 180.0 -> 188.0 (@31)
onVitalSignChanged:: 0: 188.0 -> 196.0 (@32)
onVitalSignChanged:: 0: 196.0 -> 204.0 (@33)
onVitalSignChanged:: 0: 204.0 -> 212.0 (@34)
onVitalSignChanged:: 0: 212.0 -> 220.0 (@35)
onVitalSignChanged:: 0: 220.0 -> 228.0 (@36)
onVitalSignChanged:: 0: 228.0 -> 236.0 (@37)
onVitalSignChanged:: 0: 236.0 -> 244.0 (@38)
onVitalSignChanged:: 0: 244.0 -> 252.0 (@39)
onVitalSignChanged:: 0: 252.0 -> 260.0 (@40)
onVitalSignChanged:: 0: 260.0 -> 268.0 (@41)
onVitalSignChanged:: 0: 268.0 -> 276.0 (@42)
onVitalSignChanged:: 0: 276.0 -> 284.0 (@43)
onVitalSignChanged:: 0: 284.0 -> 292.0 (@44)
onVitalSignChanged:: 0: 292.0 -> 300.0 (@45)
onVitalSignChanged:: 0: 300.0 -> 308.0 (@46)
onVitalSignChanged:: 0: 308.0 -> 316.0 (@47)
onVitalSignChanged:: 0: 316.0 -> 324.0 (@48)
onVitalSignChanged:: 0: 324.0 -> 332.0 (@49)
onVitalSignChanged:: 0: 332.0 -> 340.0 (@50)
onVitalSignChanged:: 0: 340.0 -> 348.0 (@51)
onVitalSignChanged:: 0: 348.0 -> 356.0 (@52)
onVitalSignChanged:: 0: 356.0 -> 364.0 (@53)
onVitalSignChanged:: 0: 364.0 -> 372.0 (@54)
onVitalSignChanged:: 0: 372.0 -> 380.0 (@55)
onVitalSignChanged:: 0: 380.0 -> 388.0 (@56)
onVitalSignChanged:: 0: 388.0 -> 396.0 (@57)
onVitalSignChanged:: 0: 396.0 -> 404.0 (@58)
onVitalSignChanged:: 0: 404.0 -> 412.0 (@59)
onVitalSignChanged:: 0: 412.0 -> 420.0 (@60)
onVitalSignChanged:: 0: 420.0 -> 428.0 (@61)
onVitalSignChanged:: 0: 428.0 -> 436.0 (@62)
onVitalSignChanged:: 0: 436.0 -> 444.0 (@63)
onVitalSignChanged:: 0: 444.0 -> 452.0 (@64)
onVitalSignChanged:: 0: 452.0 -> 460.0 (@65)
onVitalSignChanged:: 0: 460.0 -> 468.0 (@66)
onVitalSignChanged:: 0: 468.0 -> 476.0 (@67)
onVitalSignChanged:: 0: 476.0 -> 484.0 (@68)
onVitalSignChanged:: 0: 484.0 -> 492.0 (@69)
onVitalSignChanged:: 0: 492.0 -> 500.0 (@70)
onVitalSignChanged:: 0: 500.0 -> 508.0 (@71)
onVitalSignChanged:: 0: 508.0 -> 516.0 (@72)
onVitalSignChanged:: 0: 516.0 -> 524.0 (@73)
onVitalSignChanged:: 0: 524.0 -> 532.0 (@74)
onVitalSignChanged:: 0: 532.0 -> 540.0 (@75)
onVitalSignChanged:: 0: 540.0 -> 548.0 (@76)
onVitalSignChanged:: 0: 548.0 -> 556.0 (@77)
onVitalSignChanged:: 0: 556.0 -> 564.0 (@78)
onVitalSignChanged:: 0: 564.0 -> 572.0 (@79)
onVitalSignChanged:: 0: 572.0 -> 580.0 (@80)
onVitalSignChanged:: 0: 580.0 -> 588.0 (@81)
onSensorFailure:: 0: false -> true (@81)
onVitalSignChanged:: 0: 588.0 -> 0.0 (@82)
 */
