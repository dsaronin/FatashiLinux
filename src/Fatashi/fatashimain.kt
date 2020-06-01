package Fatashi
// Fatashi -- a dictionary search & display package

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
        var cmd: String
        var loop = true

        do {
            print("$APP_NAME > ")  // command prompt
            cmd = readLine() ?: "exit"   // accept a command

                // parse command
            when ( cmd.trim() ) {
                "x", "ex", "exit"      -> loop = false   // exit program
                "q", "quit"      -> loop = false  // exit program
                "t", "tft", "tafuta"    -> kamusi.findByEntry( cmd )  // search dictionary
                "l", "list"      -> kamusi.listAll()   // list dictionary
                "f", "flags"      -> MyEnvironment.printOptions()  // list options
                "s", "sts", "stat", "status"       -> kamusi.printStatus()   // dict status
                "h", "help"      -> MyEnvironment.printInfo("  tafuta, list, sts, help, quit, exit")
                "v", "version"   -> Version.printMyVersion( " " )
                else        -> MyEnvironment.printUsageError("$cmd is unrecognizable")
            }

        } while ( loop )
    }
}