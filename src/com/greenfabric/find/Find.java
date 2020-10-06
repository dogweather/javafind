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
import java.io.IOException;
import java.util.*;

import org.apache.oro.text.perl.MalformedPerl5PatternException;

import com.greenfabric.system.OSInfo;
import com.greenfabric.system.StdOutConsumer;
import com.greenfabric.system.SystemPack;

/**
 * This is a Java version of the Unix find utility. This class extends and
 * enhances <code>java.io.File</code>. The <code>File</code> class has a method
 * named <b>list() </b> that returns all files in the represented directory.
 * This <code>Find</code> class adds the methods <b>listRecursively() </b> and
 * <b>listFilesRecursively() </b> that search subdirectories, too.
 * <p>
 * 
 * A cool feature of this class is that it will auto-detect if it's being used
 * on a GNU system like FreeBSD or Linux, and if so will optimize file searches
 * by delegating to system utilities when appropriate.
 * 
 * Currently, it looks like this class isn't too efficient reading data from the
 * system. So, Find only calls the native GNU utilities when a regular
 * expression is explicitly specified. In this case, it's still faster: about 2x
 * faster than pure Java.
 * 
 * <p>
 * This class also adds the useful method <a href="#isSymLink">isSymLink </a> to
 * the File class.
 * <p>
 * 
 * <code>Find</code>'s behavior can be configured like the GNU find(1) program;
 * for example, to return only a certain file type, or to descend only to a
 * certain maximum depth.
 * 
 * 
 * <h2>Example of embedded usage</h2>
 * 
 * This code finds all html files in the given directory or sub-directory. Note
 * that with the supplied regular expression, this will work whether the files
 * were made under DOS (.HTM), NT (.htm), or unix (.html). The regular
 * expression has two backslashes because a backslash must be quoted in a Java
 * String.
 * 
 * <pre>
 * Find myFind = new Find(&quot;/usr/local/java&quot;);
 * myFind.setPattern(&quot;/\\.html?$/i&quot;);
 * myFind.setFindDirectories(false);
 * File[] files = myFind.listFilesRecursively();
 * </pre>
 * 
 * 
 * <h2>Command-line usage syntax</h2>
 * 
 * <pre>
 * 
 * 	java com.greenfabric.find.Find [Pathname [RegularExpression]]
 * 
 * </pre>
 * 
 * 
 * <h2>Example of Unix command-line usage</h2> (I set an alias to 'java com...')
 * 
 * <pre>
 * 
 * 	find /usr/local/java \.java$
 * 
 * </pre>
 * 
 * 
 * <h2>Debugging info</h2> You can see what JavaFind is doing behind the scenes
 * by configuring either of these system properties:
 * <ul>
 * <li><b>javafind.debug: </b> If set to any value, some debug info is printed
 * to standard output, basically reporting on dynamic behavior (whether GNU
 * optimization is being done, etc.).
 * <li><b>javafind.allowoptimize: </b> Can be set to "on" or "off", or "regex",
 * performing the same function as the setOptimizeMode() method. As described
 * above, "regex" is the default.
 * </ul>
 * 
 * 
 * <h2>Todo</h2>
 * <ul>
 * <li>Possibly adapt to use a Getopts or other cmd line argument package.
 * <li>Change so that an invalid pattern exception is thrown when calling
 * setPattern(), not in listRecursively().
 * <li>Think about adding other GNU find options.
 * <li>Think about making into a Java Bean. (OK - I've now added bean-conforming
 * accessors when it makes sense. I'd like to know what it's like using this
 * class in a JavaBean IDE...)
 * <li>Think about implementing the Swing TreeModel interface.
 * <li>Think about changing to not depend on any non Java core classes. For
 * example, it would use reflection to link and use the Perl 5 regex library. If
 * it could not be found, it would fall back to using a simpler regex subset
 * that would be implemented with the String methods.
 * <li>Use log4j.
 * </ul>
 * 
 * 
 * <h2>Bugs</h2>
 * <ul>
 * <li>On Windows 95, there may be a problem if the path is like
 * <code>c:\</code>. The find seems to return no matches. It works OK for paths
 * like <code>c:</code> or <code>c:\xxxx...</code>
 * </ul>
 * 
 * @author Robb Shecter, robb@acm.org
 */
