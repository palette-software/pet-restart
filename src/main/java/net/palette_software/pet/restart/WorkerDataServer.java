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

import java.util.List;

class WorkerDataServer extends AbstractWorker implements BalancerManagerManagedWorker {

    //Balancer Manager cluster name
    private static final String BALANCERMEMBER_NAME = "dataserver-cluster";

    //the name of the windows process of the Worker.
    private static final String WINDOWS_PROCESS_NAME = "dataserver.exe";

    //MBean object name in JMX
    private static final String M_BEAN_OBJECT_NAME = "tableau.health.jmx:name=dataserver";

    //Regex pattern string to find the pid and filter to the command line of the Worker in wmic
    private static final String SEARCH_PROCESS_REGEX = "^\"([^\"])*" + WINDOWS_PROCESS_NAME + "\".*\\s+([0-9]+)\\s*$";

    //Balancer Manager member name
    private String memberName;

    //Balancer Manager route
    private String route;

    //Balancer Manager nonce for the Worker's cluster
    private String nonce;

    //JMX port of the Worker
    private int jmxPort;

    WorkerDataServer(String memberName, String route, String nonce, int jmxPort) {
        this.memberName = memberName;
        this.route = route;
        this.nonce = nonce;
        this.jmxPort = jmxPort;
    }

    //getters for private propertiers
    public String getMBeanObjectName() {
        return M_BEAN_OBJECT_NAME;
    }
    public String getBalancerMemberName() {
        return BALANCERMEMBER_NAME;
    }
    public String getName() {
        return memberName;
    }
    public String getNonce() {
        return nonce;
    }
    public String getRoute() {
        return route;
    }
    public int getJmxPort() {
        return jmxPort;
    }
    public String getWindowsProcessName() {
        return WINDOWS_PROCESS_NAME;
    }
    public List<Integer> getProcessId(boolean multiple) throws Exception {
        return getProcessIdHelper(multiple, SEARCH_PROCESS_REGEX);
    }

    //wrapper to get Workers from Balancer Manager html source
    static List<BalancerManagerManagedWorker> getworkersFromHtml(String body) throws Exception {
        return HelperHttpClient.getWorkersFromHtml(body, BALANCERMEMBER_NAME, M_BEAN_OBJECT_NAME);
    }

    public String toString() {
        return "dataserver " + this.route + " " + this.memberName;
    }



}
