# Makefile for the nano-morpho lexer assignmet
# Author:  Hrafnkell Sigurðarson <hrs70@hi.is>
all: NanoMorphoLexer.class NanoMorphoParser.class NanoMorphoCompiler.class

NanoMorphoParser.class: NanoMorphoParser.java
	javac -g NanoMorphoParser.java

NanoMorphoCompiler.class: NanoMorphoCompiler.java
	javac -g NanoMorphoCompiler.java

NanoMorphoLexer.class: NanoMorphoLexer.java
	javac NanoMorphoLexer.java
NanoMorphoLexer.java: nanoMorpholexer.jflex
	java -jar jflex-full-1.7.0.jar nanoMorpholexer.jflex
clean:
	rm -Rf *~ NanoMorpho*.class NanoMorphoLexer.java

# This will compile the lexer and run several tests
test: NanoMorphoLexer.class ./test/test.s
	@echo 'Testing UNICODE characters code...'
	java NanoMorphoLexer ./test/testUNICODECHARS.s
	@echo 'Testing opnames...'
	java NanoMorphoLexer ./test/testOPNAME.s
	@echo 'Testing keywords...'
	java NanoMorphoLexer ./test/testKEYWORDS.s
	@echo 'Testing morpho code...'
	java NanoMorphoLexer ./test/testMORPHO.s
	@echo 'Testing comments...'
	@echo 'This should return blank since comments are ignored...'
	java NanoMorphoLexer ./test/testCOMMENTS.s

parse: NanoMorphoLexer.class NanoMorphoParser.class
	@echo 'Testing parser'
	java NanoMorphoParser test/testNANOMORPHO.s
	@echo 'Testing parser: þetta test ætti að skila villu'
	@java NanoMorphoParser test/testNANOMORPHOFAIL.s
	@java NanoMorphoParser test/testNANOMORPHOFAIL2.s
	@java NanoMorphoParser test/testNANOMORPHOFAIL3.s

compile: NanoMorphoCompiler.class
	java NanoMorphoCompiler test/testNANOMORPHO.s

debug:
	jdb NanoMorphoParser test/testNANOMORPHO.s

