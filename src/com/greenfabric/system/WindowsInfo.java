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
 * An implementation of OSInfo for "MS Windows" operating systems.
 * 
 * @see OSInfo
 * @author Robb Shecter, robb@acm.org
 */
class WindowsInfo implements OSInfo {

	public boolean supportsSymLinks() {
		return false;
	}

	public boolean isSymLink(File aFile) {
		return false;
	}

}
