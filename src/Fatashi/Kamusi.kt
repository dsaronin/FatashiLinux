package Fatashi

import java.io.File


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

// Dictionary handles everything re dictionary database, but has no language-specific logic
// properties required:
//   _kamusi_file: String,  // filename for raw dictionary
//   _field_delimiters: String,  // regex pattern to find main fields
//   _internal_fields: String = "\t",  // internal field delimiter defaults to tab
//   _record_delimiter: String  = "\n"    // dict record delimiter defaults to newline
class Kamusi(
        _kamusi_file: String,  // file object for raw dictionary
        _field_delimiters: String,  // regex pattern to find main fields
        _internal_fields: String = "\t",  // internal field delimiter
        _record_delimiter: String = "\n"    // dict record delimiter
) {
    // Object properties
    private val kamusiFile: File
    private val fieldDelimiter: Regex
    private val dictionary: List<String>

// initialize by opening file, reading in raw dict, parsing fields, and splitting into records
    init {
            // open file
        kamusiFile = File(_kamusi_file)
            // make regex pattern for replacing with std field delimiters
        fieldDelimiter = Regex( _field_delimiters )
            // read entire dict, replace all field delims with tab
            // then split into list of individual lines
        dictionary = fieldDelimiter.replace(
                kamusiFile.readText(),
                _internal_fields
        )
                .split(_record_delimiter)

    }  // end init class

//************************************************************************************
//****** utility methods         *****************************************************
//************************************************************************************

    // printStatus -- output status of dictionary
    fun printStatus() {
        println( "My dictionary, $kamusiFile (readable: ${kamusiFile.canRead()}), has ${dictionary.count()} entries")
    }

    // listAll -- output entire dictionary internal representation
    fun listAll() {
        dictionary.forEach {
            println( it )
        }
    }


//************************************************************************************
//****** search methods         *****************************************************
//************************************************************************************

    // findByEntry  -- searches all entries and returns list of matching entries
    // args:
    //   pattern: string of regex search pattern
    // returns:
    //   list of matching strings; null if none
    fun findByEntry(pattern: String): List<String>? {
        println("Pattern: >|$pattern|<" )
        return null
    }

    // findByKey  -- searches Key field in all entries and returns list of matching entries
    // args:
    //   pattern: string of regex search pattern
    // returns:
    //   list of matching strings; null if none
    fun findByKey(pattern: String): List<String>? {
        // wrap pattern with FIELD_KEY_HEAD unless pattern begins with "^"
        // and FIELD_KEY_TAIL

        return findByEntry(
                ( if( pattern.first().equals(ANCHOR_HEAD) ) "" else FIELD_KEY_HEAD )
                + pattern + FIELD_KEY_TAIL
        )
    }

    // findByDefinition  -- searches Definition field in all entries and returns list of matching entries
    // args:
    //   pattern: string of regex search pattern
    // returns:
    //   list of matching strings; null if none
    fun findByDefinition(pattern: String): List<String>? {
        // wrap pattern with FIELD_DEF
        return findByEntry( FIELD_DEF_HEAD + pattern + FIELD_DEF_TAIL)
    }

    // findByUsage  -- searches Usage field in all entries and returns list of matching entries
    // args:
    //   pattern: string of regex search pattern
    // returns:
    //   list of matching strings; null if none
    fun findByUsage(pattern: String): List<String>? {
        // wrap pattern with FIELD_USG_HEAD
        // and FIELD_USG_TAIL, unless pattern ends with "$"

        return findByEntry(
                FIELD_USG_HEAD + pattern +
                        ( if( pattern.last().equals(ANCHOR_TAIL) ) "" else FIELD_USG_TAIL )
        )
    }


}  // end class Dictionary