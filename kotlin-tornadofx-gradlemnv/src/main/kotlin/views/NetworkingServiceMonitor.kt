package views

import javafx.application.Application
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.Alert
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.Priority
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.util.Duration
import logging.GlobalLogger
import networking.Converter
import networking.NetworkingChangeListener
import networking.NetworkingState
import networking.Server
import org.json.JSONArray
import org.json.JSONObject
import publisher.*
import tornadofx.*
import utils.ValueChangeDialog
import java.awt.Desktop
import java.awt.MouseInfo
import java.awt.Robot
import java.awt.Toolkit
import java.io.File
import java.util.*

class NetworkingServiceMonitor : View() {
    private var isItsOwnApp = app.parameters.unnamed.contains("isItsOwnApp")

    private var isStarted = SimpleBooleanProperty(false)
    private val monitor = Monitor()
    private var networkingState = SimpleObjectProperty(Publisher.getNetworkingState())
    private val isNetworkingActiveBinding = Bindings.equal(this.networkingState, NetworkingState.ACTIVE)

    private var localIps = SimpleStringProperty("N/A")

    var startTime = ""
    var endTime = ""
    private val monitorNotRunningDummySubscriber = LocalSubscriber("Monitor not Running")

    var file = File("src/main/resources/wavs/alerttone2.wav")
    var path = file.absolutePath
    val alerttone = Media(File(path).toURI().toURL().toString())
    val mediaPlayer = MediaPlayer(alerttone)



    private val subscriberTable = tableview<Subscriber> {
        readonlyColumn("ID", Subscriber::key).pctWidth(40)
        readonlyColumn("Name", Subscriber::name).pctWidth(60)
        items.add(monitorNotRunningDummySubscriber)

        smartResize()

        multiSelect(true)

        contextmenu {
            enableWhen(isNetworkingActiveBinding.and(isStarted))
            item("Add Filter") {
                action {
                    if (selectionModel.isEmpty) return@action

                    val changeDialog = ValueChangeDialog("Add Patient Ids (comma separated)", { ids ->
                        selectionModel.selectedItems.forEach { s ->
                            s.patientFilterFromCommaSeparatedString(ids)
                            Publisher.updateSubscriber(s)
                        }
                    }, "")
                    changeDialog.addValidation { input ->
                        return@addValidation when (input) {
                            null -> ValidationMessage("No Input", ValidationSeverity.Error)
                            "" -> {
                                selectionModel.selectedItems.forEach {
                                    it.clearAcceptedPatients()
                                }
                                null
                            }
                            else -> {
                                try {
                                    input.split(",").forEach {
                                        Integer.parseInt(it)
                                    }
                                } catch (any: Exception) {
                                    return@addValidation ValidationMessage(
                                        "Ids are not numeric",
                                        ValidationSeverity.Error
                                    )
                                }
                                null
                            }
                        }
                    }
                    openInternalWindow(changeDialog)
                }
            }
            item("Change Name") {
                action {
                    if (selectionModel.isEmpty) return@action

                    val selectedItems = selectionModel.selectedItems
                    if (selectedItems.size > 1) {
                        alert(Alert.AlertType.INFORMATION, "Names can only be changed for one Client")
                        return@action
                    }
                    if (selectedItem !is RemoteSubscriber) {
                        alert(Alert.AlertType.INFORMATION, "Names can only be changed for remote Clients")
                        return@action
                    }

                    val changeDialog = ValueChangeDialog("Change Client Name", {
                        if (selectedItem?.name != it) {
                            selectedItem?.name = it
                            selectedItem?.addMetaInfo("name", it)
                            Publisher.updateSubscriber(selectedItem!!)
                        }
                    }, selectedItem?.name)
                    changeDialog.addValidation { input ->
                        return@addValidation when (input) {
                            selectedItem?.name -> null
                            null -> ValidationMessage("No Input", ValidationSeverity.Error)
                            "" -> ValidationMessage("Empty Input", ValidationSeverity.Error)
                            else -> {
                                Publisher.getSubscribers().forEach {
                                    if (it.value.name == input) {
                                        return@addValidation ValidationMessage(
                                            "Another Client has already that Name",
                                            ValidationSeverity.Error
                                        )
                                    }
                                }
                                null
                            }
                        }
                    }
                    openInternalWindow(changeDialog)
                }
            }
        }
    }

