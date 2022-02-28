package config


import javafx.beans.property.SimpleStringProperty

import models.Trial
import tornadofx.*
import utils.Runtime

abstract class AbstractTrialDesigner<T : Trial>(
    protected var trial: T, private var useNetworking: Boolean? = false) : View() {

    protected val codeSource = Runtime.getSourceCodePath(AbstractTrialDesigner::class.java)

    protected val filePathProperty = SimpleStringProperty(null)
    protected var filePath: String? by filePathProperty

}