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
 * This interface specifies the services that Operating System specific adapters
 * must implement.
 * <p>
 * 
 * This interface is a work in progress.
 * 
 * @author Robb Shecter, robb@acm.org
 */
public interface OSInfo {

	/**
	 * Tests for symbolic links in what could be an OS-dependent way.
	 * 
	 * @return true if the file is a symbolic link or false if it isn't, or if
	 *         the current OS doesn't support symbolic links.
	 * 
	 * @exception IOException
	 *                if there was an error accesing the filesystem.
	 */
	public boolean isSymLink(File aFile) throws IOException;

	/**
	 * @return true if the OS I represent supports the concept of symbolic links
	 *         and allows them to be tested from Java.
	 */
	public boolean supportsSymLinks();
}
