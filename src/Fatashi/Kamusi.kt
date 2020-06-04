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
    private val internalFields = _internal_fields
    private val recordDelimiter = _record_delimiter
    private val showKeyDelim = "\t-- "
    private val showUsgDelim = ":\t "
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
    fun searchKeyList(wordList: List<String>) {
        // for each search key in list, search the dictionary
        for (item in wordList) {
            print( AnsiColor.wrapGreen(">>>>>>>>> $item >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"))
                // strip off the key fragment from the constraints
            val keyfrag = itemRegex.toRegex().find(item) ?: continue
                // search the dictionary for that key fragment, after prepping the key
                /* ****************************************************************
                 * at this point, keyfrag has:
                 *     keyfrag.groupValues[0] -- full search term
                 *     keyfrag.groupValues[1] -- key
                 *     keyfrag.groupValues[2] -- constraint code: #, %, &, @, ...
                 *     keyfrag.groupValues[3] -- constraint parameter: abcd, nn
                 * ****************************************************************
                 */
            var keyitem = prepKey( keyfrag.groupValues[1] )

            // handle any kind of constraint, possibly altering keyitem
            keyitem = when (keyfrag.groupValues[2]) {
                "#"  -> prepTypeConstraint(keyitem, keyfrag.groupValues[3])
                "%"  -> prepByField(keyitem, keyfrag.groupValues[3] )
                "&"  -> prepSwahili(keyitem)
                "@"  -> keyitem   // NOP for now; future expansion
                else -> keyitem
            }

            findByEntry( keyitem )  // perform search, output results
        }
    }

    // findByEntry  -- searches all entries and returns list of matching entries
    // args:
    //   pattern: string of regex search pattern
    fun findByEntry(pattern: String) {
        // display the search pattern if verbose
        if (MyEnvironment.verboseFlag) print(AnsiColor.wrapGreen(">|$pattern|<")+"\n" )
        val itemRegex = pattern.toRegex()  // convert key to regex
            // display results, if any found
        printResults(
                    // filter dictionary grabbing only records with a match
                dictionary.filter { itemRegex.containsMatchIn(it) },
                itemRegex
        )
    }

    // printResults  -- handles all output for found items
    // output consists in pretty formatting dictionary entry, then highlighting found items
    // args:
    //   res: list of dictionary entries with at least one match
    //   rex: the regex of the search key determining that match
    fun printResults( res: List<String>, rex: Regex ){
        res.forEach {
            // output each line after highlighting the found text
            println( it
                    .replace(internalFields, showKeyDelim)
                    .replace(rex) { AnsiColor.wrapBlueBold( it.groupValues[0] )} )
        }
        if (MyEnvironment.verboseFlag) printWarn( "${res.size} results")
    }

     /*
     * ***********************************************************************
     * ******** massaging key & prep functions    ****************************
     * ***********************************************************************
     */

    // prepKey -- massages key to add/remove based on special symbols
    // NOTE: by this point, mid-term ';' and ':' have been parsed out,
    //       so if present, they will only be leading and trailing
    private fun prepKey(s: String): String {
        return s
                .replace(";".toRegex(), """\\W""")  // non-word boundaries
                .replace("_".toRegex(), " ")     // underscore ==> space
                .trim()         // lead/trailing spaces
                .removeSurrounding("=", "=")   // remove literal escape chars

    }

    // handleByField  -- massage key to constrain the search to a given dict field
    // also handles the search & printout
    private fun prepByField(keyitem: String, dfield: String): String {
        return when (dfield) {
            "1"  -> constrainToKeyField( keyitem )
            "2"  -> constrainToDefField( keyitem )
            "3"  -> constrainToUsageField( keyitem )
            else -> constrainToKeyField( keyitem )
        }
    }

    // prepConstraint  -- preps a constraint for the search
    private fun prepTypeConstraint(keyitem: String, cfield: String): String {
        return keyitem
    }

    // prepSwahili -- preps the item for expanded swahili handling
    private fun prepSwahili(keyitem: String): String  {
        return keyitem
    }

    // findByKey  -- searches Key field in all entries and returns list of matching entries
    // wrap pattern with FIELD_KEY_HEAD unless pattern begins with "^"
    // and FIELD_KEY_TAIL
    private fun constrainToKeyField(pattern: String): String {
        return (
            if( pattern.first().equals(MyEnvironment.anchorHead)
        ) "" else MyEnvironment.fieldKeyHead ) + pattern + MyEnvironment.fieldKeyTail

    }

    // findByDefinition  -- searches Definition field in all entries and returns list of matching entries
    // wrap pattern with FIELD_DEF
    private fun constrainToDefField(pattern: String): String {
        return ( MyEnvironment.fieldDefHead + pattern + MyEnvironment.fieldDefTail)
    }

    // findByUsage  -- searches Usage field in all entries and returns list of matching entries
    // wrap pattern with FIELD_USG_HEAD
    // and FIELD_USG_TAIL, unless pattern ends with "$"
    private fun constrainToUsageField(pattern: String): String {
        return (
                MyEnvironment.fieldUsgHead + pattern +
                ( if( pattern.last().equals(MyEnvironment.anchorTail) ) "" else MyEnvironment.fieldUsgTail )
        )
    }


}  // end class Dictionary