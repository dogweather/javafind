/*
 * The Original Code is 'JavaFind'
 * The Initial Developer of the Original Code is Robb Shecter. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002, 2003, 2004 by
 * Robb Shecter. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * The contents of this file may be used under the terms of the LGPL license
 * (the "GNU LIBRARY GENERAL PUBLIC LICENSE").
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Library General Public License as published 
 * by the Free Software Foundation; either version 2 of the License, or any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 */
package com.greenfabric.system;

import java.io.*;

/**
 * This class contains the knowledge needed to make decisions or detect
 * certain things about the GNU aspects of the environment.
 *
 * @author      Robb Shecter, robb@acm.org
 **/
public class GnuInfo {

    /**
     * Return true if the current operating system is one known to be based 
     * on the GNU software utilities.
     * Specifically, it checks if the current OS
     * is Linux or FreeBSD.
     **/
    public static boolean isGnu() {
	String osname = System.getProperty("os.name").toLowerCase();
	return (osname.indexOf("linux") != -1) || 
		    (osname.indexOf("freebsd") != -1);
    }


    /**
     * Tries to locate the given executable in the standard
     * executable directories.  To-do: Cache the answers given.
     *
     * @return the file if found and readable, or null if not.
     **/
    public static File findExecutable(String execName) {
	String[] binDirectories = new String[] 
	    {"/usr/bin", "/bin", "/usr/local/bin"};

	for (int i=0; i < binDirectories.length; i++) {
	    File f = new File(binDirectories[i]+"/"+execName);
	    if (f.canRead()) {
		return f;
	    }
	}

	return null;
    }
}
