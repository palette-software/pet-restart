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

class WorkerBackgrounder extends AbstractWorker {

    private static final String WINDOWS_PROCESS_NAME = "backgrounder.exe";
    private static final String SEARCH_PROCESS_REGEX = "^\"([^\"])*" + WINDOWS_PROCESS_NAME + "\".*\\s+([0-9]+)\\s*$";

    WorkerBackgrounder() {
    }

    //"C:/Program Files/Tableau/Tableau Server/10.0/bin/backgrounder.exe" -c tabsvc -XX:+UseConcMarkSweepGC -Xmx512m -Xms256m -Dcom.sun.management.jmxremote.port=8550 -Dcom.sun.management.jmxremote.rmi.port=8552 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false "-XX:ErrorFile=C:/ProgramData/Tableau/Tableau Server/data/tabsvc/logs/backgrounder/hs_err-0_pid%p.log" -Dlicensing.logFileName=backgrounder "-Djava.util.logging.config.file=C:/ProgramData/Tableau/Tableau Server/data/tabsvc/backgrounder/0/conf/logging.properties" -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager "-Djava.class.path=C:/Program Files/Tableau/Tableau Server/10.0/tomcat/bin/bootstrap.jar;C:/Program Files/Tableau/Tableau Server/10.0/tomcat/bin/tomcat-juli.jar" "-Djava.library.path=C:/Program Files/Tableau/Tableau Server/10.0/bin" "-Dcatalina.base=C:/ProgramData/Tableau/Tableau Server/data/tabsvc/backgrounder/0" "-Dcatalina.home=C:/Program Files/Tableau/Tableau Server/10.0/tomcat" "-Djava.io.tmpdir=C:/ProgramData/Tableau/Tableau Server/data/tabsvc/temp" "-Dconfig.properties=file:C:/ProgramData/Tableau/Tableau Server/data/tabsvc/config/backgrounder.properties" "-Dconnections.properties=file:C:/ProgramData/Tableau/Tableau Server/data/tabsvc/config/connections.properties" "-Dlog4j.configuration=file:C:/ProgramData/Tableau/Tableau Server/data/tabsvc/backgrounder/0/conf/log4j.xml" -Duser.timezone=UTC -Dprocid=0 -Djna.nosys=true org.apache.catalina.startup.Bootstrap start

    public String getWindowsProcessName() {
        return WINDOWS_PROCESS_NAME;
    }

    public String toString() {
        return "backgrounder";
    }

    public List<Integer> getProcessId(boolean multiple) throws Exception {
        return getProcessIdHelper(multiple, SEARCH_PROCESS_REGEX);
    }

}
