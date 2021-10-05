package utils

import javafx.beans.Observable
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import logging.GlobalLogger

import tornadofx.onChange
import java.util.*
import tornadofx.*

class HistoryManager {
    private val subscribers = FXCollections.observableArrayList<IHistoryChanged>()

    private val listOfObservables = FXCollections.observableArrayList<Observable>()
    private val listOfObservableLists = FXCollections.observableArrayList<ObservableList<Any>>()

    private val historyList = LinkedList<Pair<String, Any>>()
    private val historyPointerProperty = SimpleIntegerProperty(-1)
    private var historyPointer by historyPointerProperty

    val isUndoableProperty = SimpleBooleanProperty(false)
    var isUndoable by isUndoableProperty

    val isRedoableProperty = SimpleBooleanProperty(false)
    var isRedoable by isRedoableProperty

    init {
        historyPointerProperty.onChange {
            isUndoable = historyPointer > 0
            isRedoable = historyPointer < historyList.size - 1
        }
    }

    fun addSubscriber(obj: IHistoryChanged) {
        subscribers.add(obj)
    }

    fun removeSubscriber(obj: IHistoryChanged) {
        subscribers.remove(obj)
    }

    fun onPropertyChanged(obj: Any) {
        subscribers.forEach { it.onPropertyChanged(obj) }
        updateListeners(obj) // TODO may need to remove references to deleted observables
        printHistory()
    }

    fun isObservable(obj: Any) : Boolean {
        if (obj is SimpleBooleanProperty) return true
        if (obj is SimpleStringProperty) return true
        if (obj is SimpleIntegerProperty) return true
        if (obj is SimpleObjectProperty<*>) return true
        if (obj is SimpleDoubleProperty) return true
        if (obj is SimpleLongProperty) return true
        return false
    }

    fun <T> isObservableClass(clazz: Class<T>) : Boolean {
        if (clazz == SimpleBooleanProperty::class.java) return true
        if (clazz == SimpleStringProperty::class.java) return true
        if (clazz == SimpleIntegerProperty::class.java) return true
        if (clazz == SimpleObjectProperty<T>()::class.java) return true
        if (clazz == SimpleDoubleProperty::class.java) return true
        if (clazz == SimpleLongProperty::class.java) return true
        return false
    }

    fun isObservableList(obj: Any) : Boolean {
        if (obj is ObservableList<*>) return true
        return false
    }

    fun containsObservables(obj: Any) : Boolean {
        val fields = obj.javaClass.declaredFields
        fields.forEach {
            if (isObservableClass(it.type)) return true
        }
        return false
    }

    fun getObservables(obj: Any) : List<Observable> {
        val observables = mutableListOf<Observable>()

        val fields = obj.javaClass.declaredFields
        fields.forEach {
            if (isObservableClass(it.type) && !it.name.contains("\$delegate")) {
                it.isAccessible = true
                observables.add(it.get(obj) as Observable)
            }
        }
        return observables
    }

    fun getObservableLists(obj: Any) : List<ObservableList<Any>> {
        val observableLists = mutableListOf<ObservableList<Any>>()

        val fields = obj.javaClass.declaredFields
        fields.forEach {
            it.isAccessible = true
            val f = it.get(obj)
            if (f != null && isObservableList(f)) {
                @Suppress("UNCHECKED_CAST")
                observableLists.add(f as ObservableList<Any>)
            }
        }

        return observableLists
    }

    fun updateListeners(obj: Any) {
        val observables = getObservables(obj)
        val observableLists = getObservableLists(obj)

        observables.forEach {
            if (!listOfObservables.contains(it)) {
                it.addListener { ob ->
                    onPropertyChanged(ob)
                }
                listOfObservables.add(it)
            }
        }

        observableLists.forEach {
            if (!listOfObservableLists.contains(it)) {
                it.onChange { c ->
                    onPropertyChanged(c)
                }
                listOfObservableLists.add(it)
            }

            it.forEach { obj ->
                updateListeners(obj)
            }
        }

    }


    fun attachListenerToObservables(obj: Any) {
        val observables = getObservables(obj)
        val observableLists = getObservableLists(obj)

        listOfObservables.addAll(observables)
        listOfObservableLists.addAll(observableLists)

        observables.forEach {
            it.addListener { ob ->
                onPropertyChanged(ob)
            }
        }

        observableLists.forEach {
            it.onChange { c ->
                onPropertyChanged(c)
            }

            it.forEach { obj ->
                attachListenerToObservables(obj)
            }
        }
    }

    fun addToHistory(obj: Pair<String, Any>) {
        historyList.add(++historyPointer, obj)
        val followingItems = historyList.filterIndexed { i, _ -> i > historyPointer }
        if (followingItems.isNotEmpty()) historyList.removeAll(followingItems)
    }

    fun undo() : Pair<String, Any>? {
        if (historyList.isNotEmpty() && historyPointer > 0) {
            return historyList[--historyPointer]
        }
        return null
    }

    fun redo() : Pair<String, Any>? {
        if (historyList.isNotEmpty() && historyPointer < historyList.size - 1) {
            return historyList[++historyPointer]
        }
        return null
    }

    fun printHistory() {
        historyList.forEachIndexed { i, it ->
            GlobalLogger.app().logError(
                "${if (i == historyPointer) "--> " else "    "}${it.first} :: ${it.second}")
        }
        GlobalLogger.app().logError("")
    }
}

interface IHistoryChanged {
    fun onPropertyChanged(change: Any)
}

fun main() {

   /* val e = ExperimentBlock()

    val h = HistoryManager()

    h.attachListenerToObservables(e)

    e.customArgs = "Some custom args"

    e.patients.removeAt(3)

    e.patients[0].description = "TAU"

    e.patients[1].vitalSigns.removeAt(2)

    e.patients[2].vitalSigns[1].description = "NEW DESC"

    e.patients[4].vitalSigns[0].data.add(VitalSignDatum(20))*/

}