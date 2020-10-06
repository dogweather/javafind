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

import java.io.*;


import com.greenfabric.system.*;
import com.greenfabric.util.*;



/**
 * This uses the GNU utilities bash, find and perl to perform the same function
 * as the Java-only Find class.  It is meant to be used only from within this
 * package.
 *
 * @author	Robb Shecter, robb@acm.org
 **/
class GnuNativeFind {

    /**
     * Internal flag for whether or not this class will be able to
     * work.  Note how it's not a primitive boolean.  Making it a
     * full object means that it can have three meaningful values:
     * true = can function, false = cannot function, null = not
     * yet tested.
     **/
    private static Boolean canFunction = null;
    private static File   findLocation = null,
                          perlLocation = null,
			  bashLocation = null;
    private static QuoteTool qtool = new QuoteTool(QuoteTool.REGEX);

    private Find myFind;


    /**
     * Construct a new GnuNativeFind that gathers the information as specified
     * by the given platform independent find.
     *
     * @exception IllegalStateException	if the current operating system is not
     *					based on GNU or does not have bash, find
     *					and perl installed.  This is a runtime
     *					exception, and so does not have to be 
     *					trapped by the application programmer.
     **/
    GnuNativeFind(Find f) throws IllegalStateException {
	if (! canFunction())
	    throw new IllegalStateException("This is not a GNU system.");
	myFind = f;
    }


    /**
     * Return true if this current environment seems to support
     * everything I need to execute smoothly.  This should be called first
     * before constructing an instance of this class.  Note that this method
     * isn�t rock-solid safe because it doesn't try executing the programs, and
     * it doesn't issue a version command to make sure they're really GNU and
     * accept all of the required options.  I made the decision not to do this
     * kind of checking in the interest of time-efficiency.  I think that chances
     * are good enough that if the first tests pass, then this class can function.
     * If not, a runtime error is thrown, and the user must fix their system.
     * (Good idea?  How much time -would- it take to verify that the programs
     * can really be executed?)
     *
     * <p>One idea would be to leave things in this method as they are, but to
     * allow Find to use the Java-only implementation if this one has a problem
     * executing.
     **/
    static boolean canFunction() {
	// Have we already tested yet?
	if (canFunction == null) {
	    findLocation = GnuInfo.findExecutable("find");
	    perlLocation = GnuInfo.findExecutable("perl");
	    bashLocation = GnuInfo.findExecutable("bash");
	    boolean b = (findLocation != null) &&
		        (perlLocation != null) &&
			(bashLocation != null) &&
			GnuInfo.isGnu();
	    canFunction = new Boolean(b);
	}

	return canFunction.booleanValue();
    }


    /**
     * Fill the given Vector with filenames, using the GNU utilities.
     *
     * This method does all the work of building the find and perl
     * command line.  It's the messiest part of this package.
     **/
    void gnuFind(StdOutConsumer consumer) throws IOException {
	/*
	 * Through options to find, we handle the Find class's
	 * max/min depth, follow, and find files/directories.
	 */
	StringBuffer findOptions = new StringBuffer();
	if (myFind.getMinDepth() != Find.DEFAULT_MIN_DEPTH)
	    findOptions.append("-mindepth "+myFind.getMinDepth()+" ");
	if (myFind.getMaxDepth() != Find.DEFAULT_MAX_DEPTH)
	    findOptions.append("-maxdepth "+myFind.getMaxDepth()+" ");
	if (myFind.getFollow())
	    findOptions.append("-follow ");

	boolean dirFlag = false;
	if (myFind.getFindDirectories()) {
	    findOptions.append("-type d ");
	    dirFlag = true;
	}
	if (myFind.getFindFiles()) {
	    if (dirFlag)
		findOptions.append("-o ");
	    findOptions.append("-type b -o -type c -o -type p -o -type f -o -type l -o -type s");
	}
	findOptions.append(" ");
	
	/*
	 * Through options to perl, we handle the Find class's
	 * negated and directories to exclude properties.
	 */
	String not;
	if (myFind.getNegated())
	    not =" ! ";
	else
	    not = "";

	StringBuffer directoryFilter;
	File[] array = myFind.getDirectoriesToExclude();
	if (array.length != 0) {
	    directoryFilter = new StringBuffer(" | "+perlLocation+
		                               " -ne 'print if ! /");
	    for (int i=0; i<array.length; i++) {
		if (i != 0)
		    directoryFilter.append("|");
		directoryFilter.append("^");
		directoryFilter.append(qtool.quote(array[i].toString()));
	    }
	    directoryFilter.append("/'");
	} else {
	    directoryFilter = new StringBuffer("");
	}

	/*
	 * Put the command together
	 */
	String command = findLocation + " " + myFind + " " + findOptions +
			 " | " +
			 perlLocation + " " +
			 "-ne 'print if " + not + myFind.getPattern() + "'"+
			 directoryFilter;

	/*
	 * The command line is completed, so we'll now execute it
	 */
	debug("Executing: "+command);
	GnuLauncher.exec( consumer, command );
    }




    /**
      * Simple debugging output is provided if the javafind.debug
      * property has been set to any value.
      **/
    private static void debug(String s) {
        if (System.getProperty("javafind.debug") != null)
            System.out.println("debug in GnuNativeFind: " + s);
    }
}
