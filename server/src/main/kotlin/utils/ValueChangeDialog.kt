package utils

import javafx.scene.control.TextField
import tornadofx.*

class ValueChangeDialog(title : String, savingAction : (value : String) -> Unit, value: String? = "") : View(title) {
    private var validator : ValidationContext.Validator<*>? = null
    private val input = TextField(value)
    override val root = vbox (20) {
        add(input)

        button("Save") {
            action {
                if (validator != null && validator?.validate() == false) {
                    return@action
                }
                try {
                    savingAction(input.text.trim())
                } catch (ignored : Exception ) { }
                close()
            }
        }
    }

    fun addValidation(func : (value : String?) -> ValidationMessage?) {
        this.validator = ValidationContext().addValidator(input, input.textProperty(), ValidationTrigger.None) {
            return@addValidator func(it?.trim())
        }
    }
}