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
package com.greenfabric.util;

import java.util.StringTokenizer;
import org.apache.oro.text.perl.Perl5Util;

/**
 * This class provides a 'quoting' service.  This is necessary because
 * 'special' characters have to be quoted before they can be included
 * in an SQL query, or used in other special contexts. <p>
 *
 * It's also a small collaborative effort - experience from different people
 * with different databases can be added here to help others out making
 * cross-platform applications.
 *
 * <h2>Usage Example:</h2>
 * <pre>
 *   String text    = "original text";
 *   QuoteTool tool = new QuoteTool(QuoteTool.SQL_SERVER);
 *   System.out.printn("quoted text = " + tool.quote(text) );
 * </pre>
 *
 * <h2>Open issues</h2>
 * <ul>
 * <li>
 * Is there a way to get a database/driver name from JDBC so that
 * the appropriate QuoteTool adapter could be automatically chosen?
 *
 * <li>
 * Should the quote tool do anything about start and end quotes for
 * strings in SQL statements?  All known SQL servers will accept a single
 * quote, so it doesn't have to be.  If it is, though, it needs to be
 * optional, because it can interfere with wanting to put together
 * a LIKE statement with.
 *
 * <li>
 * It may also be that some of these are not quite complete: (What's
 * the LIKE character in Access?  How does/should it get quoted?)  If
 * <b>you</b> have some knowledge you can contribute, please do!
 *
 * @author      Robb Shecter, robb@acm.org
 **/
public class QuoteTool {

    private QuoteDelegate serverType;	// Handles different cases
    private static Perl5Util regex = new Perl5Util(); // Pattern matcher
    private static final char bs = '\\';	// The backspace character


	/**
	 * A quote delegate contains the actual commands that quote a 
	 * string depending on a particular database.  There will be 
	 * one quote delegate for each SQL database that's supported.
	 **/
	interface QuoteDelegate {
		public String quote(String s);
	}


	/**
	 * Specify MySQL syntax.
	 *
	 * <pre>
	 * \	--->	\\
	 * '	--->	\'
	 * "    --->    \"
	 * %    --->    \%
	 * _    --->    \_
	 * </pre>
	 **/
	public static final QuoteDelegate MYSQL = new QuoteDelegate() {
	public String quote(String s) {
	    return regex.substitute("s/(["+bs+bs+"'\"%_])/"+bs+"$1/g", s);
	}};
	

	/**
	 * Specify Instant DB syntax.
	 *
	 * <pre>
	 * \	--->	\\
	 * '	--->	\'
	 * </pre>
	 **/
	public static final QuoteDelegate INSTANT_DB = new QuoteDelegate() {
	public String quote(String s) {
	    return regex.substitute("s/(["+bs+bs+"'])/"+bs+"$1/g", s);
	}};
	

	/**
	 * Specify Microsoft Access '97 syntax.
	 *
	 * <pre>
	 * '	--->	''
	 * </pre>
	 **/
	public static final QuoteDelegate ACCESS97 = new QuoteDelegate() {
	public String quote(String s) {
	    return regex.substitute("s/(['])/'$1/g", s);
	}};


