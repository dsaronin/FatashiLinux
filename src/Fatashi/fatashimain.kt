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

    printInfo("...ending ${MyEnvironment.appName}")  // say good-bye to user
}

object FatashiWork  {
        // instantiate kamusi
    private val kamusi = Kamusi(
                MyEnvironment.kamusiMainFile,
                MyEnvironment.fieldDelimsMain
        )

    init {
        // show that dictionary is viable & ready
        if ( MyEnvironment.verboseFlag ) kamusi.printStatus()
    }

    private val helpList = "  tafuta, methali, list, sts, options, help, quit, exit"
    // fatashi work loops through commands
    fun work(  ) {
        var loop = true  // user input loop while true

        do {
            printPrompt("${MyEnvironment.appName} > ")  // command prompt
            val cmdlist = readLine()?.split(' ') ?: listOf("exit")

                // parse command
            when ( val cmd = cmdlist.first().trim() ) {
                "x", "ex", "exit"       -> loop = false   // exit program
                "q", "quit"             -> loop = false  // exit program
                "t", "tft", "tafuta",
                "m", "methali"     -> kamusi.searchKeyList( cmdlist.drop(1) )  // search dictionary
                "l", "list"       -> kamusi.listAll()   // list dictionary
                "f", "flags"      -> MyEnvironment.printOptions()  // list options
                "s", "sts", "stat", "status"       -> kamusi.printStatus()   // dict status
                "h", "help"      -> MyEnvironment.printInfo(helpList)
                "v", "version"   -> Version.printMyVersion( " " )
                "o", "optioins"  -> MyEnvironment.printOptions()
                ""               -> loop = true   // empty line; NOP
                else        -> MyEnvironment.printUsageError("$cmd is unrecognizable")
            }

        } while ( loop )
    }
}