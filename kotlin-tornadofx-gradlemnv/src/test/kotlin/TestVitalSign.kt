package test

import javafx.collections.FXCollections
import junit.framework.Assert
import models.VitalSign
import models.VitalSignDatum

class TestSource {
    /**
     * Test to see if json generator works correctly
     */
//    @test fun f1() {
//        val vitalSign = VitalSign()
//
//        val id = 0
//        val name = "HR"
//        val description = "Heart Rate"
//        val vitalSignCode = VitalSignCode.HR
//
//        with(vitalSign) {
//            this.id = id
//            this.name = name
//            this.description = description
//            this.vitalSignCode = vitalSignCode
//        }
//
//        assertEquals(vitalSign.toJSON().toString(), "{\"id\":$id,\"name\":\"$name\",\"description\":\"$description\",\"vitalSignCode\":\"${vitalSignCode.name}\"" +
//                ",\"lowerAlarmLevels\":[{\"level\":40.0},{\"level\":30.0},{\"level\":20.0}],\"upperAlarmLevels\":[{\"level\":60.0},{\"level\":70.0},{\"level\":80.0}]}")
//    }

    /**
     * Test to check if model loads correctly from json
     */
//    @test fun f2() {
//        val vitalSign = VitalSign()
//
//        val id = 0
//        val name = "BP"
//        val description = "Blood Pressure"
//        val vitalSignCode = VitalSignCode.BP
//
//        with(vitalSign) {
//            this.id = id
//            this.name = name
//            this.description = description
//            this.vitalSignCode = vitalSignCode
//        }
//
//        val vitalSignFromJson = VitalSign()
//        vitalSignFromJson.updateModel(vitalSign.toJSON())
//
//        assertEquals(vitalSign.toJSON().toString(), vitalSignFromJson.toJSON().toString())
//    }

    @org.junit.jupiter.api.Test
    fun testVitalSignStep() {
        val data = FXCollections.observableArrayList<VitalSignDatum>(VitalSignDatum(0), VitalSignDatum(10, 60.0), VitalSignDatum(20, 80.0))
        val vitalSign = VitalSign()
        vitalSign.data = data
        for (t in 0..30) {
            vitalSign.step()
            println("t: ${vitalSign.currentTime}, val: ${vitalSign.currentVal}")
            if (t == 9) {
                Assert.assertEquals(vitalSign.currentTime,10)
                Assert.assertEquals(vitalSign.currentVal,60.0)
            }
            if (t == 19) {
                Assert.assertEquals(vitalSign.currentTime, 20)
                Assert.assertEquals(vitalSign.currentVal,80.0)
            }
        }
    }

}