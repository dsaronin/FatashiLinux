package Fatashi

// Fatashi -- a dictionary search & display package
    private const val DEBUG  = false

// global expressions for output
 fun printInfo(s: String)   = println( AnsiColor.wrapCyan( s ) )
 fun printPrompt(s: String) = print( AnsiColor.wrapYellow( s ) )
 fun printVisual(s: String) = print( AnsiColor.wrapGreen( s ) )
 fun printWarn(s: String)   = println( AnsiColor.wrapGreenBold( s ) )
 fun printError(s: String)  = println( AnsiColor.wrapRedBold( s ) )


// kotlin conventional starting point
fun main(args: Array<String>) {

    Version.printMyVersion( " starting..." )
    MyEnvironment.setup(args)   // initialize app environment

    FatashiWork.work()      // do the work of Fatashi

    printInfo("...ending ${MyEnvironment.myProps.appName}")  // say good-bye to user
}




object FatashiWork  {

    private val helpList = "  tafuta, methali, list, browse, sts, options, help, quit, exit"

    // fatashi work loops through commands
    fun work(  ) {
        var loop = true  // user input loop while true
        var useKamusi: Kamusi?
        var dropCount: Int

        do {
            printPrompt("${MyEnvironment.myProps.appName} > ")  // command prompt
            val cmdlist = readLine()?.trim()?.split(' ') ?: listOf("exit")

            useKamusi = null    // assume this isn't a search command
            dropCount = 1       // assume need to drop cmd from list head

                // parse command
            when ( val cmd = cmdlist.first().trim() ) {
                "x", "ex", "exit"       -> loop = false   // exit program
                "q", "quit"             -> loop = false  // exit program

                // search dictionary OR methali
                "tafuta"                -> useKamusi = selectKamusi(1)
                "t","tt","ttt","tttt"   -> useKamusi = selectKamusi( cmd.length )

                "methali"               -> useKamusi = selectMethali(1)
                "m","mm","mmm","mmmm"   -> useKamusi = selectMethali( cmd.length )

                "ml","mll","mlll","mllll"  -> selectMethali( cmd.length-1 )?.listRandom()
                "ms"                       -> selectMethali(1)?.printStatus()   // dict status

                // list dictionary
                "list"                  -> selectKamusi(1)?.listRandom()
                "l","ll","lll","llll"   -> selectKamusi( cmd.length )?.listRandom()

                // browse from an item
                "b", "browse"  -> selectKamusi(1)?.browsePage( cmdlist.drop(dropCount) )

                "s", "sts", "status" -> MyEnvironment.kamusiHead?.printStatus()   // dict status

                "f", "flags"     -> MyEnvironment.printOptions()  // list options
                "h", "help"      -> MyEnvironment.printInfo(helpList)
                "v", "version"   -> Version.printMyVersion( " " )
                "o", "options"   -> MyEnvironment.printOptions()

                ""               -> loop = true   // empty line; NOP
                else        -> {  // treat it as tafuta lookup request
                    useKamusi = MyEnvironment.kamusiHead
                    dropCount = 0  // don't strip off a command
                    if (DEBUG) MyEnvironment.printUsageError("$cmd is unrecognizable")
                }
            }

                // useKamusi will be non-null if a search command was encountered
            useKamusi?.searchKeyList( cmdlist.drop(dropCount) )   // do the search on key item list

        } while ( loop )
    } // fun work

    // selectKamusi  -- jump to a specific kamusi chain level
    private fun selectKamusi( n: Int ) : Kamusi? {
        var level = n
        var kamusi =
            if (MyEnvironment.myProps.prodFlag) MyEnvironment.kamusiHead else MyEnvironment.testHead

        // loop thru looking at deeper levels as long as available
        while (level > 1 && kamusi?.nextKamusi != null ) {
            kamusi = kamusi?.nextKamusi
            level--
        }
        return kamusi
    }

    // selectMethali  -- jump to a specific methali chain level
    private fun selectMethali( n: Int ) : Kamusi? {
        var level = n
        var kamusi = MyEnvironment.methaliHead

        // loop thru looking at deeper levels as long as available
        while (level > 1 && kamusi?.nextKamusi != null ) {
            kamusi = kamusi?.nextKamusi
            level--
        }
        return kamusi
    }

}  // object FatashiWork