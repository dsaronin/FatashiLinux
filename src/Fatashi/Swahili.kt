package Fatashi

// Swahili  -- control everything language-specific

object Swahili {

    // preProcessKey  -- massage typical swahili ambiguities for dictionary lookup
    // note that English-words should be escaped with a ";" as the first symbol
    // thus preventing any swahili substitutions
   fun preProcessKey(key: String): String {
       return key.replace("^ma".toRegex(), "\\\\b(ma)?")  // [li-ya] sing/plurals
                 .replace("^mi".toRegex(), "\\\\bmi?")    //  [u-i] sing/plurals
                 .replace("^vi".toRegex(), "\\\\b[kv]i") //  [ki-vi] sing/plurals
                 .replace("^-ji".toRegex(), "-(ji)?") //  [ki-vi] sing/plurals

   }

   fun postProcessKey(key: String): String {
       return key.replace("l|r".toRegex(), "[lr]")
               .replace("z".toRegex(), "(z|dh)")
               .replace("^mu".toRegex(), "m[uw]")
   }
}