public class Find extends File {
	/*
	 * STATIC FIELDS
	 */
	private static OSInfo osInfo = SystemPack.getOSInfo();

	private static String allowOptimizeDefault = "regex";

	private static String DEBUG_PROPERTY = "javafind.debug";

	private static final String EVERYTHING_PATTERN = "//";

	private static final String BAD_PATTERN_MESSAGE = "Error:  Your regular expression is invalid.\nRegular expressions must "
			+ "follow Perl 5 syntax,\nexcept that the / characters are optional:\n"
			+ "Examples of valid regular expressions:\n\n" + "classes    html    /html/i    '/\\.h$'";

	/*
	 * OBJECT FIELDS
	 */
	private String parentPath;

	private FilenameFilter fileFilter;

	private String pattern = EVERYTHING_PATTERN;

	private Map excludeList;

	/**
	 * The default level to start searching at
	 */
	static final int DEFAULT_MIN_DEPTH = 0;

	/**
	 * The default level to search to
	 */
	static final int DEFAULT_MAX_DEPTH = Integer.MAX_VALUE;

	/*
	 * Find options, and their defaults. These are reminiscent of GNU find. When a
	 * new option is added, the private constructor must be updated.
	 */
	private boolean follow = false;

	private int maxDepth = DEFAULT_MAX_DEPTH;

	private int minDepth = DEFAULT_MIN_DEPTH;

	private boolean collectingDirectories = true;

	private boolean collectingFiles = true;

	private String optimizeMode = allowOptimizeDefault;

	/*
	 * Custom find options.
	 */
	private boolean stdOut = false; // Print names to stdout?

	private boolean negated = false;

	/*
	 * Set the default optimize mode with a static constructor.
	 */
	static {
		String s = System.getProperty("javafind.allowoptimize");
		if (s != null) {
			allowOptimizeDefault = s;
		}
	}

	/*
	 * CONSTRUCTORS
	 */

	/**
	 * Constructs a new Find object that by default matches all files.
	 * 
	 * @param directoryName the pathname of the directory to begin searching.
	 */
	public Find(String directoryName) {
		super(directoryName);

		/*
		 * Set up some of my fields
		 */
		fileFilter = new AllFilenameFilter();
		excludeList = new HashMap();

		/*
		 * Handle the case that the user gave us a directory name with the file
		 * separator on the end.
		 */
		if (directoryName.endsWith(File.separator)) {
			parentPath = getPath();
		} else {
			setParentPath();
		}
	}

	/**
	 * Constructs a new Find object that by default matches all files.
	 * 
	 * @param directory the directory to begin searching.
	 */
	public Find(File directory) {
		this(directory.getPath());
	}

	/**
	 * Constructs a new Find object that has the same options as the given find.
	 * This is used when recursing into subdirectories: A new Find is created for
	 * each subdirectory to be explored, and these new Finds should have the same
	 * options as the current one.
	 */
	private Find(Find modelFind, String name) {
		super(modelFind, name);
		fileFilter = modelFind.fileFilter;
		follow = modelFind.follow;
		minDepth = modelFind.minDepth;
		maxDepth = modelFind.maxDepth;
		collectingDirectories = modelFind.collectingDirectories;
		collectingFiles = modelFind.collectingFiles;
		stdOut = modelFind.stdOut;
		excludeList = modelFind.excludeList;
	}

	/*
	 * ACCESSORS
	 */

	/**
	 * Specifies whether matches should be printed to standard output, like unix
	 * find. Default = false. If this option is set, no matches will be returned
	 * through listRecursively().
	 */
	public void setStdOut(boolean b) {
		stdOut = b;
	}

	/**
	 * Return true if I print the names of matches to standard output instead of
	 * collect them in a data structure.
	 */
	public boolean getStdOut() {
		return stdOut;
	}

	/**
	 * Specify if Find can optimize things by using GNU tools when they exist.
	 * 
	 * @param mode <b>on </b>= Optimize whenever GNU is detected. <b>regex </b>=
	 *             optimize only when a regex has been explicitly specified. <b>off
	 *             </b>= never optimize. Default = regex.
	 */
	public void setOptimizeMode(String mode) {
		optimizeMode = mode;
	}

