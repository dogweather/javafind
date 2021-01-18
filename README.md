# java-find

> Unix [find](https://leemendelowitz.github.io/blog/gnu-find.html) implemented in Java. Vintage code from 2004

This is a Java version of the Unix find utility. This class extends 
`java.io.File` whose `list()` method that returns all files in a directory.
`Find` adds two more:

* `listRecursively()`
* `listFilesRecursively()`

A cool feature of this class is that it will auto-detect if it's being used
on a GNU system like FreeBSD or Linux, and if so will optimize file searches
by delegating to system utilities when appropriate.

Currently, it looks like this class isn't too efficient reading data from the
system. So, Find only calls the native GNU utilities when a regular
expression is explicitly specified. In this case, it's still faster: about 2x
faster than pure Java.

This class also adds the useful method [isSymLink](https://github.com/dogweather/javafind/blob/main/src/com/greenfabric/find/Find.java#L478-L492)
to the File class.

Java-Find's behavior can be configured like
[the GNU find](https://leemendelowitz.github.io/blog/gnu-find.html)
program; for example, to return only a certain file type, or to descend
only to a certain maximum depth.


## Example of programmatic usage

This code finds all html files in the given directory or sub-directory. Note
that with the supplied regular expression, this will work whether the files
were made under DOS (.HTM), NT (.htm), or unix (.html). The regular
expression has two backslashes because a backslash must be quoted in a Java
String.

```java
Find myFind = new Find("/usr/local/java");
myFind.setPattern("/\\.html?$/i");
myFind.setFindDirectories(false);
File[] files = myFind.listFilesRecursively();
```


## Example of command-line usage

(On Windows, I set an alias to 'java com...')

```
find /usr/local/java \.java$
```


## Command-line usage syntax

```
java com.greenfabric.find.Find [Pathname [RegularExpression]]
```


## Debugging Output

You can see what JavaFind is doing behind the scenes
by configuring either of these system properties:

`javafind.debug` If set to any value, some debug info is printed
to standard output, basically reporting on dynamic behavior (whether GNU
optimization is being done, etc.).

`javafind.allowoptimize` Can be set to "on" or "off", or "regex",
performing the same function as the setOptimizeMode() method. As described
above, "regex" is the default.



## Todo

<ul>
<li>Possibly adapt to use a Getopts or other cmd line argument package.
<li>Change so that an invalid pattern exception is thrown when calling
setPattern(), not in listRecursively().
<li>Think about adding other GNU find options.
<li>Think about making into a Java Bean. (OK - I've now added bean-conforming
accessors when it makes sense. I'd like to know what it's like using this
class in a JavaBean IDE...)
<li>Think about implementing the Swing TreeModel interface.
<li>Think about changing to not depend on any non Java core classes. For
example, it would use reflection to link and use the Perl 5 regex library. If
it could not be found, it would fall back to using a simpler regex subset
that would be implemented with the String methods.
<li>Use log4j.
</ul>


## Bugs

<ul>
<li>On Windows 95, there may be a problem if the path is like
<code>c:\</code>. The find seems to return no matches. It works OK for paths
like <code>c:</code> or <code>c:\xxxx...</code>
</ul>

@author Robb Shecter, robb.shecter@gmail.com
