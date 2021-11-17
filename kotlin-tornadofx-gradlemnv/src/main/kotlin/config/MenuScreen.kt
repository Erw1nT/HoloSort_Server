package config

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.Stage
import logging.GlobalLogger
import logging.LogFormat
import logging.LogLevels
import networking.NetworkingChangeListener
import networking.NetworkingState
import networking.Server
import publisher.Publisher
import tornadofx.*
import utils.UI
import views.NetworkingServiceMonitor
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


class MenuScreen : View() {
    private val versionId : String? = this.javaClass.`package`.implementationVersion
    private val buildDate : String? = this.javaClass.`package`.implementationTitle

    // stores the networking state
    private var networkingState = SimpleObjectProperty(Publisher.getNetworkingState())
    // shows the description of the networking state
    private var networkingStateDescription = SimpleObjectProperty("")
    // keeps track of open windows
    private var networkingMonitor : Stage? = null
    private var blockEditor : Stage? = null
    private var experimentRunner : Stage? = null

    var startServerButton: javafx.scene.control.Button by singleAssign()

    // listens to changes in the networking state
    private val networkingChangeListener = object : NetworkingChangeListener {
        override fun onStateChange(old: NetworkingState, new: NetworkingState) {
            // never block
            runLater {
                networkingState.set(new)
                updateNetworkingDescription()
            }
        }
    }

    private val isNetworkingStartingOrStopping = Bindings.equal(this.networkingState,NetworkingState.STARTING).or(
        Bindings.equal(this.networkingState,NetworkingState.STOPPING))

    private val isNetworkingRunning = Bindings.equal(this.networkingState,NetworkingState.ACTIVE)

    init {
        // init app logging
        GlobalLogger.app(logFormat = LogFormat.CONSOLE)
        GlobalLogger.app().logLevel = LogLevels.DEBUG

        Publisher.addNetworkingChangeListener(this.networkingChangeListener)
        this.updateNetworkingDescription()
    }

    override val root = vbox(20) {
        paddingAll = 20.0

        val iconFont = UI.loadFont("Symbola", 100.0) ?: Font.font(12.0)

        hbox(10) {

            vbox(10) {
                hgrow = Priority.ALWAYS
                alignment = Pos.CENTER_LEFT
                label("HDMLag2 2021") { font = Font.font(30.0) }
                label("Version $versionId") { font = Font.font(12.0) }
                label("Build Date $buildDate") { font = Font.font(12.0) }
            }
            vbox(10) {
                alignment = Pos.CENTER_RIGHT
                startServerButton = button("Start Server") {
                    //enableWhen(!isNetworkingStartingOrStopping)
                    //visibleWhen { !isNetworkingStartingOrStopping }
                    tooltip("Open Networking Monitor")
                    tooltip?.font = Font.font(15.0)
                    font = Font.font(12.0)
                    action {
                        runAsync {
                            if (networkingState.get() == NetworkingState.INACTIVE) {
                                Publisher.startNetworking()
                            }
                            else{
                                Publisher.stopNetworking()
                            }
                        }
                        if(networkingState.get() == NetworkingState.INACTIVE) startServerButton.text = "Stop Server"
                        else startServerButton.text = "Start Server"
                    }
                }
                label(networkingStateDescription) {
                    font = Font.font(14.0)
                    textFill = Color.TEAL
                }
            }
        }

        hbox(10) {
            vbox {
                alignment = Pos.CENTER

                button("⚙") {
                    font = iconFont
                    action {
                        blockEditor = UI.openWindow(blockEditor, if (blockEditor == null) TrialDesigner() else null)
                    }
                }
                label("Trial Designer") { font = Font.font(20.0) }
            }
            vbox {
                alignment = Pos.CENTER

                button("▷") {
                    font = iconFont
                    action {
                        /*
                        experimentAssembler = UI.openWindow(
                            experimentAssembler, if (experimentAssembler == null) ExperimentAssembler() else null)
                        */
                        experimentRunner = UI.openWindow(
                            experimentRunner, if (experimentRunner == null) ExperimentRunner() else null)
                    }

                }
                label("Experiment Runner") { font = Font.font(20.0) }
            }
            vbox {
                alignment = Pos.CENTER

                button("🖧") {
                    enableWhen(isNetworkingRunning)
                    onHover {
                        tooltip(if (networkingState.get() == NetworkingState.INACTIVE) "Start Networking" else "Stop Networking")
                        tooltip?.font = Font(20.0)
                    }
                    font = iconFont
                    action {
                        networkingMonitor = UI.openWindow(
                            networkingMonitor, if (networkingMonitor == null) NetworkingServiceMonitor() else null)
                    }
                }

                label("Networking") {
                    font = Font.font(20.0)
                }
            }
        }

    }

    private fun updateNetworkingDescription() {
        val state = this.networkingState.get()
        when (state) {
            NetworkingState.INACTIVE -> this.networkingStateDescription.value =
                ""
            NetworkingState.ACTIVE -> this.networkingStateDescription.value =
                "Server is running on: \n${utils.getLocalIp4Addresses()}\nPort: ${Server.DEFAULT_PORT}"
            NetworkingState.STARTING -> this.networkingStateDescription.value =
                "Starting Server ..."
            NetworkingState.STOPPING -> this.networkingStateDescription.value =
                "Stopping Server ..."
            else -> this.networkingStateDescription.value = ""
        }
    }

    override fun onUndock() {
        this.experimentRunner?.close()
        this.blockEditor?.close()
        this.networkingMonitor?.close()
        Publisher.removeNetworkingChangeListener(this.networkingChangeListener)
        runLater {
            try {
                Publisher.stopPublisher()
            } catch (ignored : Exception) { }
            GlobalLogger.closeAllLogFiles()
        }
    }
}

class HMDLagMenu: App(MenuScreen::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        UI.centerWindow(stage)
    }
}

fun main() {
    launch<HMDLagMenu>()
}
