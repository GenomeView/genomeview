/**
 * Copyright (c) 2005-2016, Thomas Abeel
 * 
 * This file is part of the Abeel Java Toolkit (AJT).
 * the Abeel Java Toolkit (AJT) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Abeel Java Toolkit (AJT) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Abeel Java Toolkit (AJT).  If not, see http://www.gnu.org/licenses/.
 */
package be.abeel.jargs;

import jargs.gnu.CmdLineParser;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Command line parser with basic output for a -h/--help option. This option is
 * automatically available at construction time.
 * 
 * @author Thomas Abeel
 */
public class AutoHelpCmdLineParser extends CmdLineParser {
    List<String> optionHelpStrings = new ArrayList<String>();

    public Option addHelp(Option option, String helpString) {
        if(option.shortForm()!=null)
            optionHelpStrings.add(" -" + option.shortForm() + "/--" + option.longForm() + ": " + helpString);
        else
            optionHelpStrings.add(" --" + option.longForm() + ": " + helpString);
        return option;
    }

    public boolean checkHelp() {
        Boolean help = (Boolean) this.getOptionValue(helpO, Boolean.FALSE);
        if (help) {
            printUsage();
            return true;
        }
        return false;
    }

    public void printUsage() {
        printUsage(System.err);
    }

    public void printUsage(OutputStream out) {
        printUsage(new PrintWriter(out));
    }

    public void printUsage(PrintWriter out) {
        out.println("Usage: program [options]");
        for (Iterator<String> i = optionHelpStrings.iterator(); i.hasNext();) {
            out.println(i.next());
        }
        out.flush();
    }

    private Option helpO = null;

    public AutoHelpCmdLineParser() {
        helpO = this.addHelp(this.addBooleanOption('h', "help"), "Shows this help message");
    }

}