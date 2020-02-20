// File:         NanoMorphoParser.java
// Created:      16.02.2020
// Last Changed: 16.02.2020
// Author:       Frosti Grétarsson

   

import java.io.FileNotFoundException;
import java.io.IOException;

public class NanoMorphoParser {
    private NanoMorphoLexer lexer;

    public NanoMorphoParser() {
        
    }


    /**
     * Prentar villustreng og stöðvar keyrslu
     * @param errorString
     */
    private void error(String errorString) {
        System.err.println(errorString);
        System.exit(0);
    }

    /**
     * Prentar villustreng.
     */
    private void parseError() {
        error("Error in line: " + lexer.getLine() + " in column: " + lexer.getColumn() + 
            ". Found " + lexer.getLexeme());
    }

    /**
     * Spyr lexer hvort @token passi við síðasta lesna token í lexer
     * @param token token sem á að skoða
     * @return true ef já, annars false
     */
    private boolean accept(int token) {
        if (lexer.getToken() == token) {
            try {
                lexer.next();
                return true;
            } catch (IOException e) {
                error(e.toString());
            }
        }
        return false;
    }

    /**
     * Spyr lexer hvort @token passi við síðasta lesna token í lexer
     * og hvort @lexeme passi við síðast lesna lexeme í lexer
     * @param token token á að skoða
     * @param lexeme lexeme sem á að skoða
     * @return true ef já, annars false
     */
    private boolean accept(int token, String lexeme) {
        if (lexer.getToken() == token) {
            if (lexeme.equals(lexer.getLexeme())) {
                try {
                    lexer.next();
                    return true;
                } catch (IOException e) {
                    error(e.toString());
                }
            }
        }
        return false;
    }

    /**
     * Kallar á NanoMorphoLexer.accept með @token. Ef það skilar ekki true
     * þá mun prentast villuskilaboð og forrit endar.
     * @param token
     * @return
     */
    private void expect(int token) {
        if (!accept(token)) {
            error("Error in line: " + lexer.getLine() + " in column: " + lexer.getColumn() + 
            ". Found " + lexer.getLexeme());
        }
    }

    /**
     * Kallar á NanoMorphoLexer.accept með @token og @lexeme. Ef aðferð
     * skilar ekki true mun prentast villuskilaboð og forrit endar.
     * @param token
     * @param lexeme
     * @return
     */
    private void expect(int token, String lexeme) {

        if (!accept(token, lexeme)) {
            error("Error in line: " + lexer.getLine() + " in column: " + lexer.getColumn() + 
            ". Found '" + lexer.getLexeme() + "' but expected '"+ lexeme + "'.");
        }
        
    }

    /* 
        program		=	{ function } 
    */
    private boolean program() {
        while (function()) { /* Þátta næstu */ }
        expect(NanoMorphoLexer.EOF);

        return true;

        
    }

    /*  
        function	= 	NAME, '(', [ NAME, { ',', NAME } ] ')'
        '{', { decl, ';' }, { expr, ';' }, '}' 
    */
    private boolean function() {
        if (accept(NanoMorphoLexer.NAME)) {
            expect(NanoMorphoLexer.DELIM, "(");
            if (accept(NanoMorphoLexer.NAME)) {
                while(accept(NanoMorphoLexer.DELIM, ","))
                    expect(NanoMorphoLexer.NAME);
            }
            expect(NanoMorphoLexer.DELIM, ")");
            expect(NanoMorphoLexer.DELIM, "{");

            while (decl()) expect(NanoMorphoLexer.DELIM, ";");
            while (expr()) expect(NanoMorphoLexer.DELIM, ";");

            expect(NanoMorphoLexer.DELIM, "}");
            return true;
        }

        return false;
    } 

    /* 
        decl	= 'var', NAME, { ',', NAME }
    */
    private boolean decl() {
        if (accept(NanoMorphoLexer.VAR)) {
            expect(NanoMorphoLexer.NAME);
            while ( accept(NanoMorphoLexer.DELIM, ",") ) {
                expect(NanoMorphoLexer.NAME); 
            }
            
            return true;
        }

        return false;
    }

