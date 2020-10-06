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
 * This class performs the service of executing commands in a GNU
 * environment.  It's main value is that it's not limited to a single
 * command with a set of parameters, like RunTime.exec() is.  It hands
 * the command off to bash, allowing pipes and other more complex
 * flow control to be used.
 * <p>
 * A second feature of this class is that it allows all of the RunTime
 * and Process code to be written in one place.  By using a closure-like
 * <i>consumer</i> object, this class can be used by any application.
 *
 * <h2>Examples</h2>
 * Here's a couple of short examples of how to use GnuLauncher and 
 * StdOutConsumer together.
 *
 * <pre>
 *  //
 *  // Execute find piped into wc, printing the results to standard output.
 *  //
 *  myConsumer = new StdOutConsumer() { public void receive(String s) {
 *        System.out.println(s);
 *     }};
 *  GnuLauncher.exec(myConsumer, "/usr/bin/find | /usr/bin/wc");
 *  <p>
 *  //
 *  // Execute finger, saving the results in a Vector.
 *  //
 *  final Vector results = new Vector();
 *  myConsumer = new StdOutConsumer() { public void receive(String s) {
 *        results.addElement(s);
 *     }};
 *  GnuLauncher.exec(myConsumer, "/usr/bin/finger");
 * </pre>
 *
 * Instead of hardcoding the pathnames, the GnuInfo class can locate
 * executables.
 *
 * <h2>Ideas &amp; thoughts</h2>
 * <ul>
 * 	<li>Some of the more common consumers, like the two above can be
 *          written as separate classes instead of as anonymous inner
 *	    classes.
 *	<li>This framework can include classes or methods for checking
 *          well known utilities to see if they are the GNU versions.
 *	<li>This framework handles only the most simplest case: reading the
 *          standard output from a command.  What about handling more
 *          complex cases like stderr, and input to the command?  Could this
 *          be added to the framework, and keep it just as simple?
 * </ul>
 *
 * @author      Robb Shecter, robb@acm.org
 * @see GnuInfo
 **/
public class GnuLauncher {

    private static String  bashLocation = null;
    private static Boolean canFunction = null;
    private static Runtime rt = Runtime.getRuntime();


    /**
     * Execute a command using bash.  Tested with bash version 
     * 2.02.2(1)-release.
     *
     * @param consumer	an object that does something with the output.
     *			This method iterates over the standard input,
     *			calling consumer.receive(String) once for each
     *			line.
     * @param command	a bash / unix command to be executed.  Since this
     *			is handled by bash, it can contain pipes,
     *			redirection, etc.
     * @exception IllegalStateException	if the system does not provide support
     *					for launching programs via bash.
     * @exception IOException		if an error occurred starting the
     *					system process.
     **/
    public static void exec(StdOutConsumer consumer, String command) throws 
	IOException, IllegalStateException {

	if (! canFunction())
	    throw new IllegalStateException("This is not a GNU system.");

	String[] cmd = new String[] { bashLocation, "-c", command };
	Process p  = rt.exec(cmd);
	BufferedReader in = new BufferedReader(
				new InputStreamReader(
				    p.getInputStream()));

	String line;
	while ((line = in.readLine()) != null) {
	    consumer.receive(line);
	}
	in.close();
    }


    /**
     * Return true if I can work in this environment.  Should be checked before
     * attempting to call exec().
     **/
    public static boolean canFunction() {
	if (canFunction == null) {
	    bashLocation = GnuInfo.findExecutable("bash").toString();
	    canFunction  = new Boolean( bashLocation != null );
	}

	return canFunction.booleanValue();
    }
}
