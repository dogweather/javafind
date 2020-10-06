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
import org.apache.oro.text.perl.Perl5Util;

/**
 * An implementation of OSInfo for "Unix" operating systems.
 *
 * @author      Robb Shecter, robb@acm.org
 **/
class UnixInfo implements OSInfo {
    private Perl5Util regex = new Perl5Util();

    /**
     * Regex-notation for the file path separator.
     **/
    private static String sep = "\\" + File.separatorChar;


    public boolean supportsSymLinks() { return true; }


    /**
      * Return true if this is a symbolic link.  The
      * basic algorithm is to compare the absolute path with the canonical
      * path.  If they're not equal, then the file is a symbolic link. <p>
      *
      * It turns out that a bit of work is required to rule out a "false
      * positive".  Java does not remove relative path info from
      * the absolute path, so it may still turn out to -not- be
      * a symlink.  That is, once relative information is removed from
      * the absolute path, it may really equal the canonical path.  The
      * complete algorithm is now:
      *
      * <ol>
      *	<li>	If the absolute path equals the canonical path, return
      *	 	"not a symlink".
      *	<li>	Remove possible relative path information from the absolute
      *		path:
      *		<ol>
      *			<li> Replace every occurrence of "dirname/." with
      *			     "dirname".
      *			<li> Replace every occurrence of "dirname/.." with
      *			     empty string.  (Remove it.)
      *		</ol>
      *	<li>	If the absolute path equals the canonical path, return
      *		"not a symlink".
      *	<li>	Else, return "is a symlink".
      * </ol>
      *
      * @return		true if the file is a Symbolic Link.
      * @exception	IOException	if the OS cannot access the info
      *					it needs to check the file status.
      **/
    public boolean isSymLink(File aFile) throws IOException {
        String absolutePath  = aFile.getAbsolutePath();
        String canonicalPath = aFile.getCanonicalPath();

        /*
         * Test for the most common case first:  not a symlink.
         */
        if (absolutePath.equals(canonicalPath)) {
            return false;
        }

        String fixedAbsolutePath = absolutePath;

        /*
         * Change "dirname/." to "dirname" iteratively
         */
        while (regex.match("/"+sep+"\\.("+sep+"|$)/", fixedAbsolutePath)) {
            fixedAbsolutePath =
                    regex.substitute("s/"+sep + "\\.("+sep + "|$)/$1/",
                    fixedAbsolutePath);
        }
	
        /*
         * Change "dirname/.." to "" iteratively
         */
        while (regex.match("/"+sep + "[^"+sep + "]+"+sep + "\\.{2}("+
			   sep + "|$)/", fixedAbsolutePath)) {
            fixedAbsolutePath =
		regex.substitute("s/("+sep + ")[^"+sep + "]+" +
				 sep +
				 "\\.{2}("+sep + "|$)/$1/", fixedAbsolutePath);
        }
	
        if (fixedAbsolutePath.equals(canonicalPath)) {
            return false;
        } else {
            return true;
        }
    }
    
}
