package Fatashi
// Fatashi -- a dictionary search & display package

// global expressions for output
 fun printBlue(s: String) = println( AnsiColor.wrapBlue( s ) )
 fun printWarn(s: String) = println( AnsiColor.wrapGreen( s ) )
 fun printError(s: String) = println( AnsiColor.wrapRed( s ) )


// kotlin conventional starting point
fun main(args: Array<String>) {

    Version.printMyVersion( " starting..." )
    MyEnvironment.setup(args)   // initialize app environment
    FatashiWork.work()      // do the work of Fatashi

    println("...ending ${MyEnvironment.appName}")  // say good-bye to user
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

    // fatashi work loops through commands
    // arg: environment (future)

    fun work(  ) {
        var loop = true  // user input loop while true

        do {
            print("$APP_NAME > ")  // command prompt
            val cmdlist = readLine()?.split(' ') ?: listOf("exit")

                // parse command
            when ( val cmd = cmdlist.first().trim() ) {
                "x", "ex", "exit"       -> loop = false   // exit program
                "q", "quit"             -> loop = false  // exit program
                "t", "tft", "tafuta"    -> kamusi.searchKeyList( cmdlist.drop(1) )  // search dictionary
                "l", "list"       -> kamusi.listAll()   // list dictionary
                "f", "flags"      -> MyEnvironment.printOptions()  // list options
                "s", "sts", "stat", "status"       -> kamusi.printStatus()   // dict status
                "h", "help"      -> MyEnvironment.printInfo("  tafuta, list, sts, help, quit, exit")
                "v", "version"   -> Version.printMyVersion( " " )
                ""               -> true   // empty line; NOP
                else        -> MyEnvironment.printUsageError("$cmd is unrecognizable")
            }

        } while ( loop )
    }
}