	/**
	 * Specify a list of directories that should not be traversed into.
	 */
	public void setDirectoriesToExclude(File[] dirs) {
		/*
		 * For this functionality, a Hashtable is much easier to work with (and more
		 * efficient) than an array. I decided to use the strongly typed File[] for the
		 * component's interface, and the weakly typed Hashtable for use inside of the
		 * component.
		 * 
		 * Since this class controls the total use of the hashtable, it knows what kind
		 * of objects are in there.
		 */

		Integer zero = new Integer(0);
		excludeList = new Hashtable();
		for (int i = 0; i < dirs.length; i++) {
			excludeList.put(dirs[i], zero);
		}
	}

	/**
	 * Return the list of directories to be excluded.
	 */
	public File[] getDirectoriesToExclude() {
		int size = excludeList.size();
		File[] array = new File[size];
		if (size == 0)
			return array;

		Iterator keys = excludeList.keySet().iterator();
		int i = 0;
		while (keys.hasNext()) {
			array[i] = (File) keys.next();
			i++;
		}
		return array;
	}

	/**
	 * Specify whether directories should be included in the find listing. Default
	 * is true.
	 */
	public void setFindDirectories(boolean b) {
		collectingDirectories = b;
	}

	/**
	 * Return true if directories will be returned
	 */
	public boolean getFindDirectories() {
		return collectingDirectories;
	}

	/**
	 * Specify whether plain files should be included in the find listing. Default
	 * is true.
	 */
	public void setFindFiles(boolean b) {
		collectingFiles = b;
	}

	/**
	 * Return true if plain files (non-directories) will be returned.
	 */
	public boolean getFindFiles() {
		return collectingFiles;
	}

	/**
	 * Specify whether to follow symbolic links. Default is false.
	 */
	public void setFollow(boolean b) {
		follow = b;
	}

	/**
	 * Return true if I will follow symbolic links
	 */
	public boolean getFollow() {
		return follow;
	}

	/**
	 * Specifies the maximum depth to descend to. Default is Integer.MAX_VALUE.
	 * 
	 * @param depth the maximum directory depth to recurse into. A maxdepth of 0
	 *              means only apply the tests and actions to the starting file
	 *              itself.
	 */
	public void setMaxDepth(int depth) {
		maxDepth = depth;
	}

	/**
	 * Return the maximum depth I will recurse to.
	 */
	public int getMaxDepth() {
		return maxDepth;
	}

	/**
	 * Specifies the minimum depth to find files at. Default is 0.
	 * 
	 * @param depth the minimum depth. 0 = start returning filenames immediately.
	 */
	public void setMinDepth(int depth) {
		minDepth = depth;
	}

	/**
	 * Return the minimum depth.
	 */
	public int getMinDepth() {
		return minDepth;
	}

	/**
	 * Specify to find files where the regular expression <i>doesn't </i> match.
	 * This is the same idea as the -v option on grep.
	 * <p>
	 * 
	 * I added this when I was writing another program and needed to find all files
	 * that were <i>not </i> HTML files. This had to take into account files named
	 * like .HTM, .html, etc. I haven't been able to come up with a regex to do
	 * this. Now this can be done with the following code:
	 * <p>
	 * 
	 * <pre>
	 * Find f = new Find(&quot;/&quot;);
	 * f.setPattern(&quot;/\\.html?/i&quot;);
	 * f.setNegated(true);
	 * Files[] files = f.findFilesRecursively();
	 * </pre>
	 */
	public void setNegated(boolean b) {
		negated = b;
		setPattern(pattern);
	}

	/**
	 * Return true if the regex logic is negated.
	 */
	public boolean getNegated() {
		return negated;
	}

	/**
	 * Return the regular expression I use.
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Specify the regex pattern to use when matching filenames. Default is to match
	 * all files.
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;

		/*
		 * I found this interesting: Using OO design to avoid case and if/then
		 * statements down in the searching code. Instead of checking the options when
		 * doing the actual work, delegates are created according to the currently set
		 * options. This way, there is no decision-making in the code itself. This makes
		 * it more efficient, and hopefully more understandable.
		 */

		/*
		 * Optimize if no pattern was specified
		 */
		if (pattern.equals(EVERYTHING_PATTERN)) {
			fileFilter = new AllFilenameFilter();
			return;
		}

