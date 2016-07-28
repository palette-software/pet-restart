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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

class HelperInstaller {

    private final static String HTTPD_CONF_BALANCER_MANAGER_DISABLED_PATTERN =
            "<Location /balancer-manager>\\s+" +
                    "SetHandler balancer-manager\\s+" +
                    "Require host 127\\.0\\.0\\.1\\s+" +
                    "</Location>";

    private final static String HTTPD_CONF_BALANCER_MANAGER_ENABLED =
            "<Location /balancer-manager>\n" +
                    "SetHandler balancer-manager\n" +
                    "<RequireAny>\n" +
                    "Require ip ::1\n" +
                    "Require ip 127.0.0.1\n" +
                    "</RequireAny>\n" +
                    "</Location>";

    static private String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    static boolean checkIfBalancerManagerDisabled() throws Exception {

        try {
            String test = readFile(CliControl.getTableauTemplatesDir() + "\\httpd.conf.templ", StandardCharsets.UTF_8);
            Pattern p = Pattern.compile(HTTPD_CONF_BALANCER_MANAGER_DISABLED_PATTERN);
            return p.matcher(test).find();
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    static void EnableBalancerManager() throws Exception {
        try {
            String subject = readFile(CliControl.getTableauTemplatesDir() + "\\httpd.conf.templ", StandardCharsets.UTF_8);
            BufferedWriter out = new BufferedWriter(new FileWriter(CliControl.getTableauTemplatesDir() + "\\httpd.conf.templ"));
            out.write(subject.replaceAll(HTTPD_CONF_BALANCER_MANAGER_DISABLED_PATTERN, HTTPD_CONF_BALANCER_MANAGER_ENABLED));
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new Exception(e);
        }
    }
}
