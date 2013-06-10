GenomeView readme

Documentation: http://genomeview.org/

Download: http://downloads.sourceforge.net/genomeview/

Webstart: http://genomeview.org/start/launch.jnlp


Import in Eclipse:
- Make sure you have the EGit plugin or similar installed in Eclipse
- Clone the repository: git://git.code.sf.net/p/genomeview/genomeview-code genomeview-genomeview-code
	* If you want to commit you need the RW repository: https://sourceforge.net/p/genomeview/genomeview-code/
- Add all libraries in lib/ to the classpath (should already be done)
- Add a JUnit 4 library
- Add 'src' and 'resources' as source folders.
- If there is no version of JAnnot in the lib/ directory, check out the JAnnot project and add it to the GenomeView classpath