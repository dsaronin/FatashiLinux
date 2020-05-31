package Fatashi

import java.io.FileInputStream
import java.util.*

// global parameters
const val APP_NAME = "fatashi"
const val WORK_FILE = "data/tempdict.txt"
const val PRODUCTION_FILE = "data/dsa_dictionary.txt"
const val FIELD_DELIMITERS = "(\\s+--\\s+)|(\t__[ \t\\x0B\\f]+)"
const val KAMUSI_FILE = WORK_FILE

const val CONFIG_PROPERTIES="config.properties"

// patterns for wrapping a search string to constrain it to a given dictionary field
// KEY and USAGE fields, coming at head or tail, each have two parts;
// the KEY_HEAD might not be required if the search pattern begins with "^", to anchor at BOL
// the USG_TAIL might not be required if the search pattern ends with "$", to anchor at EOL
// for consistency, FIELD_DEF also has HEAD and TAIL

const val FIELD_KEY_HEAD = "^.*"  // item KEY is first field before TAB
const val FIELD_KEY_TAIL = ".*\t"  // item KEY is first field before TAB
const val FIELD_DEF_HEAD = "^.*\t.*"  // item DEFINITION is second field between two tabs
const val FIELD_DEF_TAIL = ".*\t"  // item DEFINITION is second field between two tabs
const val FIELD_USG_HEAD = "^.*\t.*\t.*"  // item USAGE is third field, prior to EOL
const val FIELD_USG_TAIL = ".*$"  // item USAGE is third field, prior to EOL
const val ANCHOR_HEAD = '^'     // pattern anchor for head of FIELD_KEY
const val ANCHOR_TAIL = '$'     // pattern anchor for tail of FIELD_USG

// DEFAULTS for ARG line options
const val _LIST_LINE_COUNT = 20
const val KAMUSI_STANDARD_FILE = "data/tuki_kamusi.txt"
const val METHALI_STANDARD_FILE = "data/methali_kamusi.txt"


object MyEnvironment {
        // appProps will be properties read in from config.properties
    private val appProps = Properties()

        // set defaults; modifiable by external properties or command line
    var appName = APP_NAME
    var workFilename = WORK_FILE
    var productionFilename= PRODUCTION_FILE
    var fieldDelimiters = FIELD_DELIMITERS

        // calling arg flags
    var listLineCount = _LIST_LINE_COUNT
    var verboseFlag = false
    var debugFlag = false
    var kamusiMainFile = KAMUSI_FILE
    var kamusiStdFile = KAMUSI_STANDARD_FILE
    var methaliStdFile = METHALI_STANDARD_FILE

    // load the properties file and initialize variables
    init {
        appProps.load( FileInputStream(CONFIG_PROPERTIES) )
    }

    // setup -- initializes the environment
    // args -- are the cli argument list when invoked
    fun setup(args: Array<String>): Unit {
//        listProperties()
//        listArgList(args)
        parseArgList(args)
    }

    // listProperties -- list the config.properties file as seen by kotlin
    fun listProperties() {
        var propkeys = mutableListOf<String>()

        for (key in appProps.propertyNames()) {
            propkeys.add( key.toString() )
        }

        println("config property values: ")
        for ( key in propkeys.sortedBy({it.toLowerCase()}) ) {
            println("$key: >|${appProps[key]}|<")
        }
    }

    fun listArgList(args: Array<String>) {
        println( if( args.isEmpty() ) "No args passed." else "My calling args are...")
        for (i in args.indices ) println("args[$i] is: ${args[i]}")
    }

    // ex: -v -n 5 -d --version --kamusi1 "dsa_dictionary.txt" --kamusi2 "tuki_kamusi.txt" --methali "methali_kamusi.txt"

    fun parseArgList(args: Array<String>) {
        if( args.isEmpty() ) return

        var lifo = Stack<String>()

        // build stack in reverse (right-to-left) from args for top-down parsing
        for( i in args.indices.reversed() ) lifo.push( args[i] )

        // regex recognizer for short/long flags and extracts the flag w/o punctuation
        val flag_regex = Regex("""-(\w\b|-\w+)""")

        // now parse stack (left-to-right)
        while( !lifo.isEmpty() ) {
            var flagArg = lifo.pop()

            val matches = flag_regex.findAll( flagArg )  // recognize & extract
            val m=matches.firstOrNull()
            if (m == null) {
                printArgUsageError(args, "invalid flag syntax: $flagArg")
            }
            else {
                // extract the flag (it will be in either of the two groupings, but not both
                val flag = if( !m.groupValues[1].isEmpty() ) m.groupValues[1] else m.groupValues[2]

                // flag is now the extracted flag
                // println("extracted flag is: $flag")
                when (flag) {
                    "n"             -> listLineCount = popValueOrDefault(lifo,_LIST_LINE_COUNT)
                    "v", "verbose"  -> verboseFlag = true
                    "d", "debug"    -> debugFlag = true
                    "h", "help"     -> printHelp()
                    "version" -> Version.printMyVersion( " " )
                    "kamusi1" -> kamusiMainFile = popFileNameOrDefault(lifo,KAMUSI_FILE)
                    "kamusi2" -> kamusiStdFile = popFileNameOrDefault(lifo,KAMUSI_STANDARD_FILE)
                    "methali1" -> methaliStdFile = popFileNameOrDefault(lifo,METHALI_STANDARD_FILE)

                    else -> printArgUsageError(args, "unknown flag: $flag")
                }
            }

        }
    }

    private val argLine = "usage: \$ $APP_NAME [<options>] \n  <options> ::= -v -d -n dddd --version --help \n  -v: verbose, -d: debug traces, -n: dictionary list lines <nn>"

    // printHelp  -- outputs std arg line expected
    private fun printHelp() {
        println( argLine )
    }

    private fun popValueOrDefault(lifo: Stack<String>, default: Int): Int {
        return default
    }

    private fun printArgUsageError(args: Array<String>, s: String) {
        println("Command line arg input error: $s")
        printHelp()
    }

    private fun popFileNameOrDefault(lifo: Stack<String>, default: String): String {
        return default
    }

}
