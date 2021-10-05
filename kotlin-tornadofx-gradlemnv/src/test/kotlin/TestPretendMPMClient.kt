package test

import networking.Client
import networking.ClientListener
import org.json.JSONException
import org.json.JSONObject
import publisher.JSON_MESSAGE_TYPE
import publisher.MESSAGE_TYPE

fun main() {
    /** IMPORTANT: Relies on NetworkingServiceMonitor and TestPublisherInClientMode **/
    val actualPretendMPMClient = Client()
    actualPretendMPMClient.setClientName("MPM Game Client")
    actualPretendMPMClient.addClientListener(object : ClientListener {
        override fun onServerConnect() {
            println(utils.createInfoJson("pretend mpm client connected"))
        }
        override fun onClientMessage(message: String) {
            println(utils.createInfoJson(message))
        }
        override fun onServerDisconnect() {
            println(utils.createInfoJson("pretend mpm client disconnected"))
        }
        override fun onServerMessage(message: String) {
            println("Received message: $message")

            try {
                val json = JSONObject(message)
                if (json.getString(JSON_MESSAGE_TYPE) == MESSAGE_TYPE.MPMGAME.getType()) {
                    actualPretendMPMClient.sendMessage(
                        JSONObject(
                            utils.createJsonForGivenType(MESSAGE_TYPE.MPMGAME, "Thank you for the message pretend mpm controller")))
                }
            } catch (ex : JSONException) {
                println("hey you sent me bad json...")
            }
        }
    })

    // start client
    Thread {
        actualPretendMPMClient.connect()
    }.start()

    // time to run simulation
    Thread.sleep(15 * 1000)

    actualPretendMPMClient.setClientName("Renamed Client")

    Thread.sleep(5 * 1000)

    actualPretendMPMClient.addMetaInfo("name", null)
    actualPretendMPMClient.addMetaInfo("test", "some info")
    actualPretendMPMClient.sendSubscriberUpdate()

    Thread.sleep(5 * 1000)

    actualPretendMPMClient.addMetaInfo("test", "some info changed")
    actualPretendMPMClient.sendSubscriberUpdate()

    Thread.sleep(5 * 1000)

    actualPretendMPMClient.addMetaInfo("test", null)
    actualPretendMPMClient.sendSubscriberUpdate()

    Thread.sleep(5 * 1000)

    // shut down pretend mpm client
    actualPretendMPMClient.disconnect()
}
