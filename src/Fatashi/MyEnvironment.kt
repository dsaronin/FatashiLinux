package Fatashi

import java.io.File
import java.io.FileInputStream
import java.util.*

fun Boolean.toChar() = this.toString().first()

// **************************************************************************
// default parameters; publicly visible
// **************************************************************************
const val APP_NAME = "fatashi"
const val CONFIG_PROPERTIES_FILE ="config.properties"

// **************************************************************************
// default parameters; not to be used outside of MyEnvironment
// **************************************************************************

private const val WORK_FILE             = "data/tempdict.txt"
private const val PRODUCTION_FILE       = "data/dsa_dictionary.txt"

private const val MAIN_FIELD_DELIMITERS = "(\\s+--\\s+)|(\t__[ \t\\x0B\\f]+)"

// patterns for wrapping a search string to constrain it to a given dictionary field
// KEY and USAGE fields, coming at head or tail, each have two parts;
// the KEY_HEAD might not be required if the search pattern begins with "^", to anchor at BOL
// the USG_TAIL might not be required if the search pattern ends with "$", to anchor at EOL
// for consistency, FIELD_DEF also has HEAD and TAIL

private const val FIELD_KEY_HEAD = """^[^\t]*"""  // item KEY is first field before TAB
private const val FIELD_KEY_TAIL = """[^\t]*\t"""  // item KEY is first field before TAB
private const val FIELD_DEF_HEAD = """^[^\t]+\t[^\t]*"""  // item DEFINITION is second field between two tabs
private const val FIELD_DEF_TAIL = """[^\t]*\t"""  // item DEFINITION is second field between two tabs
private const val FIELD_USG_HEAD = """^[^\t]+\t[^\t]+\t[^\t]*"""  // item USAGE is third field, prior to EOL
private const val FIELD_USG_TAIL = """[^\t]*$"""  // item USAGE is third field, prior to EOL
private const val ANCHOR_HEAD = '^'     // pattern anchor for head of FIELD_KEY
private const val ANCHOR_TAIL = '$'     // pattern anchor for tail of FIELD_USG

// DEFAULTS for ARG line options
private const val LIST_LINE_COUNT = 20

object MyEnvironment {
        // appProps will be properties read in from config.properties
    private val appProps = Properties()

    // **************************************************************************
    // ******** MyEnvironment public properties  ********************************
    // **************************************************************************

    // calling argline flags
    var listLineCount = LIST_LINE_COUNT   // how many lines of dict to list
    var verboseFlag = false  // true if verbose status information
    var debugFlag = false  // true if debug traces
    var prodFlag = false  // true if use KAMUSI_MAIN_FILE

    // set defaults; modifiable by external properties or command line
    var appName = APP_NAME
    var  anchorHead       = ANCHOR_HEAD
    var  anchorTail       = ANCHOR_TAIL

    var fieldDelimsMain   = MAIN_FIELD_DELIMITERS

    var  fieldKeyHead     = FIELD_KEY_HEAD
    var  fieldKeyTail     = FIELD_KEY_TAIL
    var  fieldDefHead     = FIELD_DEF_HEAD
    var  fieldDefTail     = FIELD_DEF_TAIL
    var  fieldUsgHead     = FIELD_USG_HEAD
    var  fieldUsgTail     = FIELD_USG_TAIL

    var workFile        = WORK_FILE
    var productionFile  = PRODUCTION_FILE

    var kamusiMainFile      = if( prodFlag ) PRODUCTION_FILE else WORK_FILE

    // **************************************************************************
    // **************************************************************************

    // load the properties file and initialize variables
    init {
        loadAndSetProperties()  // get everything started
    }

    // **************************************************************************
    // **************************************************************************

    // setup -- initializes the environment
    // args -- are the cli argument list when invoked
    fun setup(args: Array<String>): Unit {
        parseArgList(args)
        if (debugFlag) {
            printProperties()
            printArgList(args)
        }
        // mainfile depends on whether we're in dev or production mode
        kamusiMainFile    = if( prodFlag ) productionFile else workFile
        if (verboseFlag) printOptions()
    }

