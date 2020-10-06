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
/**
 * This class gives access to implementation classes in the
 * System package.  It's a <i>factory</i> that helps hide 
 * implementation details from other classes.
 *
 * @author      Robb Shecter, robb@acm.org
 **/
public class SystemPack {
    
    /**
     * Return a reference to an OSInfo implementation that's
     * appropriate for the current platform.
     **/
    public static OSInfo getOSInfo() {
	/*
	 * Currently, the logic here is very simple.  It can 
	 * be easily expanded if mroe adapters (implementations
	 * of OSInfo) are written.
	 *
	 * The os.name property is the only thing checked to
	 * determine what os we're on.  Currently, the only
	 * decision made is whether the os is "windows", or
	 * anything else.
	 */
	String osname     = System.getProperty("os.name");
	boolean isWindows = (osname.toLowerCase().indexOf("windows") != -1);

	if (isWindows)
	    return new WindowsInfo();
	else
	    return new UnixInfo();
    }
}