    /* 
        expr    =	'return', expr
                |	orexpr
    */
    private boolean expr() {
        if (accept(NanoMorphoLexer.RETURN)) {
            if (!expr()) parseError();
            return true;
        }

        else if (orexpr()) {
            return true;
        }

        return false;
    }

    /* 
        orexpr	=	andexpr, [ '||', orexpr ]
    */
    private boolean orexpr() {
        if (andexpr()) {
            if (accept(NanoMorphoLexer.OPNAME_OR)) {
                if (!orexpr()) parseError();
            }
            return true;
        }

        return false;
    }
    

    /*
        andexpr	= notexpr, [ '&&', andexpr ]
    */
    private boolean andexpr() {
        if (notexpr()) {
            if (accept((NanoMorphoLexer.OPNAME_AND))) {
                if (!andexpr()) parseError();
            }
            return true;
        }

        return false;
    }

    /* 
        notexpr		=	'!', notexpr | binopexpr1
     */
    private boolean notexpr() {
        if (accept(NanoMorphoLexer.OPNAME_NOT)) {
            if (!notexpr()) parseError();
            return true;
        }
        if (binopexpr1()) return true;

        return false;
    }

    /* 
        binopexpr1	=	binopexpr2, { OPNAME1 binopexpr2 }
     */
    private boolean binopexpr1() {
        if (binopexpr2()) {
            while (accept(NanoMorphoLexer.OPNAME1)) {
                if (!binopexpr2()) parseError();
            }
            return true;
        }
        return false;
    }

    /* 
        binopexpr2	=	binopexpr3, [ OPNAME2, binopexpr2 ]
     */
    private boolean binopexpr2() {
        if (binopexpr3()) {
            if (accept(NanoMorphoLexer.OPNAME2)) {
                if (!binopexpr2()) parseError();
            }
            return true;
        }

        return false;
    }
    
    /* 
        binopexpr3 = binopexpr4, { OPNAME3, binopexpr4 }
     */
    private boolean binopexpr3() {
        if (binopexpr4()) {
            while(accept(NanoMorphoLexer.OPNAME3)) {
                if (!binopexpr4()) parseError();
            }
            return true;
        }

        return false;
    }

    /* 
        binopexpr4	=	binopexpr5, { OPNAME4, binopexpr5 }
     */
    private boolean binopexpr4() {
        if (binopexpr5()) {
            while(accept(NanoMorphoLexer.OPNAME4)) {
                if (!binopexpr5()) parseError();
            }
            return true;
        }

        return false;
    }

    /*
        binopexpr5	=	binopexpr6, { OPNAME5, binopexpr6 }
    */
    private boolean binopexpr5() {
        if (binopexpr6()) {
            while(accept(NanoMorphoLexer.OPNAME5)) {
                if (!binopexpr6()) parseError();
            }
            return true;
        }

        return false;
    }

    /*
        binopexpr6	=	binopexpr7, { OPNAME6, binopexpr7 }
    */
    private boolean binopexpr6() {
        if (binopexpr7()) {
            while(accept(NanoMorphoLexer.OPNAME6)) {
                if (!binopexpr7()) parseError();
            }
            return true;
        }

        return false;
    }

    /*
        binopexpr7	=	smallexpr, { OPNAME7, smallexpr }
    */
    private boolean binopexpr7() {
        if (smallexpr()) {
            while(accept(NanoMorphoLexer.OPNAME7)) {
                if (!smallexpr()) parseError();
            }
            return true;
        }

        return false;
    }

