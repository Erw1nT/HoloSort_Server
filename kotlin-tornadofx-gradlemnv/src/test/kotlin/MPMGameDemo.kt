package test

import controllers.GameEngine
import controllers.GameEngineInterface
import controllers.SimulationEngine
import controllers.SimulationStateChangedInterface
import generators.ModelFactory
import javafx.application.Platform
import models.*
import tornadofx.*
import java.lang.StringBuilder

class MPMGameDemoView: View(), PatientChangedInterface, SimulationStateChangedInterface, GameEngineInterface  {
    var gameEngine = GameEngine()
    val simulationEngine = SimulationEngine(ExperimentBlock())

    override val root = hbox(20) {
        button("Start") {
            action {
                gameEngine.openWindow()
                gameEngine.root.bringMonitorsToFront() // temporary fix because of WardView::InitView() makes other blocks appear above monitors
                gameEngine.wardController.moveClinician(FootDirection.NORTH)
            }
        }
    }

    override fun onStartGame() {
        simulationEngine.startSimulation()
    }

    override fun onExit() {
        Platform.exit()
    }

    override fun onFootPedalPressed() {

    }

    val patients = ModelFactory.generatePatients(2)

    init {
        val data1 = mutableListOf(VitalSignDatum(0), VitalSignDatum(10, 20.0), VitalSignDatum(20, 100.0))
        val data2 = mutableListOf(VitalSignDatum(0), VitalSignDatum(10, 0.0), VitalSignDatum(20, 50.0))

        patients[0].vitalSigns[0].data.setAll(data1)
        patients[1].vitalSigns[1].data.setAll(data2)

        simulationEngine.patients().setAll(patients)

        val events = ModelFactory.getDefaultEvents()
        simulationEngine.events().setAll(events)

        gameEngine.gameEngineInterface = this

        simulationEngine.addSimulationStateChangedListener(this)
        simulationEngine.connectPatientInterface(this)

        patients.forEachIndexed { ind, p ->
            gameEngine.root.monitors[ind]?.vitalSignLabels = Array(p.vitalSigns.size) {i -> p.vitalSigns[i].name}
            gameEngine.root.monitors[ind]?.setAlarms(
                Array(p.vitalSigns.size) {i -> p.vitalSigns[i].upperAlarmLevels.last().level},
                Array(p.vitalSigns.size) {i -> p.vitalSigns[i].lowerAlarmLevels.last().level}
            )
            gameEngine.root.alarmOffImageViews[ind]?.isVisible = false
            gameEngine.root.alarmOnImageViews[ind]?.isVisible = false
        }
    }

    val sb = StringBuilder()

    fun printMethod(methodName: String, vararg arg: Any) {
        sb.clear()
        sb.append(methodName)
        arg.forEach { sb.append(", $it") }
        System.err.println(sb)
    }

    override fun onPatientValueChanged(id: Int, vId: Int, oldValue: Double, newValue: Double, time: Int) {
        gameEngine.root.monitors[id]?.setValue(vId, newValue)
        printMethod("onPatientValueChanged", id, vId, oldValue, newValue, time)
    }

    override fun onPatientAlarmLevelChanged(id: Int, vId: Int, oldValue: Int, newValue: Int, time: Int) {
        gameEngine.root.monitors[id]?.alarmLevel = newValue
        gameEngine.root.patientRooms[id]?.patientState = patients[id].patientState
        gameEngine.root.alarmOnImageViews[id]?.isVisible = ((newValue > 0) && (gameEngine.monitorsVisibleWhenDoorsClosed || gameEngine.root.patientRooms[id]!!.isDoorOpen)) // see truth table in notes 28/11/2018
    }

    override fun onPatientSensorFailure(id: Int, vId: Int, oldValue: Boolean, newValue: Boolean, time: Int) {

    }

    override fun onPatientStateChanged(id: Int, oldValue: PatientState, newValue: PatientState, time: Int) {

    }

    override fun onSimulationStarted() {

    }

    override fun onSimulationEnded() {

    }

    override fun onFreeze(customArgs: String?) {

    }

    override fun onUnfreeze() {

    }

    override fun onStep(oldT: Int, newT: Int, simulationEngine: SimulationEngine) {

    }

    override fun onPopup(customArgs: String?) {

    }
}

class MPMGameDemo: App(MPMGameDemoView::class)

fun main(args: Array<String>) {
    launch<MPMGameDemo>()

}