package Fatashi

import java.io.File
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
        parseArgList(args)
        if (debugFlag) {
            printProperties()
            printArgList(args)
        }
    }

    // listProperties -- list the config.properties file as seen by kotlin
    fun printProperties() {
        var propkeys = mutableListOf<String>()

        for (key in appProps.propertyNames()) {
            propkeys.add( key.toString() )
        }

        println("config property values: ")
        for ( key in propkeys.sortedBy({it.toLowerCase()}) ) {
            println("$key: >|${appProps[key]}|<")
        }
    }

    private fun printArgList(args: Array<String>) {
        println( if( args.isEmpty() ) "No args passed." else "My calling args are...")
        for (i in args.indices ) println("args[$i] is: ${args[i]}")
    }

    // ex: -v -n 5 -d --version --kamusi1 "dsa_dictionary.txt" --kamusi2 "tuki_kamusi.txt" --methali "methali_kamusi.txt"

    // regex recognizer for short/long flags and extracts the flag w/o punctuation
    private val flag_regex = Regex("""--?(\w\b|\w+)""")

    fun parseArgList(args: Array<String>) {
        if( args.isEmpty() ) return

        var lifo = Stack<String>()

        // build from args LIFO stack in reverse (right-to-left) for top-down parsing
        for( i in args.indices.reversed() ) lifo.push( args[i] )

        // now parse stack (LIFO == args left-to-right)
        while( !lifo.isEmpty() ) {
            var flagArg = lifo.pop()

            val matches = flag_regex.findAll( flagArg )  // recognize & extract
            val m=matches.firstOrNull()
            if (m == null) {
                printArgUsageError("invalid flag syntax: $flagArg")
            }
            else {
                // extract the flag (it will be in either of the two groupings, but not both
                val flag = if( !m.groupValues[1].isEmpty() ) m.groupValues[1] else m.groupValues[2]

                // flag is now the extracted flag
                // println("extracted flag is: $flag")
                when (flag) {
                    "n"             -> listLineCount = popValueOrDefault(lifo,_LIST_LINE_COUNT.toString())
                    "v", "verbose"  -> verboseFlag = true
                    "d", "debug"    -> debugFlag = true
                    "h", "help"     -> printHelp()
                    "version" -> Version.printMyVersion( " " )
                    "kamusi1" -> kamusiMainFile = popFileNameOrDefault(lifo,KAMUSI_FILE)
                    "kamusi2" -> kamusiStdFile = popFileNameOrDefault(lifo,KAMUSI_STANDARD_FILE)
                    "methali1" -> methaliStdFile = popFileNameOrDefault(lifo,METHALI_STANDARD_FILE)

                    else -> printArgUsageError("unknown flag: $flag")
                }
            }

        }
        if (verboseFlag) printOptions()
    }

    private fun printOptions() {
        var optionList = "verbose (%b), debug (%b), list n(%d), main (%s), tuki (%s), methali (%s)"

        println(
                optionList.format(
                        verboseFlag, debugFlag, listLineCount,
                        kamusiMainFile, kamusiStdFile, methaliStdFile
                )
        )

    }

    // printHelp  -- outputs std arg line expected
    private fun printHelp() {
        val argLine = "\$ $APP_NAME [<options>] \n  <options> ::= -v -d -n dddd --version --help \n  -v: verbose, -d: debug traces, -n: dictionary list lines <nn>"

        println( AnsiColor.wrapBlue("Usage and Argument line expected: "))
        println( AnsiColor.wrapBlue(argLine) )
    }

    // popValueOrDefault -- peeks ahead on LIFO and pops if valid value; else default
    private fun popValueOrDefault(lifo: Stack<String>, default: String): Int {
            // peek at top of stack; if missing, use default
        var num = lifo.peek() ?: default

            // if it matches another flag, use default && dont pop
        if ( flag_regex.matches( num ) ) {
            num = default
        }

        else {
            lifo.pop()   // then pop from stack since it wasn't the next flag

            // but if it doesn't match a number
            if ( ! """^\d+$""".toRegex().matches(num) ) {
                   // it was an argument format error
                printArgUsageError( num )  // warn user
                num = default   // use default
            }
        }

        return num.toInt()
    }

    // popFileNameOrDefault  -- peeks ahead at LIFO && pops valid filename, else default
    private fun popFileNameOrDefault(lifo: Stack<String>, default: String): String {
        // peek at top of stack; if missing, use default
        var str = lifo.peek() ?: default

        // if it matches another flag, use default && dont pop
        if ( flag_regex.matches( str ) ) {
            str = default
        }

        else {
            lifo.pop()   // then pop from stack since it wasn't the next flag

            // but if it doesn't actually match a file on the system...
            if ( ! File( str ).exists() ) {
                // filename wasn't valid or doesn't exist
                printArgUsageError( "filename <$str> isn't valid or doesn't exit" )  // warn user
                str = default   // use default
            }
        }

        return str
    }

    fun printInfo(s: String){
        println( AnsiColor.wrapBlue( s ))
    }

    // ******** output usage error information ***********

    fun printUsageError(s: String) {
        // System.err.println >>> not used because of weirdness against prompt line
        println( AnsiColor.wrapRed("***** $s *****") )
    }

    private fun printArgUsageError(s: String) {
        printUsageError("Command line arg input error: $s")
    }

}
