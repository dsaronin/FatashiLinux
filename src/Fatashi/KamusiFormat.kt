package Fatashi

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException

data class KamusiFormat(
        val filename: String,
        val kamusiType: String,
        val kamusiLang: String,
        val fieldDelimiters: String,
        val wrapKeyHead: String,
        val wrapKeyTail: String,
        val wrapDefHead: String,
        val wrapDefTail: String,
        val wrapUsgHead: String,
        val wrapUsgTail: String,
        val hashtagOn: Boolean,
        val hashtagWrapPattern: String,
        val percentOn: Boolean,
        val ampersandOn: Boolean,
        val atsignOn: Boolean,
        val atsignWrapPattern: String
) {
    // secondary constructor for instantiating empty object
        constructor() : this(
                "", "", "",
                "","","",
                "","",
                "","",
                false, "",
                false,false,
                false, ""
        )

companion object {

    fun readJsonKamusiFormats(f: String) : KamusiFormat {
        val kamusiFormat : KamusiFormat
        val kamusiFormatType = object : TypeToken<KamusiFormat>() {}.type
        val gson = Gson()

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



}  // class
