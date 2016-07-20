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

import java.util.List;

final class CliControl {

    //version number
    private static final String VERSION = "1.0";

    //Simaulate all the kills/shutdowns/restarts
    private static int SIMULATION = 0;

    //url to tableau's local balance-manager
    static final String BALANCER_MANAGER_URL = "http://localhost/balancer-manager";

    //task kill executable command
    static final String TASK_KILLER = "taskkill /F /PID";

    //the default time until forcing restart on jmxable worker.
    private static int FORCE_SHUTDOWN = 240;

    //the deafualt time between checking a jmxable worker's active session numbers
    private static int JMX_POLLING_TIME = 60;

    //if true pet-restart won't do graceful restarts but simply kill the Workers.
    static boolean FORCE_RESTARTS = false;

    //the default time after worker restarts
    private static int WAIT_AFTER = 30;

    //the default time until retrying after something went wrong
    static int WAIT_AFTER_ERROR = 60;

    //tabsvc config
    static String TABSVC_CONFIG_DIR = "c:\\ProgramData\\Tableau\\Tableau Server\\data\\tabsvc\\config";

    private CliControl() {
    }

    //sleeps for given seconds
    static void sleep(int secs) throws Exception {
        Thread.sleep(1000 * secs);
    }

    //helper, restarts balancer manager managed Workers
    private static void restartBalancerManagerManagedWorkers(List<BalancerManagerManagedWorker> workers) throws Exception {

        //print the Workers' name
        for (Worker w : workers) {
            HelperLogger.loggerStdOut.info(w.toString());
        }

        for (BalancerManagerManagedWorker w : workers) {

            if (w.getJmxPort() != -1) {
                HelperLogger.loggerStdOut.info("Gracefully restarting worker " + w.getRoute());
            } else {
                HelperLogger.loggerStdOut.info("Restarting worker " + w.getRoute());
            }
            HelperLogger.loggerStdOut.info("Switching worker to Draining mode");

            //set worker fraining mode in apache
            ControllerWorker.drain(w, SIMULATION);

            //if worker is jmxable, will check presence of jmx and the necessary mbean.
            if (w.getJmxPort() != -1) {

                try (
                        HelperJmxClient jmxClient = new HelperJmxClient()
                ) {
                    HelperLogger.loggerStdOut.info("Connecting to JMX endpoint jmx://localhost:" + w.getJmxPort());

                    jmxClient.connectService("service:jmx:rmi:///jndi/rmi://:" + w.getJmxPort() + "/jmxrmi");

                    int activeSessions;
                    int elapsedSeconds = 0;
                    boolean done = false;

                    //will try until the worker is restarted.
                    while (!done) {

                        //get number of active session(s) in jmx.
                        activeSessions = Integer.parseInt(jmxClient.getActiveSessions(w.getMBeanObjectName()));

                        //in case of inconclusive results or after the given time to check for active sessions,
                        // or if there is no active sessiojn, a restart will occur.
                        if (elapsedSeconds >= FORCE_SHUTDOWN || 0 >= activeSessions) {

                            if (elapsedSeconds >= FORCE_SHUTDOWN) {
                                HelperLogger.loggerStdOut.info("Force restart.");
                            } else {
                                if (0 > activeSessions) {
                                    HelperLogger.loggerStdOut.info("Inconclusive data from MBean : ActiveSessions = " + activeSessions + ". Force restart.");
                                } else {
                                    HelperLogger.loggerStdOut.info("No active sessions.");
                                }
                            }

                            int pid = w.getProcessId(false).get(0);
                            HelperLogger.loggerStdOut.info("Sending stop signal to process " + pid);
                            ControllerWorker.kill(w, SIMULATION);
                            CliControl.sleep(CliControl.WAIT_AFTER);

                            HelperLogger.loggerStdOut.info("Switch worker to Non-disabled mode");
                            ControllerWorker.reset(w, SIMULATION);

                            done = true;
                        } else {

                            //if there are active sessions, will wait and check again.
                            HelperLogger.loggerStdOut.info("Number of active sessions " + activeSessions + ". Sleeping " + JMX_POLLING_TIME + " secs ");
                            CliControl.sleep(JMX_POLLING_TIME);
                            elapsedSeconds += JMX_POLLING_TIME;
                        }
                    }
                    HelperLogger.loggerStdOut.info("Graceful restart complete");
                }

                //non-jmxable worker will be restarted non-gracefully.
            } else {
                int pid = w.getProcessId(false).get(0);
                HelperLogger.loggerStdOut.info("Sending stop signal to process " + pid);
                ControllerWorker.kill(w, SIMULATION);
                CliControl.sleep(CliControl.WAIT_AFTER);

                HelperLogger.loggerStdOut.info("Switch worker to Non-disabled mode");
                ControllerWorker.reset(w, SIMULATION);
                HelperLogger.loggerStdOut.info("Restart complete");
            }
        }
    }

