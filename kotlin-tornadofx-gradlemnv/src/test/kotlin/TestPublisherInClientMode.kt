package test

import controllers.SimulationEngine
import generators.ModelFactory
import models.VitalSignChangedInterface
import models.VitalSignDatum
import networking.Client
import org.json.JSONObject

private fun VitalSignChangedInterface.initSimulationEngine(simulationEngine: SimulationEngine) {
    val patients = ModelFactory.generatePatients(2)
    val data1 = mutableListOf(VitalSignDatum(0), VitalSignDatum(10, 20.0), VitalSignDatum(20, 100.0))
    val data2 = mutableListOf(VitalSignDatum(0), VitalSignDatum(10, 0.0), VitalSignDatum(20, 50.0))

    patients[0].vitalSigns[0].data.setAll(data1)
    patients[0].vitalSigns[0].vitalSignChangedInterface = this
    patients[1].vitalSigns[1].data.setAll(data2)
    patients[0].vitalSigns[1].vitalSignChangedInterface = this

    simulationEngine.patients().setAll(patients)

    simulationEngine.events().setAll(ModelFactory.getDefaultEvents())
}

fun main() {
    /** IMPORTANT: Start NetworkingServiceMonitor before you run this **/
    val pretendMPMcontrollerInClientMode = Client()
    pretendMPMcontrollerInClientMode.setClientName("MPM Controller")

    // We sort of pretend we are mpmgame
    // this is how it could be done using the controller's change interface
    val vitalSignChangedInterface = object : VitalSignChangedInterface {
        override fun onVitalSignChanged(id: Int, oldValue: Double, newValue: Double, time: Int) {
            if (pretendMPMcontrollerInClientMode.isActive()) {
                pretendMPMcontrollerInClientMode.sendMessage(
                    JSONObject("{\"type\": \"mpmgame\", \"content\": {\"id\": $id, \"time\": $time, \"value\": $newValue}}"))
            }
        }

        override fun onLevelChanged(id: Int, oldValue: Int, newValue: Int, time: Int) {}
        override fun onSensorFailure(id: Int, oldValue: Boolean, newValue: Boolean, time: Int) {}
    }

    val pretendMPMControllerThread = Thread {
        pretendMPMcontrollerInClientMode.connect()
    }
    pretendMPMControllerThread.start()

    Thread.sleep(2000)

    // run simulation
    val simulationEngine = SimulationEngine(ExperimentBlock())
    vitalSignChangedInterface.initSimulationEngine(simulationEngine)
    simulationEngine.startSimulation()

    // time to run simulation
    Thread.sleep(30 * 1000)

    pretendMPMcontrollerInClientMode.setClientName("TEMP")

    // time to run simulation
    Thread.sleep(30 * 1000)

    pretendMPMcontrollerInClientMode.setClientName("OVER AND OUT")

    // time to run simulation
    Thread.sleep(2 * 1000)

    // stop simulation
    simulationEngine.simulationTimer?.cancel()

    // shut down pretend mpm controller
    pretendMPMcontrollerInClientMode.disconnect()
}