    /* 
        smallexpr	=	opname, smallexpr
                    | 	LITERAL
                    |	'(', expr, ')'
                    |	ifexpr
                    |	'while', '(', expr, ')', body
     */
    private boolean smallexpr() {
        if (smallexpr_2()) {
            return true;
        }

        if (opname()) {
            if (!smallexpr()) parseError();
            return true;
        }

        if (accept(NanoMorphoLexer.LITERAL)) return true;

        if (accept(NanoMorphoLexer.DELIM, "(")) {
            if (!expr()) parseError();
            expect(NanoMorphoLexer.DELIM, ")");
            return true;
        }

        if (ifexpr()) return true;

        if (accept(NanoMorphoLexer.WHILE)) {
            expect(NanoMorphoLexer.DELIM, "(");
            if (!expr()) parseError();
            expect(NanoMorphoLexer.DELIM, ")");
            if (!body()) parseError();
            return true;
        }

        return false;
    }


    /* 
        smallexpr_name 	=   NAME
				        |	NAME, '(', [ expr, { ',', expr } ], ')'
                        |	NAME, '=', expr
     */
    private boolean smallexpr_2() {
        if (accept(NanoMorphoLexer.NAME)) {
            if (accept(NanoMorphoLexer.DELIM, "(")) {
                if (expr()) {
                    while (accept(NanoMorphoLexer.DELIM, ",")) {
                        if (!expr()) parseError();
                    }
                }
                expect(NanoMorphoLexer.DELIM, ")");
                return true;
            }

            if (accept(NanoMorphoLexer.DELIM, "=")) {
                if (!expr()) parseError();
            }
            
            return true;
        }

        return false;
    }

    /* 
        opname		=	OPNAME1
                    |	OPNAME2
                    |	OPNAME3
                    |	OPNAME4
                    |	OPNAME5
                    |	OPNAME6
                    |	OPNAME7
     */
    private boolean opname() {
        if (accept(NanoMorphoLexer.OPNAME1)) return true;
        if (accept(NanoMorphoLexer.OPNAME2)) return true;
        if (accept(NanoMorphoLexer.OPNAME3)) return true;
        if (accept(NanoMorphoLexer.OPNAME4)) return true;
        if (accept(NanoMorphoLexer.OPNAME5)) return true;
        if (accept(NanoMorphoLexer.OPNAME6)) return true;
        if (accept(NanoMorphoLexer.OPNAME7)) return true;

        return false;

    }


    /*
        ifexpr 		=	'if', '(', expr, ')' body, 
				    { 'elsif', '(', expr, ')', body }, 
                    [ 'else', body ]
    */
    private boolean ifexpr() {
        if (accept(NanoMorphoLexer.IF)) {
            expect(NanoMorphoLexer.DELIM, "(");
            if (!expr()) parseError();
            expect(NanoMorphoLexer.DELIM, ")");
            if (!body()) parseError();
            
            while (accept(NanoMorphoLexer.ELSIF)) {
                expect(NanoMorphoLexer.DELIM, "(");
                if (!expr()) parseError();
                expect(NanoMorphoLexer.DELIM, ")");
                if (!body()) parseError();
            }

            if (accept(NanoMorphoLexer.ELSE)) {
                if (!body()) parseError();
            }

            return true;
        }

        return false;
    }

    /* 
        body = '{', { expr, ';' }, '}'
     */
    private boolean body() {
        expect(NanoMorphoLexer.DELIM, "{");
        while (expr()) { 
            expect(NanoMorphoLexer.DELIM, ";");
        }
        expect(NanoMorphoLexer.DELIM, "}");

        return true;
    }

    

   

    /**
     * Þáttar forritstexta sem finnst í skjali @fileName
     * @param fileName
     * @return true ef gilt forrit
     * @throws FileNotFoundException
     * @throws IOException
     */
    public boolean parse(String fileName)throws FileNotFoundException, IOException{
        this.lexer = NanoMorphoLexer.newLexer(fileName);
        return program();
    }


    public static void main(String[] args) {
        try {
            NanoMorphoParser parser = new NanoMorphoParser();
            parser.parse(args[0]);
            System.out.println("Successfully parsed " + args[0]);
        } catch (Exception e) {
            System.out.println("File not found");
        }
    }
}