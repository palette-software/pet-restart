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

class WorkerGateway extends AbstractWorker {

    //the name of the windows process of the Worker.
    private static final String WINDOWS_PROCESS_NAME = "httpd.exe";

    //Regex pattern string to find the pid and filter to the command line of the Worker in wmic
    private static final String SEARCH_PROCESS_REGEX = "^\"([^\"])*" + WINDOWS_PROCESS_NAME + "\" -E.*\\s+([0-9]+)\\s*$";

    WorkerGateway() {
    }

    // "C:/Program Files/Tableau/Tableau Server/10.0/apache/bin/httpd.exe" -E "C:/ProgramData/Tableau/Tableau Server/data/tabsvc/logs/httpd/startup.log" -f "C:/ProgramData/Tableau/Tableau Server/data/tabsvc/config/httpd.conf"

    //"C:\Program Files\Tableau\Tableau Server\10.0\apache\bin\httpd.exe" -d "C:/Program Files/Tableau/Tableau Server/10.0/apache" -E "C:/ProgramData/Tableau/Tableau Server/data/tabsvc/logs/httpd/startup.log" -f "C:/ProgramData/Tableau/Tableau Server/data/tabsvc/config/httpd.conf"


    public List<Integer> getProcessId(boolean multiple) throws Exception {
        return getProcessIdHelper(multiple, SEARCH_PROCESS_REGEX);
    }

    public String getWindowsProcessName() {
        return WINDOWS_PROCESS_NAME;
    }

    public String toString() {
        return "httpd";
    }

}
