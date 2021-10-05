package generators

import javafx.collections.FXCollections.observableArrayList
import javafx.collections.ObservableList
import javafx.scene.image.Image
import javafx.scene.paint.Color

const val DEFAULT_BLOCK_LENGTH = 300
const val DEFAULT_NUM_PATIENTS = 6
const val DEFAULT_NUM_VITAL_SIGNS = 6



const val DEFAULT_VITAL_SIGN_VALUE = 50.0
val STANDARD_COLOURS_FIELDS = Color::class.java.fields.filter { it.type == Color::class.java }
val STANDARD_COLOURS = Array(STANDARD_COLOURS_FIELDS.size) { i -> Pair(STANDARD_COLOURS_FIELDS[i].name, STANDARD_COLOURS_FIELDS[i].get(null) as Color) }
val STANDARD_COLOURS_OBSERVABLE_LIST: ObservableList<Pair<String, Color>?> = observableArrayList(STANDARD_COLOURS.toList())




const val DEFAULT_EMBEDDED_TABLE_HEIGHT = 200.0
const val DEFAULT_MAIN_TABLE_WIDTH = 600.0


val HR_COLOR : Color = Color.GREEN
val BP_COLOR : Color = Color.rgb(186, 85, 211)
val SPO2_COLOR : Color = Color.rgb(135, 206, 250)
val RESP_COLOR : Color = Color.YELLOW
val ETCO2_COLOR : Color = Color.YELLOW
val TEMP_COLOR : Color = Color.WHITE


val MONITOR_COLOR_MAPPING = mapOf(
    Pair("HR", HR_COLOR),
    Pair("PULSE", HR_COLOR),
    Pair("NiBP", BP_COLOR),
    Pair("BP", BP_COLOR),
    Pair("SpO2", SPO2_COLOR),
    Pair("SPO2", SPO2_COLOR),
    Pair("SAT", SPO2_COLOR),
    Pair("RESP", RESP_COLOR),
    Pair("RR", RESP_COLOR),
    Pair("ETCO2", ETCO2_COLOR),
    Pair("EtCO2", ETCO2_COLOR),
    Pair("etCO2", ETCO2_COLOR),
    Pair("TEMP", TEMP_COLOR),
    Pair("Temp", TEMP_COLOR),
    Pair("T", TEMP_COLOR)
)

const val MONITOR_MAIN_FONT_SIZE = 20.0
const val MONITOR_SIDE_FONT_SIZE = 10.0