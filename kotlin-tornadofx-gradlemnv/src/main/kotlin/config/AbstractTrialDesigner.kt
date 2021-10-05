package config


import javafx.beans.property.SimpleStringProperty

import models.Trial
import models.TrialsConfiguration
import tornadofx.*
import utils.Runtime

abstract class AbstractTrialDesigner<T : Trial>(
    protected val trialsConfig: TrialsConfiguration<T>, private var useNetworking: Boolean? = false) : View() {

    protected val codeSource = Runtime.getSourceCodePath(AbstractTrialDesigner::class.java)

    protected val filePathProperty = SimpleStringProperty(null)
    protected var filePath: String? by filePathProperty

}