		if (negated) {
			fileFilter = new NegatedRegexFilenameFilter(pattern);
		} else {
			fileFilter = new RegexFilenameFilter(pattern);
		}
	}

	/**
	 * Return true if I am a symbolic link.
	 * 
	 * @return true if the file is a Symbolic Link.
	 * @exception IOException if the OS cannot access the info it needs to check the
	 *                        file status.
	 */
	public boolean isSymLink() throws IOException {

		/*
		 * The logic has been moved to an OSInfo adapter, so that symlinks are checked
		 * in the appropriate way for the current operating system.
		 */
		return osInfo.isSymLink(this);
	}

	/*
	 * PUBLIC SERVICES
	 */

	/**
	 * Perform the find recursively, returning an array of File objects. This method
	 * is the one that would be used by applications doing anything non-trivial with
	 * the Find results.
	 * 
	 * @exception MalformedPerl5PatternException if the regex pattern has a syntax
	 *                                           error
	 * @exception IOException                    if there's a problem accessing the
	 *                                           file system, or executing native
	 *                                           GNU utilities.
	 * @return The list of matching Files
	 */
	public File[] listFilesRecursively() throws MalformedPerl5PatternException, IOException {
		List fileList = generateList();

		// Create an array of files from the vector of files.
		File[] array = new File[fileList.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = new File((String) fileList.get(i));
		}

		return array;
	}

	/**
	 * Perform the find recursively, returning a list of strings. This is here to be
	 * parallel with the list() method of java.io.File, and for efficiency, in case
	 * all the client is interested in is Strings.
	 * 
	 * @exception MalformedPerl5PatternException if the regex pattern has a syntax
	 *                                           error
	 * @exception IOException                    if there's a problem accessing the
	 *                                           file system, or executing native
	 *                                           GNU
	 * @return The list of matching filenames
	 */
	public Iterator listRecursively() throws IOException, MalformedPerl5PatternException {
		return generateList().iterator();
	}

	/*
	 * PRIVATE METHODS
	 */

	/**
	 * This method acts like a switch, either starting the recursive find, written
	 * in Java, or delegating the task to the operating system.
	 */
	private List generateList() throws MalformedPerl5PatternException, IOException {
		final List fileList = new LinkedList();
		StdOutConsumer printer, saver, currentConsumer;

		/*
		 * 1. Prepare two types of consumers, that do different things w/ the output.
		 */
		printer = new StdOutConsumer() {
			public void receive(String s) {
				System.out.println(s);
			}
		}; // I print everything to stdout!

		saver = new StdOutConsumer() {
			public void receive(String s) {
				fileList.add(s);
			}
		}; // I save everything in a vector!

		/*
		 * 2. Decide which consumer will handle the output for this run.
		 */
		currentConsumer = stdOut ? printer : saver;

		/*
		 * 3. Start the find going, sending the output to the chosen consumer.
		 */
		boolean canOptimize = optimizeMode.equals("on")
				|| (optimizeMode.equals("regex") && !pattern.equals(EVERYTHING_PATTERN));

		if (canOptimize && GnuNativeFind.canFunction()) {
			debug("Using GNU Native Find");
			GnuNativeFind nativeFind = new GnuNativeFind(this);
			nativeFind.gnuFind(currentConsumer);
		} else {
			debug("Using Pure-Java Find");
			if (test(this))
				currentConsumer.receive(this.toString());
			if (maxDepth == 0)
				return fileList;
			listRecursively(currentConsumer, 1);
		}

		return fileList;
	}

	/**
	 * Return true if the given file matches the current regex and all other tests.
	 * Currently, this method is used only from generateList(), but it'd be nice to
	 * factor it out of filterFiles, too.
	 */
	private boolean test(Find file) {
		if (fileFilter.accept(null, file.getAbsolutePath())) {
			if (file.isDirectory()) {
				if (file.collectingDirectories) {
					return true;
				}
			} else {
				if (file.collectingFiles) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Perform the recursive action. It consists of three steps:
	 * 
	 * <ol>
	 * <li>Generate a list of all files in the current directory, and filter out the
	 * matching files as well as the subdirectories to recurse into.
	 * 
	 * <li>Return the output to the output consumer.
	 * 
	 * <li>Loop through all subdirectories, executing their "listRecursively()"
	 * methods.
	 * </ol>
	 * 
	 * @param consumer an object that takes all of the output and does something
	 *                 with it.
	 * @param depth    the current depth we�re at.
	 */
	private void listRecursively(StdOutConsumer consumer, int depth) throws IOException {
		String[] currentFiles = list();
		if (currentFiles == null) { // Empty directory?
			return;
		}
		List descendList = new LinkedList();
		Iterator files = filterFiles(currentFiles, descendList);

		/*
		 * Copy over the current directory's files. This is where we save the matches.
		 * Do it only if we're up to the minDepth.
		 */
		if (depth >= minDepth) {
			while (files.hasNext()) {
				consumer.receive(parentPath + files.next());
			}
		}

		/*
		 * Iterate through the given list of subdirectories. Only go a level lower if we
		 * haven't reached the maxDepth. This is the recursive step.
		 */
		if (depth < maxDepth) {
			Iterator subFinds = descendList.iterator();
			while (subFinds.hasNext()) {
				Find find = (Find) subFinds.next();
				// "If we're following, or it's not a symlink..."
				if (follow || (!find.isSymLink())) {
					find.listRecursively(consumer, depth + 1);
				}
			}
		}
	}

	/**
	 * Given a filename filter and a list of files, create two subsets of the files:
	 * 1) A list of matching filenames, and 2) A list of subdirectories to recurse
	 * into.
	 * 
	 * This method has been constructed so that calls to the File object, like
	 * isDirectory() and isFile() are done at most once per file. And, calls like
	 * getPath() are done at most once per directory.
	 * 
	 * @return a list of files that are matches.
	 * @param listing     the list of files in the current directory
	 * @param descendList another return value - this will contain the list of
	 *                    directories to be descended into. (No, this isn't great
	 *                    style. At this level, though, I want to avoid creating
	 *                    another new object for containing the complex return value
	 *                    of this function.)
	 */
	private Iterator filterFiles(String[] listing, List descendList) {
		List filesToKeep = new LinkedList();

		// Copy over only the desired files from the full listing.
		String fileName;
		Find tempFind;
		for (int i = 0; i < listing.length; i++) {
			fileName = listing[i];
			tempFind = new Find(this, fileName);

			if (tempFind.isDirectory()) {
				/*
				 * Once we know that this is a directory, we tell it to set up its parent path.
				 */
				tempFind.setParentPath();
				if (!excludeList.containsKey(tempFind)) {
					descendList.add(tempFind);
					if (collectingDirectories && fileFilter.accept(null, tempFind.getAbsolutePath())) {
						filesToKeep.add(fileName);
					}
				}
			} else { // Is a plain file.
				if (collectingFiles && fileFilter.accept(null, tempFind.getAbsolutePath())) {
					filesToKeep.add(fileName);
				}
			}

		}
		return filesToKeep.iterator();
	}

	/**
	 * Set up my parentPath variable correctly. This works, but I think that the
	 * logic is redundant.
	 */
	private void setParentPath() {
		parentPath = makeParentPath();
	}

	/**
	 * Create the parent pathname of the current file. This takes relative file
	 * names into account. One enhancement would be to have a version of this where
	 * it always returns an absolute path.
	 */
	private String makeParentPath() {
		return getPath() + File.separator;
	}

	/**
	 * Provide debugging output if the debug property has been set (to anything).
	 */
	private static void debug(String s) {
		if (System.getProperty(DEBUG_PROPERTY) != null)
			System.out.println("debug in JavaFind: " + s);
	}

	/**
	 * Handles execution when used from the command line. This is a wimpy main(),
	 * and could be improved with a getargs type of package.
	 */
	public static void main(String[] args) throws IOException {
		if (args.length > 2) {
			System.out.println("Usage: Find [pathname [regex]]");
			return;
		}
		String path = ".";
		if (args.length >= 1) { // Path specified?
			path = args[0];
		}
		Find find = new Find(path);
		find.setStdOut(true); // Give us output on stdout.

		if (args.length == 2) { // Pattern specified?
			find.setPattern(args[1]);
		}
		try {
			find.listRecursively();
		} catch (MalformedPerl5PatternException e) {
			try {
				find.setPattern("/" + args[1] + "/");
				find.listRecursively();
			} catch (MalformedPerl5PatternException e2) {
				System.err.print(BAD_PATTERN_MESSAGE);
				return;
			}
		}
	}

}
