package cerg.mnv.service


import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import cerg.mnv.utils.SoundDownloads
import mpmgame.MpmGameMessageRequest
import mpmgame.MpmGameMessageTypes
import networking.Client
import networking.ClientListener
import org.json.JSONObject
import publisher.MESSAGE_TYPE
import transfer.BinaryTransferTypes
import utils.createJsonForGivenType
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "Cerg Network Service"

/**
 * Unused at the moment but could be used to run
 * networking communication as a service
 */
class NetworkingService : Service() {
    var server = "localhost"
    var name = Build.MODEL ?: "NetworkingService"

    private var client : Client? = null
    private val clientListeners = Collections.newSetFromMap(ConcurrentHashMap<ClientListener, Boolean>())
    private var defaultClientListener = object : ClientListener {
        override fun onClientMessage(message: String) {
            Log.i(TAG, "Client Message: $message")
        }
        override fun onServerDisconnect() {
            Log.i(TAG, "Server disconnect")
        }
        override fun onServerMessage(message: String) {
            Log.i(TAG, "Server Message: $message")
        }
        override fun onServerConnect() {
            Log.i(TAG, "Server connect")
        }
    }

    private val networkingServiceBinder = NetworkingServiceBinder()
    inner class NetworkingServiceBinder : Binder() {
        fun getService() : NetworkingService {
            return this@NetworkingService
        }
    }

    override fun onCreate() {
        Log.i(TAG, "$TAG onCreate")
    }

    override fun onDestroy() {
        Log.i(TAG, "$TAG onDestroy")
        SoundDownloads.stopCheckingForDownloads()
        SoundDownloads.stopSoundDownloads(null)
    }

    fun restartService() {
        var metaInfo : Map<String, String>? = null
        if (this.client != null) {
            this.client?.clearClientListeners()
            this.client?.addMetaInfo("name", this.name)
            metaInfo = this.client?.getMetaInfo()
            this.client?.disconnect()
        }

        Thread {
            this.client = Client(this.server)
            this.syncMetaInfo(metaInfo)
            if (this@NetworkingService.clientListeners.isEmpty()) {
                this.client?.addClientListener(defaultClientListener)
            } else {
                this@NetworkingService.clientListeners.forEach {
                    this.client?.addClientListener(it)
                }
            }
            this.client?.connect()
        }.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "$TAG is starting networking")

        if (this.client == null ||
                (this.client?.isActive() == false && this.client?.isConnecting == false)) this.restartService()

        return super.onStartCommand(intent, flags, startId)
    }

    fun syncMetaInfo(metaInfo : Map<String, String>?) : Boolean {
        var hasPatientFilterChanged = false
        if (metaInfo != null) {
            val oldPatientFilterValue = this.client?.getMetaInfo()?.get("patientFilter")
            hasPatientFilterChanged = oldPatientFilterValue != metaInfo["patientFilter"]
            this.client?.syncMetaInfo(metaInfo)
        }
        if (metaInfo == null || !metaInfo.containsKey("name")) this.client?.addMetaInfo("name", this.name)
        else this.name = metaInfo.getValue("name")
        return hasPatientFilterChanged
    }

    override fun onBind(intent: Intent): IBinder {
        Log.i(TAG, "$TAG bound")
        return this.networkingServiceBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // for now we also disconnect when service is unbound
        // since the only user is out local same process activity
        this.client?.disconnect()
        Log.i(TAG, "$TAG unbound")
        return super.onUnbind(intent)
    }

    fun isNetworkActive() : Boolean {
        return this.client?.isActive() ?: false
    }

    fun sendMessage(json : JSONObject) {
        this.client?.sendMessage(json)
    }

    fun sendMpmGameMessageRequest(mpmGameMessageType: MpmGameMessageTypes) {
        this.sendMessage(mpmgame.createMpmGameJson(MpmGameMessageRequest(mpmGameMessageType).toJSONObject(),
                MpmGameMessageTypes.MPMGAME_MESSAGE_REQUEST))
    }

    fun sendBinaryTransferMessage(transferType: BinaryTransferTypes, jsonObject: JSONObject) {
        val transferWrapper = JSONObject("{\"type\":${transferType.getId()}}")
        transferWrapper.put("message", jsonObject)
        this.sendMessage(this.wrapMesageType(MESSAGE_TYPE.TRANSFER, transferWrapper))
    }

    private fun wrapMesageType(messageType: MESSAGE_TYPE, jsonObject: JSONObject) : JSONObject {
        val wrapper = JSONObject(createJsonForGivenType(messageType, ""))
        wrapper.put("content", jsonObject)
        return wrapper
    }


    fun sendMessageOfType(messageType: MESSAGE_TYPE, jsonObject: JSONObject) {
        this.sendMessage(this.wrapMesageType(messageType, jsonObject))
    }

    fun addClientListener(listener: ClientListener) {
        this.clientListeners.add(listener)
        this.client?.addClientListener(listener)
    }

    fun removeClientListener(listener: ClientListener) {
        this.clientListeners.remove(listener)
        this.client?.removeClientListener(listener)
    }
}