    // reacts to changes in the networking state
    private val networkingChangeListener = object : NetworkingChangeListener {
        override fun onStateChange(old: NetworkingState, new: NetworkingState) {
            runLater {
                // update state
                networkingState.set(new)

                // stop monitoring if networking is not active
                if (new == NetworkingState.INACTIVE && isStarted.get()) stop()

                updateIPs()
            }
        }
    }

    // reacts to changes in subscribers (new, leaving, updates)
    private val subscriberChangeListener = object : SubscriberChangeListener {
        override fun onChange(subscriber: Subscriber) {
            subscriberTable.refresh()

            // the name will be set in ConnectionHandler::processMessage and this triggers onChange
            if (subscriber.name == "lens")
            {
                val screenSize: java.awt.Dimension = Toolkit.getDefaultToolkit().screenSize
                val handshakeMessage = JSONObject()
                handshakeMessage.put("screenWidth", screenSize.width)
                handshakeMessage.put("screenHeight", screenSize.height)
                Publisher.sendMessage(handshakeMessage, subscriber)
            }

            // we send back the updated meta info entries for 'confirmation'
            val metaInfoEntries = subscriber.getMetaInfoEntries()
            if (metaInfoEntries.isEmpty()) return
            Publisher.sendMessage(Converter.createUpdateSubscriberMessage(metaInfoEntries), subscriber)
        }

        override fun onSubscribe(subscriber: Subscriber) {
            if (!subscriberTable.items.contains(subscriber)) {
                runLater { subscriberTable.items.add(subscriber) }
            }
        }

        override fun onUnsubscribe(subscriber: Subscriber) {
            if (subscriberTable.items.contains(subscriber)) {
                runLater { subscriberTable.items.remove(subscriber) }
            }
        }

        override fun onMessage(subscriber: Subscriber?, jsonObject: JSONObject) {
            runLater {
                messageList.items.add("Network: " + jsonObject.toString())
            }

            // wenn die Nachricht vom Frontend kommt
            if (subscriber?.name == "frontend") {
                Publisher.publish(jsonObject)
            }

            // a message of type "backend" arrives, which has set the property "target"
            // for instance: target is "lens" -> msg will be sent to HoloLens, when it's connected and named "lens"
            if (jsonObject.get("type") == "backend") {

                // when a backend message has the "target" property, it is used to relay to a specific target
                val target = jsonObject.opt("target")

                if (target is String && target == "lens")
                {
                    println("target is $target")
                    val lens = subscriberTable.items.find{ it.name == target } ?: return

                    //TODO: only transfer, if the hololensCueType is not none
                    //only transfer the Module Rectangle Information
                    val holoLensMsg = JSONObject()

                    val contentObject = jsonObject.get("content")
                    if (contentObject is JSONObject)
                    {
                        val rect = contentObject.get("rect")
                        val interruptionLength = contentObject.get("interruptionLength")
                        val hololensCueType = contentObject.get("hololensCueType")

                        holoLensMsg.put("prevModuleRect", rect)
                        holoLensMsg.put("interruptionLength", interruptionLength)
                        holoLensMsg.put("hololensCueType", hololensCueType)

                        Publisher.sendMessage(holoLensMsg, lens)
                    }
                    return
                }

                if (jsonObject.get("content") is JSONObject)
                {
                    val content = jsonObject.getJSONObject("content") as JSONObject

                    if (target is String && target == "web client")
                    {
                        // why ist errorCountInterruption of type Any! if it can be null?
                        val errorCountInterruption = content.get("errorCountInterruption")

                        val jsonObj = JSONObject()
                        jsonObj.put("type", "web client")
                        jsonObj.put("errorCountInterruption", errorCountInterruption)

                        // only relay the errorCountInterruption to the webClient, since the timestamp isnt needed
                        val webClient = subscriberTable.items.find{ it.name == "web client" } ?: return
                        Publisher.sendMessage(jsonObj, webClient)
                    }

                    val startT = content.opt("startTime")
                    val endT = content.opt("endTime")

                    if (startT != null && endT != null)
                    {
                        System.out.println("Interruption Times")
                        startTime = content.optString("startTime", " ")
                        endTime = content.optString("endTime", " ")
                        Publisher.publish(content)
                    }


                }
                else if (jsonObject.get("content") is JSONArray)
                {
                    val csvData = jsonObject.getJSONArray("content") as JSONArray
                    for (i in 0 until csvData.length()) {
                        convertToCSVFile(csvData.getJSONObject(i))
                        System.out.println("Data: " + csvData.getJSONObject(i))
                    }
                    System.out.println("Close log files")
                    //GlobalLogger.closeAllLogFiles()
                }

            }
        }
    }