    //restarts the Vizql Workers.
    private static void restartVizqlWorkers() throws Exception {



        HelperLogger.loggerStdOut.info("Locating vizqlserver-cluster workers from balancer-manager");

        //get the html source of balancer-manager
        String body = HelperHttpClient.getPage(BALANCER_MANAGER_URL);

        //get the Workers from the source of balancer-manager
        List<BalancerManagerManagedWorker> workers = WorkerVizql.getworkersFromHtml(body);

        //restrart the worker(s)
        restartBalancerManagerManagedWorkers(workers);
    }

    //restarts Dataserver Workers
    private static void restartDataServerWorkers() throws Exception {


        HelperLogger.loggerStdOut.info("Locating dataserever-cluster workers from balancer-manager");

        //get the html source of balancer-manager
        String body = HelperHttpClient.getPage(BALANCER_MANAGER_URL);

        //get the Workers from the source of balancer-manager
        List<BalancerManagerManagedWorker> workers = WorkerDataServer.getworkersFromHtml(body);

        //restrart the worker(s)
        restartBalancerManagerManagedWorkers(workers);

    }

    //restarts Vizportal Workers
    private static void restartVizportalWorkers() throws Exception {


        HelperLogger.loggerStdOut.info("Locating local-vizportal workers from balancer-manager");

        //get the html source of balancer-manager
        String body = HelperHttpClient.getPage(BALANCER_MANAGER_URL);

        //get the Workers from the source of balancer-manager
        List<BalancerManagerManagedWorker> workers = WorkerVizportal.getworkersFromHtml(body);

        //restrart the worker(s)
        restartBalancerManagerManagedWorkers(workers);
    }


    private static void restartGateway() throws Exception {

        HelperLogger.loggerStdOut.info("Restarting Gateway");

        //kill the Gateway process
        ControllerWorker.kill(new WorkerGateway(), SIMULATION);

        CliControl.sleep(CliControl.WAIT_AFTER);

    }

    private static void restartRepository() throws Exception {

        HelperLogger.loggerStdOut.info("Restarting Repository");

        //restart Postgre Server from cli
        ControllerWorker.RestartPostgreServer(WorkerRepositoryServer.getAppPath(), WorkerRepositoryServer.getDataDir(), SIMULATION);

        CliControl.sleep(CliControl.WAIT_AFTER);
    }

    private static void restartBackgrounderWorkers() throws Exception {

        HelperLogger.loggerStdOut.info("Restarting Backgrounder(s)");

        //kill all Backgrounder processes
        ControllerWorker.killAll(new WorkerBackgrounder(), SIMULATION);

        CliControl.sleep(CliControl.WAIT_AFTER);
    }

