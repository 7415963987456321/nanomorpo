/**
	JFlex scanner example based on a scanner for NanoMorpho.
	Author: Hrafnkell Sigurðarson, 2020
    Byggt á nanolexer frá Snorra Agnarssyni

	This stand-alone scanner/lexical analyzer can be built and run using:
		java -jar JFlex-full-1.7.0.jar nanomopholexer.jflex
		javac NanoMorphoLexer.java
		java NanoMorphoLexer inputfile > outputfile
	Also, the program 'make' can be used with the proper 'makefile':
		make test
    Which will make the program and run all of the tests in the /test directory.
*/

import java.io.*;

%%

%public
%class NanoMorphoLexer
%unicode
%byaccj

%{

// This part becomes a verbatim part of the program text inside
// the class, NanoMorphoLexer.java, that is generated.

// Definitions of tokens:
final static int ERROR = -1;
final static int DELIM = 1000;
final static int IF = 1001;
final static int ELSIF = 1002;
final static int ELSE = 1003;
final static int NAME = 1004;
final static int OPNAME = 1005;
final static int LITERAL = 1006;
final static int WHILE = 1007;
final static int RETURN = 1008;
final static int VAR = 1009;
final static int COMMENT = 1010;
final static int MULTILINECOMMENT = 1011;


// A variable that will contain lexemes as they are recognized:
private static String lexeme;

// This runs the scanner:
public static void main( String[] args ) throws Exception {
	NanoMorphoLexer lexer = new NanoMorphoLexer(new FileReader(args[0]));
	int token = lexer.yylex();
    System.out.println("Token: \t Lexeme:");
	while( token != 0 ) {
		System.out.println(""+token+": \t '"+lexeme+"\'");
		token = lexer.yylex();
	}
}

%}

    /* Reglulegar skilgreiningar --  Regular definitions */

_MULTILINECOMMENT = (\{;;; (.*|\n|\r|\t) *;;;\})
_COMMENT = (;;;.*(\n|\r|\t))
_DIGIT   = [0-9]
_FLOAT   = {_DIGIT}+\.{_DIGIT}+([eE][+-]?{_DIGIT}+)?
_INT     = {_DIGIT}+
_STRING  = \"([^\"\\] | \\b | \\t | \\n | \\f | \\r | \\\" | \\\' | \\\\ | (\\[0-3][0-7][0-7]) | \\[0-7][0-7]   | \\[0-7])*\"
_CHAR    = \'([^\'\\] | \\b | \\t | \\n | \\f | \\r | \\\" | \\\' | \\\\ | (\\[0-3][0-7][0-7]) | (\\[0-7][0-7]) | (\\[0-7]))\'
_DELIM   = [,;(){}\[\]]
_OPNAME  = ([\+\:&<>\-*/%!?\~\^|=] | == | -- | \+\+ )
_NAME    = [:letter:]([:letter:]|{_DIGIT})*

%%

  /* Lesgreiningarreglur -- Scanning rules */

{_MULTILINECOMMENT} {
	lexeme = yytext();
    // Uncomment for debugging this function is not supposed to return anything
	// return MULTILINECOMMENT; 
}

{_COMMENT} {
	lexeme = yytext();
    // Uncomment for debugging this function is not supposed to return anything
	// return COMMENT;
}

{_DELIM} {
	lexeme = yytext();
	return DELIM;
}

{_STRING} | {_FLOAT} | {_CHAR} | {_INT} | null | true | false {
	lexeme = yytext();
	return LITERAL;
}

// Keywords:
"while" {
	lexeme = yytext();
	return WHILE;
}

"if" {
	lexeme = yytext();
	return IF;
}

"elsif" {
	lexeme = yytext();
	return ELSIF;
}

"else" {
	lexeme = yytext();
	return ELSE;
}

"var" {
    lexeme = yytext();
    return VAR;
}


"return" {
	lexeme = yytext();
	return RETURN;
}


{_OPNAME} {
	lexeme = yytext();
	return OPNAME;
}

{_NAME} {
	lexeme = yytext();
	return NAME;
}

// Stuff that gets ignored or returns an error:
/* ";".*$ { */
/* } */

[ \t\r\n\f] {
}

. {
	lexeme = yytext();
	return ERROR;
}
