package Fatashi

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException

class KamusiFormat {

    companion object {
        fun readJsonKamusiFormats(f: String) : KamusiFormat {
            val kamusiFormat : KamusiFormat
            val gson = Gson()
            val kamusiFormatType = object : TypeToken <KamusiFormat>() {}.type

            try {
                kamusiFormat = gson.fromJson( File(f).readText(), kamusiFormatType)
            }
            catch(ex: NumberFormatException){
                println(ex)
                println("file: $f: you're incorrectly specifying a number")
                return KamusiFormat()
            }
            catch(ex: JsonSyntaxException){
                println(ex)
                println("file: $f: there's a JSON formatting error")
                return KamusiFormat()
            }
            catch (ex: IOException) {
                println(ex)
                println("file: $f: caused an I/O Exception Error")
                return KamusiFormat()
            }
            catch(ex: Exception){
                println(ex)
                println("file: $f: caused an Exception Error")
                return KamusiFormat()
            }

            return kamusiFormat
        }
        
    }
//********************************************************************************8
//********************************************************************************8
//********************************************************************************8

}