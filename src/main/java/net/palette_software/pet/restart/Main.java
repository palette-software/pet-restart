/*
The MIT License (MIT)
Copyright (c) 2016, Palette Software

Permission is hereby granted, free of charge, to any person obtaining a copy of this
software and associated documentation files (the "Software"), to deal in the Software
without restriction, including without limitation the rights to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be included in all copies
or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package net.palette_software.pet.restart;

import org.apache.commons.cli.*;

public class Main {

    public static void main(String[] args) {

        //if true, help will shown
        boolean need_help = true;

        //fast&dirty cli implementation
        CommandLineParser parser = new GnuParser();
        Options options = new Options();

        //create the command line options
        CliControl.createCommandLineOptions(options);

        try {

            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            //Turn simulation on if the simulation cli flag is set.
            CliControl.useCommandLineOptionSimulation(line);

            if (CliControl.isSimulation()) {
                HelperLogger.loggerStdOut.info("Running simulation.");
            }

            //check if tabsvc is running
            if (HelperWindowsTask.isTabsvcRunning()) {

                HelperLogger.loggerStdOut.info("tabsvc.exe not ruinning...");

            } else {

                //Turn force-restart on if the force cli flag is set.
                CliControl.useCommandLineOptionForce(line);

                //change tabsvc config dir if it has been set in command line.
                CliControl.useCommandLineOptionTabsvcConfigDir(line);

                //change force restart timeout if it has been set in command line.
                CliControl.useCommandLineOptionForceRestartTimeout(line);

                //change JMX polling timeout if it has been set in command line.
                CliControl.useCommandLineOptionJmxPollingTime(line);

                //change wait time between pet-restart operations if it has been set in command line.
                CliControl.useCommandLineOptionWait(line);

                //change wait time after something went wrong if it has been set in command line.
                CliControl.useCommandLineOptionWaitErrors(line);

                need_help = CliControl.runCliControlledTasks(line);
            }

            //Show CLI help if it is needed
            CliControl.showCommandLineHelp(need_help, options, line);

        } catch (Exception e) {
            //e.printStackTrace();
            HelperLogger.loggerStdOut.info(e.getMessage());
            HelperLogger.loggerFile.fatal("fatal:", e);
        }
    }

}
