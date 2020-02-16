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
        orexpr	=	andexpr, [ '||', orexpr ]
    */
    private boolean orexpr() {
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
                |	NAME, '=', expr
                |	orexpr
    */
    private boolean expr() {
        if (accept(NanoMorphoLexer.RETURN)) {
            return expr();
        }

        if (accept(NanoMorphoLexer.NAME)) {
            expect(NanoMorphoLexer.OPNAME5, "=");
            return expr();
        }

        if (orexpr()) {
            return true;
        }

        return false;
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

            return true;
        }

        return false;
    }

    /* 
        program		=	{ function } 
    */
    private boolean program() {
        return function();
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