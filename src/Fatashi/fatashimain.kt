package Fatashi

// Fatashi -- a dictionary search & display package

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

    private val helpList = "  tafuta, methali, list, sts, options, help, quit, exit"
    // fatashi work loops through commands
    fun work(  ) {
        var loop = true  // user input loop while true
        var useKamusi: Kamusi?

        do {
            printPrompt("${MyEnvironment.myProps.appName} > ")  // command prompt
            val cmdlist = readLine()?.trim()?.split(' ') ?: listOf("exit")

            useKamusi = null    // assume this isn't a search command

                // parse command
            when ( val cmd = cmdlist.first().trim() ) {
                "x", "ex", "exit"       -> loop = false   // exit program
                "q", "quit"             -> loop = false  // exit program
                // search dictionary OR methali
                "t", "ta", "tafuta"    ->
                    useKamusi = if (MyEnvironment.myProps.prodFlag)
                                     MyEnvironment.kamusiHead
                                else MyEnvironment.testHead
                "m", "ma", "methali"   -> useKamusi = MyEnvironment.methaliHead

                "ml"                 -> MyEnvironment.methaliHead?.listAll()
                "ms"                 -> MyEnvironment.methaliHead?.printStatus()   // dict status
                "l", "list"          -> MyEnvironment.kamusiHead?.listAll()   // list dictionary
                "s", "sts", "status" -> MyEnvironment.kamusiHead?.printStatus()   // dict status

                "f", "flags"     -> MyEnvironment.printOptions()  // list options
                "h", "help"      -> MyEnvironment.printInfo(helpList)
                "v", "version"   -> Version.printMyVersion( " " )
                "o", "options"   -> MyEnvironment.printOptions()

                ""               -> loop = true   // empty line; NOP
                else        -> MyEnvironment.printUsageError("$cmd is unrecognizable")
            }

                // useKamusi will be non-null if a search command was encountered
            useKamusi?.searchKeyList( cmdlist.drop(1) )   // do the search on key item list

        } while ( loop )
    }
}