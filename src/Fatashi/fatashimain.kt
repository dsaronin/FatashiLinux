package Fatashi
// Fatashi -- a dictionary search & display package

// kotlin conventional starting point
fun main(args: Array<String>) {
    MyEnvironment.setup(args)   // initialize app environment

    Version.printMyVersion( " starting..." )
    FatashiWork.work()
    println("...ending $APP_NAME")
}

object FatashiWork  {
        // instantiate kamusi
    private val kamusi = Kamusi(KAMUSI_FILE, FIELD_DELIMITERS)

    init {
        // show that dictionary is viable & ready
        kamusi.printStatus()
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
            when (cmd) {
                "exit"      -> loop = false   // exit program
                "quit"      -> loop = false  // exit program
                "tafuta"    -> kamusi.findByEntry( cmd )  // search dictionary
                "list"      -> kamusi.listAll()   // list dictionary
                "sts"       -> kamusi.printStatus()   // dict status
                "help"      -> println("tafuta, list, sts, help, quit, exit")
                else        -> println("$cmd is unrecognizable; try again!")
            }

        } while ( loop )
    }
}