    private val messageList = listview<String> {
        items.add("No Messages")

        contextmenu {
            enableWhen(isNetworkingActiveBinding.and(isStarted))
            item("Send Message") {
                action {
                    if (selectionModel.isEmpty) return@action

                    if (subscriberTable.selectionModel.isEmpty) {
                        alert(Alert.AlertType.INFORMATION, "Please select one or more recipients")
                        return@action
                    }
                    subscriberTable.selectionModel.selectedItems.forEach {
                        Publisher.sendMessage(JSONObject(selectedItem), it)
                    }
                }
            }
        }
    }
    private val monitorListener = object : MonitorListener {

        override fun onMessage(message: String) {
            runLater {
                messageList.items.add("Network: " + message)
            }
            val jsonMessage = JSONObject(message)
            if (jsonMessage.get("type") == "frontend") {
                if (!jsonMessage.has("dataType")) {
                    mediaPlayer.play()
                    mediaPlayer.seek(Duration(0.0))
                    var interruptionLength = jsonMessage.get("content")
                    interruptionLength = interruptionLength.toString().toLong()
                    var delayLength = (interruptionLength * 1000) + 600
                    val timer = Timer()
                    var x = 960
                    var y = 600
                    val r = Robot()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            var a = MouseInfo.getPointerInfo()
                            var point = a.getLocation()
                            //x =  point.getX().toInt()
                            //y =  point.getY().toInt()
                            timer.scheduleAtFixedRate(
                                object : TimerTask() {
                                    override fun run(){
                                        //r.mouseMove(x, y)
                                        //TODO: comment back in (or disable when in debug?)
                                    }
                                },
                                0, 1
                            )
                        }
                    }, 300)
                    //Set the schedule function

                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            mediaPlayer.play()
                            mediaPlayer.seek(Duration(0.0))
                            timer.cancel()
                        }
                    }, delayLength)
                } else {
                    println(jsonMessage.toString())
                }
            }
        }
    }


    fun convertToCSVFile(csvData: JSONObject) {
        val participantNumber = csvData.optString("participantNumber", " ")
        val block = csvData.optString("block", " ")
        val device = csvData.optString("device", " ")
        val interruptionTrial = csvData.optString("interruptionTrial", " ")
        val trials = csvData.optString("trial", " ")
        val clickInModule = csvData.optString("time", " ")
        val clickInModuleInt = csvData.optString("timeInt", " ")
        val resumptionError = csvData.optString("timeResumptionError", " ")
        val patientID = csvData.optString("patientID", " ")
        val module = csvData.optString("module", " ")
        val wrongModule = csvData.optString("errorsModule", " ")
        val wrongInput = csvData.optString("errorsInput", " ")
        val emptyModule = csvData.optString("errorsEmptyModule", " ")
        val interruptionLength = csvData.optString("interruptionLength", " ")
        val clickOK = csvData.optString("clickOnOK", " ")
        val clickOKInt = csvData.optString("clickOnOKInt", " ")
        val startTimeInterruption = csvData.optString("startTimeIT", " ")
        val startTimeInterruptionInt = csvData.optString("startTimeInteger", " ")
        val endTimeInterruption = csvData.optString("endTimeIT", " ")
        val endTimeInterruptionInt = csvData.optString("endTimeInteger", " ")
        val errorCountInterruption = csvData.optString("errorCountInterruption", " ")
        val hololensCueType = csvData.optString("hololensCueType", " ")

        System.out.println("StartTime:" + startTimeInterruption)
        System.out.println("EndTime:" + endTimeInterruption)

        // Note: The columns needs to be added beforehand,
        // see: ExperimentRunner::createLogger
        val logEntry = GlobalLogger.exp().newLogEntry()
        logEntry.setValue("Participant Number", participantNumber)
        logEntry.setValue("Block", block)
        logEntry.setValue("Device", device)
        logEntry.setValue("Interruption Trial", interruptionTrial)
        logEntry.setValue("Trial", trials)
        logEntry.setValue("First Click In Module", clickInModule)
        logEntry.setValue("INTEGER: First Click In Module", clickInModuleInt)
        logEntry.setValue("Wrong Click In Module After Interruption", resumptionError)
        logEntry.setValue("Patient ID", patientID)
        logEntry.setValue("Module", module)
        logEntry.setValue("Error Wrong Module", wrongModule)
        logEntry.setValue("Error Input", wrongInput)
        logEntry.setValue("Error Empty Module", emptyModule)
        logEntry.setValue("Interruption Length", interruptionLength)
        logEntry.setValue("Click on OK", clickOK)
        logEntry.setValue("INTEGER: Click on OK", clickOKInt)
        logEntry.setValue("Start Time Interruption", startTimeInterruption)
        logEntry.setValue("INTEGER: Start Time Interruption", startTimeInterruptionInt)
        logEntry.setValue("End Time Interruption", endTimeInterruption)
        logEntry.setValue("INTEGER: End Time Interruption", endTimeInterruptionInt)
        logEntry.setValue("Error Count Interruption", errorCountInterruption)
        logEntry.setValue("Hololens Cue Type", hololensCueType)

        GlobalLogger.exp().log(logEntry)

    }

    override val root = vbox(20) {
        this.paddingAll = 10
        this.isFillWidth = true
        this.minWidth = 800.0

        hbox(50) {
            isFillWidth = true
            hbox(5) {
                alignment = Pos.CENTER
                label("Networking Monitor") {
                    font = Font.font(18.0)
                    textFill = Color.DARKOLIVEGREEN
                }
            }
            hbox(0) {
                alignment = Pos.CENTER
                label("Networking State: ") {
                    font = Font.font(14.0)
                }
                label(networkingState) {
                    font = Font.font(14.0)
                    textFill = Color.MAROON
                }
                label(localIps) {
                    visibleWhen { localIps.isNotEmpty }
                    paddingLeft = 20
                    font = Font.font(14.0)
                    textFill = Color.DARKKHAKI
                    tooltip("Click to copy IP(s)")
                    tooltip?.font = Font.font(15.0)
                    cursor = Cursor.HAND
                    setOnMouseClicked {
                        val clipboard = Clipboard.getSystemClipboard()
                        val content = ClipboardContent()

                        var ips = utils.getLocalIp4Addresses().toString()
                        System.out.println(ips)
                        ips = ips.replace("[\\[\\]]".toRegex(), "")

                        if (ips.isEmpty()) return@setOnMouseClicked

                        content.putString(ips)
                        clipboard.setContent(content)
                    }
                }
            }
        }

        hbox(50) {
            isFillWidth = true
            alignment = Pos.CENTER
            val startNetworkingButton = button("Start Networking") {
                enableWhen(Bindings.equal(networkingState, NetworkingState.INACTIVE))
                action { runAsync { Publisher.startNetworking() } }
            }
            button("Stop Networking") {
                enableWhen(isNetworkingActiveBinding)
                action {
                    parent.requestFocus()
                    runAsync {
                        Publisher.stopNetworking()
                    } ui {
                        startNetworkingButton.requestFocus()
                    }
                }
            }

            val startButton = button("Start Monitor") {
                enableWhen(isNetworkingActiveBinding.and(!isStarted))
                action { start() }
            }
            runLater { startButton.requestFocus() }
            button("Stop Monitor") {
                enableWhen(isNetworkingActiveBinding.and(isStarted))
                action {
                    stop()
                    startButton.requestFocus()
                }
            }
            val startWebsocket = button("Start Websocket") {
                enableWhen(isNetworkingActiveBinding.and(isStarted))
                action {
                    var file = File("src/test/websocket_client/patientDocumentation.html")
                    var pathToWebsocket = file.absolutePath
                    Desktop.getDesktop().browse(File(pathToWebsocket).toURI())

                }
            }

            button("Send to Lens")
            {
                enableWhen(isNetworkingActiveBinding.and(isStarted))
                action {
                    val lens = subscriberTable.items.find{ it.name == "lens" } ?: return@action
                    Publisher.sendMessage(JSONObject("{\"type\": \"message\", \"content\": {\"string1\": \"content1\"}}\""), lens)
                }
            }

            runLater { startWebsocket.requestFocus() }
        }

        hbox(5) {
            isFillWidth = true
            vgrow = Priority.ALWAYS
            this.add(subscriberTable)
            messageList.hgrow = Priority.ALWAYS
            this.add(messageList)
        }

        hbox(5) {
            isFillWidth = true
            val msg = textfield()
            msg.hgrow = Priority.ALWAYS
            button("Send Message") {
                enableWhen(isNetworkingActiveBinding)
                action {
                    Publisher.publish(JSONObject(utils.createInfoJson(msg.text)))
                    msg.clear()
                }
            }
        }
    }

    init {
        Publisher.addNetworkingChangeListener(this.networkingChangeListener)
        this.updateIPs()
    }

    private fun updateIPs() {
        if (this.networkingState.get() != NetworkingState.ACTIVE) {
            this.localIps.value = ""
            return
        }

        this.localIps.value = "IP: N/A"
        val ipsForLocalMachine = utils.getLocalIp4Addresses()
        if (ipsForLocalMachine.isNotEmpty()) {
            var count = 0
            val tmp = StringBuilder("IP").append(if (ipsForLocalMachine.size > 1) "(s)" else "").append(": ")
            ipsForLocalMachine.forEach {
                if (count != 0) tmp.append(", ")
                tmp.append(it)
                count++
            }
            tmp.append(" | PORT: ${Server.DEFAULT_PORT}")
            this.localIps.value = tmp.toString()
        }
    }

    private fun start() {
        System.out.println(path +  mediaPlayer.error+ mediaPlayer.status)
        mediaPlayer.play()
        mediaPlayer.seek(Duration(0.0))
        this.monitor.addMonitorListener(this.monitorListener)
        Publisher.subscribe(this.monitor)
        this.subscriberTable.items.clear()
        this.messageList.items.clear()
        this.subscriberTable.items.addAll(
            Publisher.getSubscribers().values.sortedWith(
                Comparator { sub1, sub2 -> sub1.name.compareTo(sub2.name) })
        )
        Publisher.addSubscribeListener(this.subscriberChangeListener)
        this.isStarted.set(true)
    }

    private fun stop() {
        this.monitor.removeMonitorListener()
        Publisher.unsubscribe(this.monitor)
        Publisher.removeSubscribeListener(this.subscriberChangeListener)
        this.subscriberTable.items.clear()
        this.subscriberTable.items.add(this.monitorNotRunningDummySubscriber)
        this.messageList.items.clear()
        this.messageList.items.add("No Messages")
        this.isStarted.set(false)
    }

    override fun onUndock() {
        // we react differently depending on where we are running
        if (!this.isItsOwnApp) return

        this.stop()
        super.onUndock()
        runAsync {
            try {
                Publisher.stopPublisher()
                app.stop()
            } catch (dontCarAnyMore: Exception) {
            }
        }
    }
}

class NetworkingMonitorApp : App(NetworkingServiceMonitor::class)

fun main() {
    Application.launch(NetworkingMonitorApp::class.java, "isItsOwnApp")
}