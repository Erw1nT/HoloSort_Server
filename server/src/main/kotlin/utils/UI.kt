package utils

import javafx.scene.text.Font
import javafx.stage.*
import tornadofx.*
import java.io.InputStream

object UI {

    /**
     * Tries to center window filling entire screen if second parameter is true
     * Note: if the screen is too small such that x or y get negative we also fill the screen
     *       effectively resizing to fit
     */
    fun centerWindow(stage: Stage, fillScreen: Boolean = false) {
        val primaryScreenBounds = Screen.getPrimary().visualBounds
        val screenWidth = primaryScreenBounds.width
        val screenHeight = primaryScreenBounds.height
        val stageWidth = stage.width
        val stageHeight = stage.height
        val stageX = screenWidth / 2 - stageWidth / 2
        val stageY = screenHeight / 2 - stageHeight / 2
        if (stageX < 0 || stageY < 0 || fillScreen) {
            stage.x = 0.0
            stage.y = 0.0
            stage.width = screenWidth
            stage.height = screenHeight
        } else {
            stage.x = stageX
            stage.y = stageY
        }
    }

    /**
     * Convenience method for showing/opening a window given the view.
     * Should make sure that:
     * 1. the window opening only happens once
     * 2. the window will centered on first opening as well as fits the primary display
     * 3. the window will not be covered by another
     */
    fun openWindow(stage: Stage?, view: View? = null, escapeClosesWindow: Boolean = false): Stage? {
        // we have to have either one already
        if (stage == null && view == null) return null

        if (stage == null) {
            val ret = view?.openWindow(escapeClosesWindow = escapeClosesWindow, owner = null)
            if (ret != null) UI.centerWindow(ret)
            return ret
        }

        if (!stage.isShowing) stage.show()
        else stage.requestFocus()

        return stage
    }

    /**
     * Loads given font provided the file is located in the fonts directory as .ttf
     * Returns Font if font registration succeeds null otherwise
     */
    fun loadFont(font: String, size: Double = 0.0): Font? {
        var inputStream : InputStream? = null
        return try {
            inputStream = UI::class.java.getResourceAsStream("/fonts/$font.ttf")
            Font.loadFont(inputStream, size)
        } catch (e: Exception) {
            null
        } finally {
            try {
                inputStream?.close()
            } catch (ignored: Exception) {}
        }
    }

    fun showModalNotification(message: String, owner: Window? = null, timeout : Long = 0) : Stage? {
        val popup =  Notification(message).openModal(StageStyle.UNDECORATED,
            if (owner == null) Modality.APPLICATION_MODAL else Modality.WINDOW_MODAL, false, owner)
        if (popup != null) UI.centerWindow(popup)

        if (timeout > 0) { runLater { popup?.close() } }

        return popup
    }
}

class Notification(message : String): Fragment() {
    override val root = vbox {
        paddingAll = 40.0
        label(message) {
            font = Font.font(25.0)
        }
    }
}