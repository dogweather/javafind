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
package com.greenfabric.find;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.String;
import org.apache.oro.text.perl.*;

/**
 * Implements the FilenameFilter interface using the Perl5 regular
 * expression library.  This class would be used when dealing with File
 * objects.
 *
 * @author	Robb Shecter, robb@acm.org
 **/
public final class RegexFilenameFilter implements FilenameFilter {
  
	private static Perl5Util regex = new Perl5Util();
	private String	pattern;

	/**
	 * Constructs a new RegexFilenameFilter that uses the given
	 * regular expression.
	 *
	 * @param	s	the regex pattern to use when matching files
	 **/
	public RegexFilenameFilter(String s) {
	    pattern = s;
	}


	/**
	 * Determines if a file matches the regular expression.
	 *
	 * @param dir	ignored
	 * @param name	the filename to be compared against the regex
	 * @return	whether the given pathname matches the regex pattern.
	 * @exception	MalformedPerl5PatternException	if the pattern was
	 *							not valid.
	 **/
	public boolean accept(File dir, String name) 
			    throws MalformedPerl5PatternException {
	    return regex.match(pattern, name);
	}
}
