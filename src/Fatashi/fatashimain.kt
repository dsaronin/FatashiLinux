// Fatashi -- a dictionary search & display package
package Fatashi

import java.io.File

// global parameters
const val APP_NAME = "fatashi"
const val WORK_FILE = "data/tempdict.txt"
const val PRODUCTION_FILE = "data/dsa_dictionary.txt"
const val FIELD_DELIMITERS = "(\\s+--\\s+)|(\t__[ \t\\x0B\\f]+)"
const val KAMUSI_FILE = WORK_FILE

fun main() {
    println("\n$APP_NAME starting...")
    val dictionary = Dictionary(KAMUSI_FILE,FIELD_DELIMITERS)

    dictionary.printStatus()
//    dictionary.listAll()

    println("...ending $APP_NAME")
}