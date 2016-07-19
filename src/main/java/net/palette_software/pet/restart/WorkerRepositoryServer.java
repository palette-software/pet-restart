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

import static net.palette_software.pet.restart.HelperFile.filePregMatch;

class WorkerRepositoryServer extends AbstractWorker {

    //the name of the windows process of the Worker.
    private static final String WINDOWS_PROCESS_NAME = "pg_ctl.exe";

    WorkerRepositoryServer() {
    }

    // this worker is not killable via taskkill
    public List<Integer> getProcessId(boolean multiple) throws Exception {
        throw new Exception("This worker is not killable via taskkill");
    }

    public String getWindowsProcessName() {
        return WINDOWS_PROCESS_NAME;
    }

    public String toString() {
        return "repository";
    }

    static String getAppPath() throws Exception {

        if (!HelperFile.checkIfDir(CliControl.TABSVC_CONFIG_DIR)) {
            throw new Exception(CliControl.TABSVC_CONFIG_DIR + " is not a directory.");
        }

        return filePregMatch(CliControl.TABSVC_CONFIG_DIR + "//" + HelperFile.WORKGROUP_YAML_FILENAME, "^pgsql\\.pgctl: (.*)$");

    }

    static String getDataDir() throws Exception {

        if (!HelperFile.checkIfDir(CliControl.TABSVC_CONFIG_DIR)) {
            throw new Exception(CliControl.TABSVC_CONFIG_DIR + " is not a directory.");
        }

        return filePregMatch(CliControl.TABSVC_CONFIG_DIR + "//" + HelperFile.WORKGROUP_YAML_FILENAME, "^pgsql\\.data\\.dir: (.*)$");

    }
}