    private static void restartCacheServerWorkers() throws Exception {

        HelperLogger.loggerStdOut.info("Restarting Cache Server(s)");

        //get Redis AUTH from config file
        String pw = WorkerCacheServer.getCacheServerAuthPassword();

        List<Integer> ports = WorkerCacheServer.getCacheServerports();
        HelperLogger.loggerStdOut.info("There " + (ports.size() > 1 ? "are" : "is") + " " + ports.size() + " port" + (ports.size() > 1 ? "s" : ""));
        for (int port : ports) {
            HelperLogger.loggerStdOut.info("Restarting Cache server at port " + port);
            ControllerWorker.restartCacheServer(pw, port, SIMULATION);
            CliControl.sleep(CliControl.WAIT_AFTER);
        }
    }

    //turn simulation on if the simulation cli flag is set.
    static void useCommandLineOptionSimulation(CommandLine line) {
        if (line.hasOption("simulation")) {
            SIMULATION = 1;
        }
    }

    //return true is pet-restart in simulation mode.
    static boolean isSimulation() {
        return SIMULATION > 0;
    }

    //Turn force-restart on if the force cli flag is set.
    static void useCommandLineOptionForce(CommandLine line) {
        if (line.hasOption("force")) {
            FORCE_RESTARTS = true;
        }
    }

    //change tabsvc config dir if it has been set in command line.
    static void useCommandLineOptionTabsvcConfigDir(CommandLine line) throws Exception {
        if (line.hasOption("tabsvc-config-dir")) {
            String tabsvc_config_dir = line.getOptionValue("tabsvc-config-dir");

            if (!HelperFile.checkIfDir(tabsvc_config_dir)) {
                throw new Exception("tabsvc-config-dir must be a valid directory");
            }
            TABSVC_CONFIG_DIR = tabsvc_config_dir;
        }
    }

    //change force restart timeout if it has been set in command line.
    static void useCommandLineOptionForceRestartTimeout(CommandLine line) throws Exception {
        int clicontrol_force_shutdown;
        if (line.hasOption("force-restart-timeout")) {
            try {
                clicontrol_force_shutdown = Integer.parseInt(line.getOptionValue("force-restart-timeout"));
            } catch (Exception e) {
                throw new Exception("force-restart-timeout must be a number");
            }
            if (clicontrol_force_shutdown < 1) {
                throw new Exception("force-restart-timeout must be a positive number");
            }
            FORCE_SHUTDOWN = clicontrol_force_shutdown;
        }
    }

    //change JMX polling timeout if it has been set in command line.
    static void useCommandLineOptionJmxPollingTime(CommandLine line) throws Exception {
        int jmxclienthelper_jmx_polling_time;
        if (line.hasOption("jmx-polling-time")) {
            try {
                jmxclienthelper_jmx_polling_time = Integer.parseInt(line.getOptionValue("jmx-polling-time"));
            } catch (Exception e) {
                throw new Exception("jmx-polling-time must be a number");
            }
            if (jmxclienthelper_jmx_polling_time < 1) {
                throw new Exception("jmx-polling-time be a positive number");
            }
            if (FORCE_SHUTDOWN < jmxclienthelper_jmx_polling_time) {
                throw new Exception("force-restart-timeout must be at least jmx-polling-time");
            }
            JMX_POLLING_TIME = jmxclienthelper_jmx_polling_time;
        }
    }

    //change wait time between pet-restart operations if it has been set in command line.
    static void useCommandLineOptionWait(CommandLine line) throws Exception {
        if (line.hasOption("wait")) {
            int clicontrol_wait;
            try {
                clicontrol_wait = Integer.parseInt(line.getOptionValue("wait"));
            } catch (Exception e) {
                throw new Exception("wait must be a number");
            }
            if (clicontrol_wait < 1) {
                throw new Exception("wait must be a positive number");
            }
            WAIT_AFTER = clicontrol_wait;
        }
    }

    //change wait time after something went wrong if it has been set in command line.
    static void useCommandLineOptionWaitErrors(CommandLine line) throws Exception {
        if (line.hasOption("wait-errors")) {
            int clicontrol_wait_errors;
            try {
                clicontrol_wait_errors = Integer.parseInt(line.getOptionValue("wait-errors"));
            } catch (Exception e) {
                throw new Exception("wait-errors must be a number");
            }
            if (clicontrol_wait_errors < 1) {
                throw new Exception("wait-errors must be a positive number");
            }

            WAIT_AFTER_ERROR = clicontrol_wait_errors;
        }
    }