	/**
	 * Specify the Microsoft SQL Server syntax.
	 *
	 * <pre>
	 * \	--->	\\
	 * '	--->	\'
	 * </pre>
	 **/
	public static final QuoteDelegate SQL_SERVER = new QuoteDelegate() {
	public String quote(String s) {
	    return regex.substitute("s/(["+bs+bs+"'])/"+bs+"$1/g", s);
	}};

	
	/**
	 * Specify HTML quoting. (This is unreadable in Javadocs - see
	 * the source code to understand what transformations this
	 * quote tool makes.)
	 *
	 * <pre>
	 * &	--->	&amp;
	 * >	--->	&gt;
	 * <	--->	&lt;
	 * "	--->	&quot;
	 * </pre>
	 **/
	public static final QuoteDelegate HTML = new QuoteDelegate() {
	public String quote(String s) {
	   s = regex.substitute("s/&/&amp;/g", s);
	   s = regex.substitute("s/>/&gt;/g", s);
	   s = regex.substitute("s/</&lt;/g", s);
	   s = regex.substitute("s/\"/&quot;/g", s);
	   return s;
	}};

	
	/**
	 * Specify Regular expression quoting.  This is really just
	 * a start - it should probably also quote the "." character
	 * and others..
	 *
	 * <pre>
	 * /	--->	\/
	 * </pre>
	 **/
	public static final QuoteDelegate REGEX = new QuoteDelegate() {
		public String quote(String s) {
			return replace(s, "/").toString();
		}
		
		/**
		 * Do a one-character quoting/substitution w/out
		 * the regex engine.
		 **/
		private StringBuffer replace(String text, String character) {
			StringBuffer result = new StringBuffer();
			/*
			 * Handle the case if the character is the first
			 * character.
			 */
			if (text.startsWith(character)) {
				result.append("\\" + character);
			}

			/*
			 * Now check for other instances.
			 */
			StringTokenizer st = new StringTokenizer(text, character);
			while (st.hasMoreTokens()) {
				result.append(st.nextToken());
				if (st.hasMoreTokens()) {
					result.append("\\" + character);
				}
			}
			return result;
		}
	};
	

    /**
     * Specify HTTP URL quoting.
     **/
    public static final QuoteDelegate HTTP = new QuoteDelegate() {
	
	public String quote(String s) {
	    StringBuffer result = new StringBuffer();
	    for (int i=0; i<s.length(); i++) {
		char currentChar = s.charAt(i);
		if (isUnsafe(currentChar)) {
		    result.append(encode(currentChar));
		} else {
		    result.append(currentChar);
		}
	    }
	    return result.toString();
	}
	
	/**
	 * Return true if the given character is unsafe for 
	 * use in an HTTP URL. This method currently reports
	 * only ascii letters and numbers as unsafe.  It's
	 * a bit unclear from the RFC exactly which special
	 * characters need to be quoted. Only readability
	 * suffers from quoting too many characters...
	 **/
	private boolean isUnsafe(char c) {
	    if (c > 126 || c < 32)             // Control or non-ascii char?
	        return true;

	    if (Character.isLetterOrDigit(c)) // A letter or number?
	        return false;
	    
	    return true;                       // Must be a char like %&/(), etc.
	}
	
	/**
	 * Encode the given character properly for HTTP URLs.
	 **/
	private String encode(char c) {
	    if (c < 16) {
		return "%0" + Integer.toString(c, 16);
	    } else {
		return "%" + Integer.toString(c, 16);
	    }
	}
    };



    /**
     * Create a new QuoteTool.  It must be passed one of the delegates
     * defined in this class. For example:
     * <pre>
     *     tool = new QuoteTool(QuoteDelegate.ACCESS97);
     * </pre>
     *
     * @param	serverType	the type of sql server in use
     **/
    public QuoteTool(QuoteDelegate serverType) {
	this.serverType = serverType;
    }




    /**
     * Class testing
     **/
    public static void main(String[] args) {
	if (args.length != 1) {
	    System.out.println("Usage: QuoteTool <test text>");
	    System.exit(1);
	}

	String text = args[0];
	QuoteTool tool1 = new QuoteTool(QuoteTool.SQL_SERVER);
	QuoteTool tool2 = new QuoteTool(QuoteTool.ACCESS97);
	QuoteTool tool3 = new QuoteTool(QuoteTool.MYSQL);
	QuoteTool tool4 = new QuoteTool(QuoteTool.HTML);
	QuoteTool tool5 = new QuoteTool(QuoteTool.INSTANT_DB);
	QuoteTool tool6 = new QuoteTool(QuoteTool.HTTP);

	System.out.println("Original text: "+text);
	System.out.println("SQL SERVER:    "+tool1.quote(text));
	System.out.println("ACCESS97:      "+tool2.quote(text));
	System.out.println("MYSQL:         "+tool3.quote(text));
	System.out.println("HTML:          "+tool4.quote(text));
	System.out.println("INSTANT DB:    "+tool5.quote(text));
	System.out.println("HTTP:          "+tool6.quote(text));
    }



    /**
     * Quote the given string so that it can be used in an
     * SQL or other kind of statement.
     **/
    public String quote(String s)
    {
	return serverType.quote(s);
    }
}
