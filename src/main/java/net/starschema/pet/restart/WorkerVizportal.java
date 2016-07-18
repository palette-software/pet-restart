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

class WorkerVizportal extends AbstractWorker implements BalancerManagerManagedWorker {

    //Balancer Manager cluster name
    private static final String BALANCERMEMBER_NAME = "local-vizportal";

    //the name of the windows process of the Worker.
    private static final String WINDOWS_PROCESS_NAME = "vizportal.exe";

    //MBean object name in JMX. Because there is no getperformanceMetrics in the MBean,
    // set it to "" to skip the JMX part.
    private static final String M_BEAN_OBJECT_NAME = "";

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

    WorkerVizportal(String memberName, String route, String nonce, int jmxPort) {
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
    public String getWindowsProcessName() {
        return WINDOWS_PROCESS_NAME;
    }
    public int getJmxPort() {
        return jmxPort;
    }
    public List<Integer> getProcessId(boolean multiple) throws Exception {
        return getProcessIdHelper(multiple, SEARCH_PROCESS_REGEX);
    }

    //single instance only, there is no route
    public String getRoute() {return ""; }

    //wrapper to get Workers from Balancer Manager html source
    static List<BalancerManagerManagedWorker> getworkersFromHtml(String body) throws Exception {
        return HttpClientHelper.getWorkersFromHtml(body, BALANCERMEMBER_NAME, M_BEAN_OBJECT_NAME);
    }

    public String toString() {
        return "vizqlserver " + this.route + " " + this.memberName;
    }
}
