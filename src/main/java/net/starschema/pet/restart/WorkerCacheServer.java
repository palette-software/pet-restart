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

import java.util.ArrayList;
import java.util.List;

import static net.starschema.pet.restart.HelperFile.filePregMatch;

class WorkerCacheServer extends AbstractWorker {

    //the name of the windows process of the Worker.
    private static final String WINDOWS_PROCESS_NAME = "redis-server.exe";

    WorkerCacheServer() {
    }

    //"C:/Program Files/Tableau/Tableau Server/10.0/redis/bin/redis-server.exe" "C:/ProgramData/Tableau/Tableau Server/data/tabsvc/config/redis.conf" --logfile "C:/ProgramData/Tableau/Tableau Server/data/tabsvc/logs/cacheserver/redis_0.log" --dir "C:/ProgramData/Tableau/Tableau Server/data/tabsvc/cacheserver" --heapdir "C:/ProgramData/Tableau/Tableau Server/data/tabsvc/temp" --dbfilename redisdb_0.rdb --port 6379

    // this worker is not killable via taskkill
    public List<Integer> getProcessId(boolean multiple) throws Exception {
        throw new Exception("This worker is not killable via taskkill");
    }

    public String getWindowsProcessName() {
        return WINDOWS_PROCESS_NAME;
    }

    public String toString() {
        return "cacheserver";
    }

    static String getCacheServerAuthPassword() throws Exception {

        if (!HelperFile.checkIfDir(CliControl.TABSVC_CONFIG_DIR)) {
            throw new Exception(CliControl.TABSVC_CONFIG_DIR + " is not a directory.");
        }

        return filePregMatch(CliControl.TABSVC_CONFIG_DIR + "//" + HelperFile.REDIS_CONFIG_FILENAME, "^requirepass (\\w+)$");

    }

    static List<Integer> getCacheServerports() throws Exception {

        if (!HelperFile.checkIfDir(CliControl.TABSVC_CONFIG_DIR)) {
            throw new Exception(CliControl.TABSVC_CONFIG_DIR + " is not a directory.");
        }

        String portline = filePregMatch(CliControl.TABSVC_CONFIG_DIR + "//" + HelperFile.WORKGROUP_YAML_FILENAME, "^cacheserver\\.hosts: ([,:\\w]+)$");

        List<Integer> ports = new ArrayList<>();

        for (String part : portline.split(",")) {
            String[] port = part.split(":");
            try {
                ports.add(Integer.parseInt(port[1]));
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                throw new Exception("Can not extract Cache Server port(s). ");
            }
        }
        return ports;
    }
}
