/**
    JFlex scanner example based on a scanner for NanoMorpho.
    Authors: Hrafnkell Sigurðarson  <hrs70@hi.is>,
             Róbert Ingi Huldarsson <rih4@hi.is>,
             Frosti Grétarsson      <frg17@hi.is>,
    Date:    jan. 2020.
    
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
%line
%column
%unicode
%byaccj

%{

// This part becomes a verbatim part of the program text inside
// the class, NanoMorphoLexer.java, that is generated.

// Definitions of tokens:
public final static int ERROR = -1;
public final static int DELIM = 1000;
public final static int IF = 1001;
public final static int ELSIF = 1002;
public final static int ELSE = 1003;
public final static int NAME = 1004;
public final static int LITERAL = 1005;
public final static int WHILE = 1006;
public final static int RETURN = 1007;
public final static int VAR = 1008;
public final static int EOF = 9999;


// Opnames
public final static int OPNAME1 = 1011;
public final static int OPNAME2 = 1012;
public final static int OPNAME3 = 1013;
public final static int OPNAME4 = 1014;
public final static int OPNAME5 = 1015;
public final static int OPNAME6 = 1016;
public final static int OPNAME7 = 1017;
public final static int OPNAME_AND = 1020;
public final static int OPNAME_OR = 1021;
public final static int OPNAME_NOT = 1022;


// A variable that will contain lexemes as they are recognized:
private String lexeme;
private int token;

// This runs the scanner:
public static void main( String[] args ) throws Exception {
    NanoMorphoLexer lexer = new NanoMorphoLexer(new FileReader(args[0]));
    int token = lexer.yylex();
    System.out.println("Token: \t Lexeme:");
    while( token != 0 ) {
        System.out.println(""+token+": \t '"+lexer.getLexeme()  +"\'");
        token = lexer.yylex();
    }
}

public static NanoMorphoLexer newLexer(String fileName) throws FileNotFoundException, IOException {
    NanoMorphoLexer lexer = new NanoMorphoLexer(new FileReader(fileName));
    lexer.next();
    return lexer;
    
}

public void next() throws IOException {
    this.token = this.yylex();
    this.lexeme = this.yytext();
}

public int getToken() {
    return this.token;
}

public int getLine() {
    return this.yyline + 1;
}

public int getColumn() {
    return this.yycolumn + 1;
}

public String getLexeme() {
    return this.lexeme;
}

public String state() {
    return "Line: " + (this.yyline+1) + 
    ", column: " + (this.yycolumn+1) +
    ". Found: " + this.yytext();
}

%}

    /* Reglulegar skilgreiningar --  Regular definitions */

_MULTILINECOMMENT = (\{;;; (.*|\n|\r|\t) *;;;\})
_COMMENT = (;;;.*)
_DIGIT   = [0-9]
_FLOAT   = {_DIGIT}+\.{_DIGIT}+([eE][+-]?{_DIGIT}+)?
_INT     = {_DIGIT}+
_STRING  = \"([^\"\\] | \\b | \\t | \\n | \\f | \\r | \\\" | \\\' | \\\\ | (\\[0-3][0-7][0-7]) | \\[0-7][0-7]   | \\[0-7])*\"
_CHAR    = \'([^\'\\] | \\b | \\t | \\n | \\f | \\r | \\\" | \\\' | \\\\ | (\\[0-3][0-7][0-7]) | (\\[0-7][0-7]) | (\\[0-7]))\'
_DELIM   = [=,;(){}\[\]]
_OPNAME  = [\+\:&<>\-*/%!?\~\^|=]+
_NAME    = [:letter:]([:letter:]|{_DIGIT})*

%%

  /* Lesgreiningarreglur -- Scanning rules */

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

    if(lexeme.equals("&&")){
        return OPNAME_AND;
    } else if(lexeme.equals("||")){
        return OPNAME_OR;
    }   else if(lexeme.equals("!")){
        return OPNAME_NOT;
    }

    char firstLetter = lexeme.charAt(0);
    switch(firstLetter){
        case '*':
        case '/':
        case '%':
            return OPNAME7;
        case '+':
        case '-':
            return OPNAME6;
        case '<':
        case '>':
        case '=':
            return OPNAME5;
        case '&':
            return OPNAME4;
        case '|':
            return OPNAME3;
        case ':':
            return OPNAME2;
        case '?':
        case '~':
        case '^':
            return OPNAME1;
    }
}

{_NAME} {
    lexeme = yytext();
    return NAME;
}

// Stuff that gets ignored or returns an error:

{_MULTILINECOMMENT} {
}

{_COMMENT} {
}

<<EOF>> {
    return EOF;
}

[ \t\r\n\f] {
}

. {
    lexeme = yytext();
    return ERROR;
}