    // loadAndSetProperties  -- grab external configuration parameters & set
    fun loadAndSetProperties() {
        appProps.load( FileInputStream(CONFIG_PROPERTIES_FILE) )

        productionFile    = appProps.getProperty("PRODUCTION_FILE") ?: PRODUCTION_FILE
        workFile          = appProps.getProperty("WORK_FILE") ?: WORK_FILE

        fieldDelimsMain  = appProps.getProperty("MAIN_FIELD_DELIMITERS") ?: MAIN_FIELD_DELIMITERS

        fieldKeyHead     = appProps.getProperty("FIELD_KEY_HEAD") ?: FIELD_KEY_HEAD
        fieldKeyTail     = appProps.getProperty("FIELD_KEY_TAIL") ?: FIELD_KEY_TAIL
        fieldDefHead     = appProps.getProperty("FIELD_DEF_HEAD") ?: FIELD_DEF_HEAD
        fieldDefTail     = appProps.getProperty("FIELD_DEF_TAIL") ?: FIELD_DEF_TAIL
        fieldUsgHead     = appProps.getProperty("FIELD_USG_HEAD") ?: FIELD_USG_HEAD
        fieldUsgTail     = appProps.getProperty("FIELD_USG_TAIL") ?: FIELD_USG_TAIL

    }

    // listProperties -- list the config.properties file as seen by kotlin
    fun printProperties() {
        val propkeys = mutableListOf<String>()

        for (key in appProps.propertyNames()) {
            propkeys.add( key.toString() )
        }

        println("config property values: ")
        for ( key in propkeys.sortedBy {it.toLowerCase()}) {
            println("$key: >|${appProps[key]}|<")
        }
    }

    // regex recognizer for short/long flags and extracts the flag w/o punctuation
    private val flag_regex = Regex("""--?(\w\b|\w+)""")

    // parseArgList -- parse the command line argument option flags
    // ex: -v -n 5 -d --version --kamusi1 "dsa_dictionary.txt"
    fun parseArgList(args: Array<String>) {
        if( args.isEmpty() ) return

        val lifo = Stack<String>()

        // build from args LIFO stack in reverse (right-to-left) for top-down parsing
        for( i in args.indices.reversed() ) lifo.push( args[i] )

        // now parse stack (LIFO == args left-to-right)
        while( !lifo.isEmpty() ) {
            val flagArg = lifo.pop()

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
                    "n"             -> listLineCount = popValueOrDefault(lifo,LIST_LINE_COUNT.toString())
                    "v", "verbose"  -> verboseFlag = true
                    "d", "debug"    -> debugFlag = true
                    "p", "prod"     -> prodFlag = true
                    "h", "help"     -> printHelp()
                    "version" -> Version.printMyVersion( " " )
                    "kamusi1"  -> kamusiMainFile = popFileNameOrDefault(lifo, productionFile )
//                    "kamusi2"  -> kamusiStdFile = popFileNameOrDefault(lifo,KAMUSI_STANDARD_FILE)
//                    "methali1" -> methaliStdFile = popFileNameOrDefault(lifo,METHALI_STANDARD_FILE)

                    else -> printArgUsageError("unknown flag: $flag")
                }
            }

        }
    }

    // printInfo -- print something informative, wrapped in Blue
    fun printInfo(s: String){
        Fatashi.printInfo( s )
    }

    // printUsageError -- print an error, wrapped in Red
    fun printUsageError(s: String) {
        // System.err.println >>> not used because of weirdness against prompt line
        printError("***** $s *****")
    }

    // **************************************************************************
    // *****  private internal MyEnvironment Functions  *************************
    // **************************************************************************

    // printArgList -- outputs the command line argument option flags
    private fun printArgList(args: Array<String>) {
        println( if( args.isEmpty() ) "No args passed." else "My calling args are...")
        for (i in args.indices ) println("args[$i] is: ${args[i]}")
    }

    // printOptions -- display the current state of options
    fun printOptions() {
        val optionList = "  verbose (%c), debug (%c), prod (%c) list n(%d), main (%s)"

        printInfo(
                optionList.format(
                        verboseFlag.toChar(), debugFlag.toChar(), prodFlag.toChar(),
                        listLineCount,
                        kamusiMainFile
                )
        )

    }

    // printHelp  -- outputs std arg line expected
    private fun printHelp() {
        val argLine = "\$ $APP_NAME [<options>] \n  <options> ::= -v -d -n dddd --version --help \n  -v: verbose, -d: debug traces, -n: dictionary list lines <nn>"

        Fatashi.printInfo("Usage and Argument line expected: ")
        Fatashi.printInfo(argLine)
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

    // ******** output usage error information ***********

    private fun printArgUsageError(s: String) {
        printUsageError("Command line arg input error: $s")
    }

}
