package models


import javafx.beans.property.*
import javafx.collections.ObservableList
import tornadofx.*
import tornadofx.JsonModel
import javax.json.JsonObject

class Patient(id: Int = 1, name: String="test", dateOfBrith: String="01/01/0001", height:Int = 0, weight:Int = 0,
              allergies: ObservableList<String>,
              drugOneDosis: Double = 0.1,
              drugOneTime:String = "01:01",
              drugTwoDosis:Double = 0.2,
              drugTwoTime:String = "02:02",
              drugThreeDosis:Double = 0.3,
              drugThreeTime:String = "03:03",
              respiratoryDescription : String = "respi",
              respiratorySize:String = "respiSize",
              respiratoryType:String = "respiType",
              catheterDescription:String = "cD",
              catheterPosition:String ="cP",
              catheterType:String = "cT",
              tubeDescription :String = "tD",
              tubeTime : String = "tTi",
              tubeType : String = "tTy",
              positioningMain : String = "pM",
              positioningHead : String = "pH",
              positioningTrunk: String = "pT" ) : JsonModel {

    val idProperty = SimpleIntegerProperty(id)
    var id by idProperty

    val nameProperty = SimpleStringProperty(name)
    var name by nameProperty

    val dateOfBirthProperty = SimpleStringProperty(dateOfBrith)
    var dateOfBirth by dateOfBirthProperty

    val heightProperty = SimpleIntegerProperty(height)
    var height by heightProperty

    val weightProperty = SimpleIntegerProperty(weight)
    var weight by weightProperty

    val allergiesProperty = SimpleListProperty<String>(allergies)
    var allergies by allergiesProperty

    val drugOneDosisProperty = SimpleDoubleProperty(drugOneDosis)
    var drugOneDosis by drugOneDosisProperty

    val drugOneTimeProperty = SimpleStringProperty(drugOneTime)
    var drugOneTime by drugOneTimeProperty

    val drugTwoDosisProperty = SimpleDoubleProperty(drugTwoDosis)
    var drugTwoDosis by drugTwoDosisProperty

    val drugTwoTimeProperty = SimpleStringProperty(drugTwoTime)
    var drugTwoTime by drugTwoTimeProperty

    val drugThreeDosisProperty = SimpleDoubleProperty(drugThreeDosis)
    var drugThreeDosis by drugThreeDosisProperty

    val drugThreeTimeProperty = SimpleStringProperty(drugThreeTime)
    var drugThreeTime by drugThreeTimeProperty

    val respiratoryDescriptionProperty = SimpleStringProperty(respiratoryDescription)
    var respiratoryDescription by respiratoryDescriptionProperty

    val respiratoryTypeProperty = SimpleStringProperty(respiratoryType)
    var respiratoryType by respiratoryTypeProperty

    val respiratorySizeProperty = SimpleStringProperty(respiratorySize)
    var respiratorySize by respiratorySizeProperty

    val catheterDescriptionProperty = SimpleStringProperty(catheterDescription)
    var catheterDescription by catheterDescriptionProperty

    val catheterTypeProperty = SimpleStringProperty(catheterType)
    var catheterType by catheterTypeProperty

    val catheterPositionProperty = SimpleStringProperty(catheterPosition)
    var catheterPosition by catheterPositionProperty

    val tubeDescriptionProperty = SimpleStringProperty(tubeDescription)
    var tubeDescription by tubeDescriptionProperty

    val tubeTypeProperty = SimpleStringProperty(tubeType)
    var tubeType by tubeTypeProperty

    val tubeTimeProperty = SimpleStringProperty(tubeTime)
    var tubeTime by tubeTimeProperty

    val positioningMainProperty = SimpleStringProperty(positioningMain)
    var positioningMain by positioningMainProperty

    val positioningHeadProperty = SimpleStringProperty(positioningHead)
    var positioningHead by positioningHeadProperty

    val positioningTrunkProperty = SimpleStringProperty(positioningTrunk)
    var positioningTrunk by positioningTrunkProperty


    override fun updateModel(json: JsonObject) {
        with(json) {
            id = int("id")!!
            name = string("name")
            dateOfBirth = string("dateOfBirth")
            height= int("height")!!
            weight= int("weight")!!
            allergies.setAll("allergies")
            drugOneDosis = double("drugOneDosis")!!
            drugOneTime = string("drugOneTime")
            drugTwoDosis = double("drugTwoDosis")!!
            drugTwoTime = string("drugTwoTime")
            drugThreeDosis = double("drugThreeDosis")!!
            drugThreeTime = string("drugThreeTime")
            respiratoryDescription = string("respiratoryDescription")
            respiratorySize = string("respiratorySize")!!
            respiratoryType = string("respiratoryType")
            catheterDescription = string("catheterDescription")
            catheterPosition = string("catheterDescription")
            catheterType = string("catheterType")
            tubeDescription = string("tubeDescription")
            tubeTime = string("tubeTime")
            tubeType = string("tubeType")
            positioningMain = string("positioningMain")
            positioningHead=string("positioningHead")
            positioningTrunk=string("positioningTrunk")

        }
    }

    override fun toJSON(json: JsonBuilder) {
        with(json) {
            add("id", id)
            add("name", name)
            add("dateOfBirth", dateOfBirth)
            add("height", height)
            add("weight", weight)
            add("allergies", allergies)
            add("drugOneDosis", drugOneDosis)
            add("drugOneTime", drugOneTime)
            add("drugTwoDosis", drugTwoDosis)
            add("drugTwoTime", drugTwoTime)
            add("drugThreeDosis", drugThreeDosis)
            add("drugThreeTime", drugThreeTime)
            add("respiratoryDescription", respiratoryDescription)
            add("respiratorySize", respiratorySize)
            add("respiratoryType", respiratoryType)
            add("catheterDescription", catheterDescription)
            add("catheterDescription", catheterPosition)
            add("catheterType", catheterType)
            add("tubeDescription", tubeDescription)
            add("tubeTime", tubeTime)
            add("tubeType", tubeType)
            add("positioningMain", positioningMain)
            add("positioningHead", positioningHead)
            add("positioningTrunk", positioningTrunk)
        }
    }



    override fun toString(): String = toJSON().toString()

}