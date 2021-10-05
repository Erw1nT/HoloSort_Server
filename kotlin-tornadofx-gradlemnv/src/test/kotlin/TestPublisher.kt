package test

import controllers.SimulationEngine
import generators.ModelFactory
import models.VitalSignChangedInterface
import models.VitalSignDatum
import networking.Client
import org.json.JSONObject
import publisher.LocalSubscriber
import publisher.Publisher

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
    // test case: run publisher for a minute
    Publisher.startNetworking()

    // interface for vital sign listener
    val vitalSignChangedInterface = object : VitalSignChangedInterface {
        override fun onVitalSignChanged(id: Int, oldValue: Double, newValue: Double, time: Int) {
            Publisher.publish(JSONObject(
                "{\"type\": \"broadcast\", \"content\": {\"id\": $id, \"time\": $time, \"value\": $newValue}}"))
        }

        override fun onLevelChanged(id: Int, oldValue: Int, newValue: Int, time: Int) {}
        override fun onSensorFailure(id: Int, oldValue: Boolean, newValue: Boolean, time: Int) {}
    }

    val simulationEngine = SimulationEngine(ExperimentBlock())
    vitalSignChangedInterface.initSimulationEngine(simulationEngine)

    // 2 local subscribers
    // for remote connections use Client
    Publisher.subscribe(LocalSubscriber("Local Subscriber 1"))
    Publisher.subscribe(LocalSubscriber("Local Subscriber 2"))

    // 2 remote clients in background
    val remoteClientThread1 = Thread {Client().connect()}
    val remoteClientThread2 = Thread {Client().connect()}
    remoteClientThread1.start()
    remoteClientThread2.start()

    // give clients time to connect...
    Thread.sleep(5*1000)

    // broadcast message
    Publisher.publish(JSONObject(
        "{\"type\": \"broadcast\", \"content\": {\"what\": \"some important thingy\", \"what else\": \"no clue\"}}"))

    // run simulation
    simulationEngine.startSimulation()

    // broadcast time
    for (i in 0..10) {
        Publisher.publish(JSONObject(
            ("{\"type\": \"broadcast\", \"content\": {\"what\": \"time\", \"now\": ${System.currentTimeMillis()}}}")))
        Thread.sleep(1000)
    }

    // interrupt remote clients
    remoteClientThread1.interrupt()
    remoteClientThread2.interrupt()

    // stop simulation timer and publisher
    simulationEngine.simulationTimer?.cancel()

    // stop publisher
    Publisher.stopPublisher()
}
