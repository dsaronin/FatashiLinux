// Fatashi -- a dictionary search & display package
package Fatashi

import java.io.FileInputStream
import java.util.Properties


// global parameters
const val APP_NAME = "fatashi"
const val WORK_FILE = "data/tempdict.txt"
const val PRODUCTION_FILE = "data/dsa_dictionary.txt"
const val FIELD_DELIMITERS = "(\\s+--\\s+)|(\t__[ \t\\x0B\\f]+)"
const val KAMUSI_FILE = WORK_FILE

const val CONFIG_PROPERTIES="config.properties"

fun main(args: Array<String>) {
    val appProperties = initMain( Properties() )

    println("\n$APP_NAME starting...")
    Fatashi.work()
    println("...ending $APP_NAME")
}

private fun initMain(appProps: Properties): Properties {
        // load the properties file
    appProps.load( FileInputStream(CONFIG_PROPERTIES) )

    println("config keys: ")
    for ( key in appProps.propertyNames() ) {
        println("$key: >|${appProps[key]}|<")
    }

    return appProps
}

object Fatashi  {
        // instantiate kamusi
    private val kamusi = Kamusi(KAMUSI_FILE, FIELD_DELIMITERS)

    init {
        // show that dictionary is viable & ready
        kamusi.printStatus()
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
                "tafuta"    -> kamusi.findByEntry( cmd )  // search dictionary
                "list"      -> kamusi.listAll()   // list dictionary
                "sts"       -> kamusi.printStatus()   // dict status
                "help"      -> println("tafuta, list, sts, help, quit, exit")
                else        -> println("$cmd is unrecognizable; try again!")
            }

        } while ( loop )
    }
}