package utils

import javafx.scene.control.*
import javafx.scene.control.cell.ComboBoxTableCell
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.util.Callback
import models.*
import tornadofx.*

import logging.GlobalLogger
import java.util.*
/*
class ColourCell : TableCell<VitalSign, Color?>() {
    private val cp = colorpicker {
        setOnAction {
            GlobalLogger.app().logError("Value: $value")
            GlobalLogger.app().logError("Item: ${this@ColourCell.rowItem}")
            this@ColourCell.rowItem.colour = value
        }
    }

    init {
        graphic = cp
        contentDisplay = ContentDisplay.GRAPHIC_ONLY
        isEditable = true
    }

    override fun updateItem(item: Color?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item != null)
            cp.value = this@ColourCell.rowItem.colour
    }
}

val colourCellFactory = Callback<TableColumn<VitalSign, Color?>, TableCell<VitalSign, Color?>> {
    return@Callback ColourCell()
}

class EventTypeCell : ComboBoxTableCell<Event, EventType>() {
    override fun commitEdit(newValue: EventType?) {
        val isSame = rowItem.eventType == newValue
        if (!isSame && (rowItem.eventType == EventType.SOUNDS || rowItem.eventType == EventType.TIMER)) {
            rowItem.customArgsProperty.set("")
        }
        super.commitEdit(newValue)
    }

    init {
        this.items.setAll(EventType.values().asList())
    }

    override fun updateItem(item: EventType?, empty: Boolean) {
        super.updateItem(item, empty)

        textFill = Color.BLACK
        isDisabled = false
        if (item != null) {
            text = item.toString()

            if (item == EventType.EMPTY) {
                textFill = Color.RED
            } else if (item == EventType.START || item == EventType.END) {
                isDisabled = true
                textFill = Color.LIGHTGRAY
            } else if (item == EventType.SOUNDS) {
                rowItem.configAction = { e, ex ->
                    val p = findParent<View>()
                    SoundSequenceConfModal(e, ex, tableView::refresh).
                        openWindow(modality = Modality.WINDOW_MODAL, owner = p?.currentWindow)
                }
            } else if (item == EventType.TIMER) {
                rowItem.configAction = { e, ex ->
                    val p = findParent<View>()
                    ResponseTimerConfModal(e, ex, tableView::refresh).
                        openWindow(modality = Modality.WINDOW_MODAL, owner = p?.currentWindow)
                }
            } else if (item == EventType.FREEZE) {
                rowItem.configAction = { e, ex ->
                    val p = findParent<View>()
                    SagatConfigModal(e, ex, tableView::refresh).
                        openWindow(modality = Modality.WINDOW_MODAL, owner = p?.currentWindow)
                }
            } else if (item == EventType.POPUP) {
                rowItem.configAction = { e, ex ->
                    val p = findParent<View>()
                    PopupConfigModal(e, ex, tableView::refresh).
                        openWindow(modality = Modality.WINDOW_MODAL, owner = p?.currentWindow)
                }
            }
        } else {
            text = null
        }
    }
}

class EventConfigActionCell(private val experimentBlock: ExperimentBlock) :
    TableCell<Event, ((event : Event, experimentBlock: ExperimentBlock) -> Unit)?>() {
    override fun updateItem(item: ((event : Event, experimentBlock: ExperimentBlock) -> Unit)?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item != null &&
            (rowItem.eventType == EventType.SOUNDS || rowItem.eventType == EventType.TIMER ||
                    rowItem.eventType == EventType.FREEZE || rowItem.eventType == EventType.POPUP)) {

            this.graphic = button {
                text = "⚙"
                font = Font.font(10.0)
                tooltip = Tooltip("Configure Event Type")
                action {
                    item(rowItem, this@EventConfigActionCell.experimentBlock)
                }
            }
        } else {
            this.graphic = null
        }
    }
}

//    override fun startEdit() {
//        super.startEdit()
//        if (this.graphic.javaClass == ComboBox<EventType>().javaClass) {
//            (this.graphic as ComboBox<EventType>).selectionModel = eventTypeCellSelectionModel
//        }
//    }

val eventTypeCellFactory = Callback<TableColumn<Event, EventType>, TableCell<Event, EventType>> {
    return@Callback EventTypeCell()
}
*/
class GenericCrudUi : HBox() {
    val addButton = Button("+")
    val removeButton = Button("-")
    val upButton = Button("▲")
    val downButton = Button("▼")

    init {
        this.add(addButton)
        this.add(removeButton)
        this.add(upButton)
        this.add(downButton)
    }
}

object CustomUIComponents {
    fun <S> getCrudUiForTable(t: TableView<S>, factory: () -> S) : HBox {
        val addButton = Button("+")
        addButton.setOnAction {
            t.items?.add(factory())
        }

        val removeButton = Button("-")
        removeButton.setOnAction {
            if (t.items != null && t.items.size > 0 && t.selectedItem != null) {
                t.items.remove(t.selectedItem)

                /*if (!(t.selectedItem is Event && ((t.selectedItem as Event).eventType == EventType.START
                            || (t.selectedItem as Event).eventType == EventType.END)
                            || (t.selectedItem is VitalSignDatum && t.items.size <= 1)
                            )
                ) {

                }*/
            }
        }

        val upButton = Button("▲")
        upButton.setOnAction {
            val i = t.selectedCell?.row
            if (i != null && i > 0) Collections.swap(t.items, i, i - 1)
        }

        val downButton = Button("▼")
        downButton.setOnAction {
            val i = t.selectedCell?.row
            if (i != null && i < t.items.size - 1) Collections.swap(t.items, i, i + 1)
        }

        return HBox(5.0, addButton, removeButton, upButton, downButton)
    }

    fun <S> getCrudUiForList(t: ListView<S>, factory: () -> S) : HBox {
        val addButton = Button("+")
        addButton.setOnAction {
            t.items?.add(factory())
        }

        val removeButton = Button("-")
        removeButton.setOnAction {
            if (t.items != null &&
                t.items.size > 0 &&
                t.selectedItem != null &&
                t.selectionModel != null &&
                t.selectionModel.selectedIndex >= 0) {

                t.items.removeAt(t.selectionModel.selectedIndex)
            }
        }


        return HBox(5.0, addButton, removeButton)
    }

    class FileOpenDialogButtonCell<T>(val action : (T, String) -> Unit) : TableCell<T, String>() {
        override fun updateItem(item: String?, empty: Boolean) {
            super.updateItem(item, empty)
            val nrOfItems = tableView.items.size
            if (index < nrOfItems) {
                graphic = button {
                    text = "\uD83D\uDCC2"
                    textFill = Color.BLACK
                    font = Font.font("Symbola", 12.0) ?: Font.font(12.0)
                    tooltip = Tooltip("Choose File")
                    action {
                        val file = chooseFile("Load sound file",
                            filters = arrayOf(
                                FileChooser.ExtensionFilter("wav (*.wav)", "*.wav"),
                                FileChooser.ExtensionFilter("mp3 (*.mp3)", "*.mp3")
                            ), mode = FileChooserMode.Single)
                        if (index == -1 || index >= nrOfItems) return@action
                        action(rowItem, if (file.isNotEmpty()) {
                            file.first().absolutePath
                        } else "")
                    }
                }
            } else {
                graphic = null
            }
        }
    }
}
