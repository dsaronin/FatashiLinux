package Fatashi

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException

const val APP_NAME = "fatashi"    // app name used in prompt
const val LIST_LINE_COUNT = 20   // how many lines of dict to list

class ConfigProperties(
        var appName: String = APP_NAME,     // app name used in prompt
        var listLineCount: Int = LIST_LINE_COUNT,   // how many lines of dict to list
        var verboseFlag: Boolean = false,  // true if verbose status information
        var debugFlag: Boolean = false,  // true if debug traces
        var prodFlag: Boolean = false,  // true if production (not test) mode
        val kamusiList: List<String> = listOf(""),  // ordered filenames for dictionaries
        val methaliList: List<String> = listOf(""), // ordered filenames for methali dicts
        val testList: List<String> = listOf("")     // ordered filenames for test dicts

) {
    companion object {

        fun readJsonConfigurations(f: String): ConfigProperties {
            val myProperties: ConfigProperties
            val myPropertiesType = object : TypeToken<ConfigProperties>() {}.type
            val gson = Gson()

            try {
                myProperties = gson.fromJson(File(f).readText(), myPropertiesType)
            } catch (ex: JsonSyntaxException) {
                println(ex)
                println("file: $f: there's a JSON formatting error")
                return ConfigProperties()
            } catch (ex: IOException) {
                println(ex)
                println("file: $f: caused an I/O Exception Error")
                return ConfigProperties()
            } catch (ex: Exception) {
                println(ex)
                println("file: $f: caused an Exception Error")
                return ConfigProperties()
            }

            return myProperties
        }
    }
}