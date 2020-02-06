# Makefile for the nano-morpho lexer assignmet
# Author:  Hrafnkell Sigurðarson <hrs70@hi.is>

NanoMorphoLexer.class: NanoMorphoLexer.java
	javac NanoMorphoLexer.java
NanoMorphoLexer.java: nanoMorpholexer.jflex
	java -jar jflex-full-1.7.0.jar nanoMorpholexer.jflex
clean:
	rm -Rf *~ NanoMorphoLexer*.class NanoMorphoLexer.java

# This will compile the lexer and run several tests
test: NanoMorphoLexer.class ./test/test.s
	@echo 'Testing UNICODE characters code...'
	java NanoMorphoLexer ./test/testUNICODECHARS.s
	@echo 'Testing opnames...'
	java NanoMorphoLexer ./test/testOPNAME.s
	@echo 'Testing morpho code...'
	java NanoMorphoLexer ./test/testMORPHO.s
	@echo 'Testing comments...'
	@echo 'This should return blank since comments are ignored...'
	java NanoMorphoLexer ./test/testCOMMENTS.s
