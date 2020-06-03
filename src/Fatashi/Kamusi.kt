package Fatashi

import java.io.File

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
    private val keyModifiers = "#%&@"  // permits modification to search keys
    private val spaceReplace = "_"      // underscores in keys are replaced by space
    private val itemRegex = "(^=.+=$|[-~;:]?[\\w'+]+[;:]?)(\\W|[$keyModifiers])?(\\w+)?"
    /***************************************************************************************
     *     explanation for itemRegex (above):
     *     either completely escape the item:
     *     =aaaaaa= escapes item 'aaaaaa' literally to allow inclusion of RegEx operators
     *
     *     or use following syntax:
     *     ; -- will be used to indicate non-word boundary anchor ( \W )
     *          either prefixing or suffixing the item: ;aaa, aaaa;, or ;aaaa;
     *     : -- future
     *
     *     within the item itself:
     *     prefix - is used in kamusi to show a verb stem
     *     prefix ~ is used in kamusi to show an adjectival stem
     *     all _ (underscores) are replaced with spaces, to allow multiple words in search pattern
     *     ' is a normal swahili indicator for syllable ng' : -ng'ang'ania  -- grip sth tightly
     *     Future
     *          +  -- will be used to delimit a verb stem, followed by conjunction
     *                allowing swahili smart verb processing (see elsewhere)
     *                -kom+esha, from -koma (stem is -kom+a)
     *
     *     keyModifiers come after the item and act to constrain the search
     *          #abc  -- constrains matches only to records with (abc) qualifier in field 2: (tech)
     *          %n    -- constrains the search to only field n of each record
     *          &     -- invoke swahili smart processing (see elsewhere)
     *          @     -- future
     **********************************************************************/



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
/*
*   m=matches.firstOrNull()
*   m.groupValues[1]
 */
    // searchKeyList -- searches dictionary using List of Keys
    fun searchKeyList(wordList: List<String>): List<String>? {
        for (item in wordList) {
            val keyfrag = itemRegex.toRegex().find(item) ?: continue
            val results = findByEntry( keyfrag.groupValues[1] )
        }
        return null
    }


    // findByEntry  -- searches all entries and returns list of matching entries
    // args:
    //   pattern: string of regex search pattern
    // returns:  List<String>
    //   list of matching strings; null if none
    fun findByEntry(pattern: String): List<String>? {
        printBlue("  Pattern: >|$pattern|<" )
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
                ( if( pattern.first().equals(MyEnvironment.anchorHead) ) "" else MyEnvironment.fieldKeyHead )
                + pattern + MyEnvironment.fieldKeyTail
        )
    }

    // findByDefinition  -- searches Definition field in all entries and returns list of matching entries
    // args:
    //   pattern: string of regex search pattern
    // returns:
    //   list of matching strings; null if none
    fun findByDefinition(pattern: String): List<String>? {
        // wrap pattern with FIELD_DEF
        return findByEntry( MyEnvironment.fieldDefHead + pattern + MyEnvironment.fieldDefTail)
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
                MyEnvironment.fieldUsgHead + pattern +
                        ( if( pattern.last().equals(MyEnvironment.anchorTail) ) "" else MyEnvironment.fieldUsgTail )
        )
    }


}  // end class Dictionary