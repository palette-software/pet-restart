/*
The MIT License (MIT)
Copyright (c) 2016, Starschema Ltd

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

package net.starschema.pet.restart;

import java.util.List;

final class CliControl {

    //url to tableau's local balance-manager
    static final String BALANCER_MANAGER_URL = "http://localhost/balancer-manager";

    //task kill executable command
    static final String TASK_KILLER = "taskkill /F /PID";

    //the default time until forcing restart on jmxable worker.
    static int FORCE_SHUTDOWN = 240;

    //the deafualt time between checking a jmxable worker's active session numbers
    static int JMX_POLLING_TIME = 60;

    //if true pet-restart won't do graceful restarts but simply kill the Workers.
    static boolean FORCE_RESTARTS = false;

    //the default time after worker restarts
    static int WAIT_AFTER = 30;

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
            Main.loggerStdOut.info(w.toString());
        }

        for (BalancerManagerManagedWorker w : workers) {

            if (w.getJmxPort() != -1) {
                Main.loggerStdOut.info("Gracefully restarting worker " + w.getRoute());
            } else {
                Main.loggerStdOut.info("Restarting worker " + w.getRoute());
            }
            Main.loggerStdOut.info("Switching worker to Draining mode");

            //set worker fraining mode in apache
            ControllerWorker.drain(w);

            //if worker is jmxable, will check presence of jmx and the necessary mbean.
            if (w.getJmxPort() != -1) {

                try (
                        HelperJmxClient jmxClient = new HelperJmxClient()
                ) {
                    Main.loggerStdOut.info("Connecting to JMX endpoint jmx://localhost:" + w.getJmxPort());

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
                                Main.loggerStdOut.info("Force restart.");
                            } else {
                                if (0 > activeSessions) {
                                    Main.loggerStdOut.info("Inconclusive data from MBean : ActiveSessions = " + activeSessions + ". Force restart.");
                                } else {
                                    Main.loggerStdOut.info("No active sessions.");
                                }
                            }

                            int pid = w.getProcessId(false).get(0);
                            Main.loggerStdOut.info("Sending stop signal to process " + pid);
                            ControllerWorker.kill(w);
                            CliControl.sleep(CliControl.WAIT_AFTER);

                            Main.loggerStdOut.info("Switch worker to Non-disabled mode");
                            ControllerWorker.reset(w);

                            done = true;
                        } else {

                            //if there are active sessions, will wait and check again.
                            Main.loggerStdOut.info("Number of active sessions " + activeSessions + ". Sleeping " + JMX_POLLING_TIME + " secs ");
                            CliControl.sleep(JMX_POLLING_TIME);
                            elapsedSeconds += JMX_POLLING_TIME;
                        }
                    }
                    Main.loggerStdOut.info("Graceful restart complete");
                }

                //non-jmxable worker will be restarted non-gracefully.
            } else {
                int pid = w.getProcessId(false).get(0);
                Main.loggerStdOut.info("Sending stop signal to process " + pid);
                ControllerWorker.kill(w);
                CliControl.sleep(CliControl.WAIT_AFTER);

                Main.loggerStdOut.info("Switch worker to Non-disabled mode");
                ControllerWorker.reset(w);
                Main.loggerStdOut.info("Restart complete");
            }
        }
    }

    //restarts the Vizql Workers.
    static void restartVizqlWorkers() throws Exception {



        Main.loggerStdOut.info("Locating vizqlserver-cluster workers from balancer-manager");

        //get the html source of balancer-manager
        String body = HttpClientHelper.getPage(BALANCER_MANAGER_URL);

        //get the Workers from the source of balancer-manager
        List<BalancerManagerManagedWorker> workers = WorkerVizql.getworkersFromHtml(body);

        //restrart the worker(s)
        restartBalancerManagerManagedWorkers(workers);
    }

    //restarts Dataserver Workers
    static void restartDataServerWorkers() throws Exception {


        Main.loggerStdOut.info("Locating dataserever-cluster workers from balancer-manager");

        //get the html source of balancer-manager
        String body = HttpClientHelper.getPage(BALANCER_MANAGER_URL);

        //get the Workers from the source of balancer-manager
        List<BalancerManagerManagedWorker> workers = WorkerDataServer.getworkersFromHtml(body);

        //restrart the worker(s)
        restartBalancerManagerManagedWorkers(workers);

    }

    //restarts Vizportal Workers
    static void restartVizportalWorkers() throws Exception {


        Main.loggerStdOut.info("Locating local-vizportal workers from balancer-manager");

        //get the html source of balancer-manager
        String body = HttpClientHelper.getPage(BALANCER_MANAGER_URL);

        //get the Workers from the source of balancer-manager
        List<BalancerManagerManagedWorker> workers = WorkerVizportal.getworkersFromHtml(body);

        //restrart the worker(s)
        restartBalancerManagerManagedWorkers(workers);
    }


    static void restartGateway() throws Exception {

        Main.loggerStdOut.info("Restarting Gateway");

        //kill the Gateway process
        ControllerWorker.kill(new WorkerGateway());

        CliControl.sleep(CliControl.WAIT_AFTER);

    }

    static void restartRepository() throws Exception {

        Main.loggerStdOut.info("Restarting Repository");

        //restart Postgre Server from cli
        ControllerWorker.RestartPostgreServer(WorkerRepositoryServer.getAppPath(), WorkerRepositoryServer.getDataDir());

        CliControl.sleep(CliControl.WAIT_AFTER);
    }

    static void restartBackgrounderWorkers() throws Exception {

        Main.loggerStdOut.info("Restarting Backgrounder(s)");

        //kill all Backgrounder processes
        ControllerWorker.killAll(new WorkerBackgrounder());

        CliControl.sleep(CliControl.WAIT_AFTER);
    }

    static void restartCacheServerWorkers() throws Exception {

        Main.loggerStdOut.info("Restarting Cache Server(s)");

        //get Redis AUTH from config file
        String pw = WorkerCacheServer.getCacheServerAuthPassword();

        List<Integer> ports = WorkerCacheServer.getCacheServerports();
        Main.loggerStdOut.info("There " + (ports.size() > 1 ? "are" : "is") + " " + ports.size() + " port" + (ports.size() > 1 ? "s" : ""));
        for (int port : ports) {
            Main.loggerStdOut.info("Restarting Cache server at port " + port);
            ControllerWorker.restartCacheServer(pw, port);
            CliControl.sleep(CliControl.WAIT_AFTER);
        }
    }
}