    /**
     * Run tasks if their flags are set.
     * @return the need to show help. Only if none of these tasks will run, it will be true.
     */
    static boolean runCliControlledTasks(CommandLine line) throws Exception {
        boolean need_help=true;
        if (line.hasOption("restart") || line.hasOption("reload-postgres")) {
            need_help = false;
            restartRepository();
        }

        if (line.hasOption("restart") || line.hasOption("restart-cache")) {
            need_help = false;
            restartCacheServerWorkers();
        }

        if (line.hasOption("restart") || line.hasOption("restart-vizportal")) {
            need_help = false;
            restartVizportalWorkers();
        }

        if (line.hasOption("restart") || line.hasOption("restart-vizql")) {
            need_help = false;
            restartVizqlWorkers();
        }

        if (line.hasOption("restart") || line.hasOption("restart-dataserver")) {
            need_help = false;
            restartDataServerWorkers();
        }

        if (line.hasOption("restart") || line.hasOption("restart-backgrounder")) {
            need_help = false;
            restartBackgrounderWorkers();
        }

        if (line.hasOption("restart") || line.hasOption("reload-apache")) {
            need_help = false;
            restartGateway();
        }

        if (line.hasOption("version")) {
            need_help = false;
            HelperLogger.loggerStdOut.info("Version: " + VERSION);
        }
        return need_help;
    }

    //create the command line options
    static void createCommandLineOptions(Options options) {
        options.addOption("h", "help", false, "This help.");
        options.addOption("v", "version", false, "Print version information.");
        options.addOption("r", "restart", false, "Restart all processes one-by-one.");
        options.addOption("rv", "restart-vizql", false, "Restart VizQL workers.");
        options.addOption("rc", "restart-cache", false, "Restart Cache Server.");
        options.addOption("s", "simulation", false, "Simulate all the restarts.");

        options.addOption("rb", "restart-backgrounder", false, "Restart Backgrounder workers.");
        options.addOption("rp", "restart-vizportal", false, "Restart Vizportal workers.");
        options.addOption("rd", "restart-dataserver", false, "Restart Data Server workers.");
        options.addOption("pg", "reload-postgres", false, "Send reload signal to repository.");
        options.addOption("ra", "reload-apache", false, "Reload gateway rules.");
        options.addOption("f", "force", false, "Disable JMX, send signals immediately (non-graceful).");

        options.addOption(OptionBuilder.withLongOpt("jmx-polling-time")
                .withDescription("JMX data polling time")
                .hasArg()
                .withArgName("SECONDS")
                .create());

        options.addOption(OptionBuilder.withLongOpt("wait")
                .withDescription("Waiting time between jobs")
                .hasArg()
                .withArgName("SECONDS")
                .create());

        options.addOption(OptionBuilder.withLongOpt("wait-errors")
                .withDescription("Waiting time after errors/retries")
                .hasArg()
                .withArgName("SECONDS")
                .create());

        options.addOption(OptionBuilder.withLongOpt("force-restart-timeout")
                .withDescription("Force restart timeout")
                .hasArg()
                .withArgName("SECONDS")
                .create());

        options.addOption(OptionBuilder.withLongOpt("tabsvc-config-dir")
                .withDescription("Path to tabsvc configs")
                .hasArg()
                .withArgName("PATH")
                .create());
    }

    /**
     * Show CLI help if it is needed
     * @param need_help if it is true, help will shown, because no task was selected.
     * @param options pet-restart cli options
     * @param line pet-restart cli command line
     */
    static void showCommandLineHelp(boolean need_help, Options options, CommandLine line) {
        if (need_help || line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("pet", options);
        }
    }
}