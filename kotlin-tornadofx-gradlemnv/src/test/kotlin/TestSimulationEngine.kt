package test

import controllers.SimulationEngine
import controllers.SimulationStateChangedInterface
import generators.ModelFactory
import models.*
import utils.OutputWriter
import java.lang.StringBuilder
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class TestSimulationEngine: PatientChangedInterface, SimulationStateChangedInterface {
    val outputWriter = OutputWriter()
    val sb = StringBuilder()

    override fun onPatientValueChanged(id: Int, vId: Int, oldValue: Double, newValue: Double, time: Int) {
        printMethod("onPatientValueChanged", id.toString(), vId.toString(), oldValue.toString(), newValue.toString(), time.toString())
    }

    override fun onPatientAlarmLevelChanged(id: Int, vId: Int, oldValue: Int, newValue: Int, time: Int) {
        printMethod("onPatientAlarmLevelChanged", id.toString(), vId.toString(), oldValue.toString(), newValue.toString(), time.toString())
    }

    override fun onPatientSensorFailure(id: Int, vId: Int, oldValue: Boolean, newValue: Boolean, time: Int) {
        printMethod("onPatientSensorFailure", id.toString(), vId.toString(), oldValue.toString(), newValue.toString(), time.toString())
    }

    override fun onPatientStateChanged(id: Int, oldValue: PatientState, newValue: PatientState, time: Int) {
        printMethod("onPatientStateChanged", id.toString(), oldValue.toString(), newValue.toString(), time.toString())
    }

    override fun onSimulationStarted() {
        printMethod("onSimulationStarted")
    }

    override fun onSimulationEnded() {
        printMethod("onSimulationEnded")
    }

    override fun onFreeze(customArgs: String?) {
        printMethod("onFreeze", customArgs ?: "null")
    }

    override fun onUnfreeze() {
        printMethod("onUnfreeze")
    }

    override fun onStep(oldT: Int, newT: Int, simulationEngine: SimulationEngine) {
        printMethod("onStep", oldT.toString(), newT.toString(), simulationEngine.toString())
    }

    override fun onPopup(customArgs: String?) {
        printMethod("onPopup", customArgs ?: "null")
    }

    fun printMethod(methodName: String, vararg arg: String) {
        sb.clear()
        sb.append(methodName)
        arg.forEach { sb.append(", $it") }
        System.err.println(sb)
        sb.append("\n")
        outputWriter.writer.write(sb.toString())
        outputWriter.writer.flush()
    }
}

fun main(args: Array<String>) {
    val testSimulationEngine = TestSimulationEngine()

    val simulationEngine = SimulationEngine(ExperimentBlock())

    val patients = ModelFactory.generatePatients(2)
    val data1 = mutableListOf(VitalSignDatum(0), VitalSignDatum(10, 20.0), VitalSignDatum(20, 100.0))
    val data2 = mutableListOf(VitalSignDatum(0), VitalSignDatum(10, 0.0), VitalSignDatum(20, 50.0))

    patients[0].vitalSigns[0].data.setAll(data1)
    patients[1].vitalSigns[1].data.setAll(data2)

    simulationEngine.patients().setAll(patients)
    simulationEngine.addSimulationStateChangedListener(testSimulationEngine)
    simulationEngine.connectPatientInterface(testSimulationEngine)

    val events = ModelFactory.getDefaultEvents()
    events.add(Event(2, 20, EventType.FREEZE, "some custom args for 2nd task"))

    simulationEngine.events().setAll(events)

    System.err.println(events)

    simulationEngine.startSimulation()

    thread {
        sleep(26000)
        simulationEngine.isFrozen = false
    }.run()

}