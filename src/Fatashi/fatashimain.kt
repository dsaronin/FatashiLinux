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
    Fatashi.work()
    println("...ending $APP_NAME")
}

object Fatashi  {
        // instantiate dictionary
    private val dictionary = Dictionary(KAMUSI_FILE,FIELD_DELIMITERS)

    init {
        // show that dictionary is viable & ready
        dictionary.printStatus()
    }

    // fatashi work loops through commands
    // arg: environment (future)

    fun work(  ) {
        var cmd: String
        var loop = true

        do {
            print("$APP_NAME > ")  // command prompt
            cmd = readLine() ?: "exit"   // accept a command

                // parse command
            when (cmd) {
                "exit"      -> loop = false   // exit program
                "quit"      -> loop = false  // exit program
                "tafuta"    -> dictionary.findByEntry( cmd )  // search dictionary
                "list"      -> dictionary.listAll()   // list dictionary
                "sts"       -> dictionary.printStatus()   // dict status
                "help"      -> println("tafuta, list, sts, help, quit, exit")
                else        -> println("$cmd is unrecognizable; try again!")
            }

        } while ( loop )
    }
}