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
// for consistancy, FIELD_DEF also has HEAD and TAIL

const val FIELD_KEY_HEAD = "^.*"  // item KEY is first field before TAB
const val FIELD_KEY_TAIL = ".*\t"  // item KEY is first field before TAB
const val FIELD_DEF_HEAD = "^.*\t.*"  // item DEFINITION is second field between two tabs
const val FIELD_DEF_TAIL = ".*\t"  // item DEFINITION is second field between two tabs
const val FIELD_USG_HEAD = "^.*\t.*\t.*"  // item USAGE is third field, prior to EOL
const val FIELD_USG_TAIL = ".*$"  // item USAGE is third field, prior to EOL
const val ANCHOR_HEAD = '^'     // pattern anchor for head of FIELD_KEY
const val ANCHOR_TAIL = '$'     // pattern anchor for tail of FIELD_USG

object MyEnvironment {
    lateinit var appName: String
    lateinit var workFilename: String
    lateinit var productionFilename: String
    lateinit var fieldDelimiters: String

    private val appProps = Properties()

    // load the properties file
    init {
        appProps.load( FileInputStream(CONFIG_PROPERTIES) )
    }

    // setup -- intializes the environment
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
    fun parseArgList(args: Array<String>) {
        if( args.isEmpty() ) return

        var lifo = Stack<String>()

        // build stack in reverse (right-to-left) from args for top-down parsing
        for( i in args.indices..0 ) {
            lifo.push( args[i] )
        }

        val flag_regex = Regex("""-(\w\b|-\w+)""")
        // now parse stack (left-to-right)
        while( !lifo.isEmpty() ) {
            var flag = lifo.pop()

            val matches = rex.findAll( sflag )
            val m=mtch1.firstOrNull()
            if (m != null) {
                val flag = if( m.groupValues[1].isEmpty() ) m.groupValues[2] else m.groupValues[1]
                println("value: ${m.value}; group: $flag")
            }
            else println("no match")
        }
    }

}
