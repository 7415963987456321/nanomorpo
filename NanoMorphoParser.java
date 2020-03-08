// File:         NanoMorphoParser.java
// Created:      16.02.2020
// Last Changed: 16.02.2020
// Author:       Frosti Grétarsson

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;


public class NanoMorphoParser {
    private NanoMorphoLexer lexer;
    private int lastToken;
    private String lastLexeme;
    private Vector<String> vars;
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
                lastToken = lexer.getToken();
                lastLexeme = lexer.getLexeme();
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
                    lastToken = lexer.getToken();
                    lastLexeme = lexer.getLexeme();
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

    int varPos(String name)
    {
        for (int i = 0; i < vars.size(); i++) {
            if(vars.get(i).equals(name)) return i;
        }
        error("Variable "+name+" is not defined");
        return -1;
    }

    /* 
        program		=	{ function } 
    */
    private Object[] program() {
        Vector<Object> f = new Vector<>();
        Object[] fun = function();
        while (fun != null) {
            f.add(fun);
            fun = function();
        }
        expect(NanoMorphoLexer.EOF);

        //generateProgram(f);
        return f.toArray();
    }

    /*  
        function	= 	NAME, '(', [ NAME, { ',', NAME } ] ')'
        '{', { decl, ';' }, { expr, ';' }, '}' 
    */
    private Object[] function() {
        if (accept(NanoMorphoLexer.NAME)) {
            String functionName = lastLexeme;
            Vector<Object> fun = new Vector<>();
            int argCount = 0;
            int varCount = 0;
            vars = new Vector<>();
            expect(NanoMorphoLexer.DELIM, "(");
            if (accept(NanoMorphoLexer.NAME)) {
                vars.add(lastLexeme);
                argCount++;
                while(accept(NanoMorphoLexer.DELIM, ",")) {
                    argCount++;
                    expect(NanoMorphoLexer.NAME);
                    vars.add(lastLexeme);
                }
            }
            expect(NanoMorphoLexer.DELIM, ")");
            expect(NanoMorphoLexer.DELIM, "{");

            int varDecls = decl();
            while (varDecls > 0) {
                varCount += varDecls;
                expect(NanoMorphoLexer.DELIM, ";");
                varDecls = decl();
            }
            
            Vector<Object> expressions = new Vector<>();
            Object[] ex = expr();
            while (ex != null) {
                expressions.add(ex);
                expect(NanoMorphoLexer.DELIM, ";");
                ex = expr();
            }


            expect(NanoMorphoLexer.DELIM, "}");
            return new Object[] {functionName, argCount, varCount, expressions.toArray()};

        };

        return null;
    }

    /* 
        decl	= 'var', NAME, { ',', NAME }
    */
    private int decl() {
        int decls = 0;
        if (accept(NanoMorphoLexer.VAR)) {
            expect(NanoMorphoLexer.NAME);
            decls++;
            vars.add(lastLexeme);
            while ( accept(NanoMorphoLexer.DELIM, ",") ) {
                expect(NanoMorphoLexer.NAME);
                decls++;
                vars.add(lastLexeme);
            }
        }
        return decls;
    }

    /* 
        expr    =	'return', orexpr
                |	orexpr
    */
    private Object[] expr() {
        Object[] ex = null;
        if (accept(NanoMorphoLexer.RETURN)) {
            ex = orexpr();
            if (ex == null) parseError();
            return new Object[] {"RETURN", ex};
        }
        else {
            ex = orexpr();
        }
        return ex;
    }

    /* 
        orexpr	=	andexpr, [ '||', orexpr ]
    */
    private Object[] orexpr() {
        Object[] ex = andexpr();
        if (ex != null) {
            if (accept(NanoMorphoLexer.OPNAME_OR)) {
                Object[] ex2 = orexpr();
                if (ex2 == null) parseError();
                return new Object[] {"OR", ex, ex2};
            }
            return ex;
        }
        return null;
    }
    

    /*
        andexpr	= notexpr, [ '&&', andexpr ]
    */
    private Object[] andexpr() {
        Object[] ex = notexpr();
        if (ex != null) {
            if (accept((NanoMorphoLexer.OPNAME_AND))) {
                Object[] ex2 = andexpr();
                if (ex2 == null) parseError();
                return new Object[] {"AND", ex, ex2};
            }
            return ex;
        }
        return null;
    }

    /* 
        notexpr		=	'!', notexpr | binopexpr1
     */
    private Object[] notexpr() {
        Object[] ex = null;
        if (accept(NanoMorphoLexer.OPNAME_NOT)) {
            ex = notexpr();
            if (ex == null) parseError();
            return new Object[] {"NOT", ex};
        }

        ex = binopexpr1();
        if (ex != null) return ex;

        return null;
    }

    /* 
        binopexpr1	=	binopexpr2, { OPNAME1 binopexpr2 }
     */
    private Object[] binopexpr1() {
        Object[] ex = binopexpr2();
        if (ex != null) {
            while (accept(NanoMorphoLexer.OPNAME1)) {
                String op = lastLexeme;
                Object[] ex2 = binopexpr2();
                if (ex2 == null) parseError();
                Object[] args = new Object[] {ex, ex2};
                ex = new Object[] {"CALL", op, args};
            }
            return ex;
        }
        return null;
    }

    /* 
        binopexpr2	=	binopexpr3, [ OPNAME2, binopexpr2 ]
     */
    private Object[] binopexpr2() {
        Object[] ex = binopexpr3();
        if (ex != null) {
            if (accept(NanoMorphoLexer.OPNAME2)) {
                Object[] ex2 = binopexpr2();
                if (ex2 == null) parseError();
                Object[] args = new Object[] { ex, ex2};
                ex = new Object[] {"CALL", ":", args};
            }
            return ex;
        }
        return null;
    }
    
    /* 
        binopexpr3 = binopexpr4, { OPNAME3, binopexpr4 }
     */
    private Object[] binopexpr3() {
        Object[] ex = binopexpr4();
        if (ex != null) {
            while(accept(NanoMorphoLexer.OPNAME3)) {
                String op = lastLexeme;
                Object[] ex2 = binopexpr4();
                if (ex2 == null) parseError();
                Object[] args = {ex, ex2};
                ex = new Object[] {"CALL", op, args};
            }
            return ex;
        }
        return null;
    }

    /* 
        binopexpr4	=	binopexpr5, { OPNAME4, binopexpr5 }
     */
    private Object[] binopexpr4() {
        Object[] ex = binopexpr5();
        if (ex != null) {
            while(accept(NanoMorphoLexer.OPNAME4)) {
                String op = lastLexeme;
                Object[] ex2 = binopexpr5();
                if (ex2 == null) parseError();
                Object[] args = new Object[] {ex, ex2};
                ex = new Object[] {"CALL", op, args};
            }
            return ex;
        }
        return null;
    }

    /*
        binopexpr5	=	binopexpr6, { OPNAME5, binopexpr6 }
    */
    private Object[] binopexpr5() {
        Object[] ex = binopexpr6();
        if (ex != null) {
            while(accept(NanoMorphoLexer.OPNAME5)) {
                String op = lastLexeme;
                Object[] ex2 = binopexpr6();
                if (ex2 == null) parseError();
                Object[] args = new Object[] {ex, ex2};
                ex = new Object[] {"CALL", op, args};
            }
            return ex;
        }
        return null;
    }

    /*
        binopexpr6	=	binopexpr7, { OPNAME6, binopexpr7 }
    */
    private Object[] binopexpr6() {
        Object[] ex = binopexpr7();
        if (ex != null) {
            while(accept(NanoMorphoLexer.OPNAME6)) {
                String op = lastLexeme;
                Object[] ex2 = binopexpr7();
                if (ex2 == null) parseError();
                Object[] args = new Object[] {ex, ex2};
                ex = new Object[] {"CALL", op, args};
            }
            return ex;
        }
        return null;
    }

    /*
        binopexpr7	=	smallexpr, { OPNAME7, smallexpr }
    */
    private Object[] binopexpr7() {
        Object[] ex = smallexpr();
        if (ex != null) {
            while(accept(NanoMorphoLexer.OPNAME7)) {
                String op = lastLexeme;
                Object[] ex2 = smallexpr();
                if (ex2 == null) parseError();
                Object[] args = new Object[] {ex, ex2};
                ex = new Object[] {"CALL", op, args};
            }
            return ex;
        }
        return null;
    }

    /* 
        smallexpr	=	opname, smallexpr
                    | 	LITERAL
                    |	'(', expr, ')'
                    |	ifexpr
                    |	'while', '(', expr, ')', body
     */
    private Object[] smallexpr() {
        Object[] ex = smallexpr_2();
        if (ex != null) {
            return ex;
        }


        if (opname()) {
            String op = lastLexeme;
            ex = smallexpr();
            if (ex == null) parseError();
            Object[] args = new Object[] {ex};
            return new Object[] {"CALL", op, args};
        }

        if (accept(NanoMorphoLexer.LITERAL)) return new Object[] {"LITERAL", lastLexeme};

        if (accept(NanoMorphoLexer.DELIM, "(")) {
            ex = expr();
            if (ex ==null) parseError();
            expect(NanoMorphoLexer.DELIM, ")");
            return ex;
        }

        ex = ifexpr();
        if (ex != null) {
            return ex;
        }

        if (accept(NanoMorphoLexer.WHILE)) {
            expect(NanoMorphoLexer.DELIM, "(");
            ex = expr();
            if (ex == null) parseError();
            expect(NanoMorphoLexer.DELIM, ")");
            Object[] bod = body();
            if (bod == null) parseError();
            return new Object[] {"WHILE", ex, bod};
        }

        return null;
    }


    /* 
        smallexpr_name 	=   NAME
				        |	NAME, '(', [ expr, { ',', expr } ], ')'
                        |	NAME, '=', expr
     */
    private Object[] smallexpr_2() {
        if (accept(NanoMorphoLexer.NAME)) {
            Object[] ex = null;
            Vector<Object> args = new Vector<>();
            String name = lastLexeme;
            if (accept(NanoMorphoLexer.DELIM, "(")) {
                ex = expr();
                if (ex != null) {
                    args.add(ex);
                    while (accept(NanoMorphoLexer.DELIM, ",")) {
                        ex = expr();
                        if (ex == null) parseError();
                        args.add(ex);
                    }
                }
                expect(NanoMorphoLexer.DELIM, ")");

                return new Object[] {"CALL", name, args.toArray()};
            }
            
            else if (accept(NanoMorphoLexer.DELIM, "=")) {
                ex = expr();
                int pos = varPos(name);
                if (ex == null) parseError();
                return new Object[] {"STORE", pos, ex};
            }

            return new Object[] {"FETCH", varPos(name)};
        }

        return null;
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
    private Object[] ifexpr() {
        Object[] ifex = null;
        if (accept(NanoMorphoLexer.IF)) {
            expect(NanoMorphoLexer.DELIM, "(");
            Object[] ex1 = expr();
            if (ex1 == null) parseError();
            expect(NanoMorphoLexer.DELIM, ")");
            Object[] ex2 = body();
            if (ex2 == null) parseError();
            ifex = new Object[] {"IF", ex1, ex2, null};

            Object[] tail = ifex;
            while (accept(NanoMorphoLexer.ELSIF)) {
                expect(NanoMorphoLexer.DELIM, "(");
                ex1 = expr();
                if (ex1 == null) parseError();
                expect(NanoMorphoLexer.DELIM, ")");
                ex2 = body();
                if (ex2 == null) parseError();
                Object[] elif = new Object[] {"IF", ex1, ex2, null};
                tail[3] = elif;
                tail = elif;
            }

            if (accept(NanoMorphoLexer.ELSE)) {
                Object[] ex3 = body();
                if (ex3 == null) parseError();
                tail[3] = ex3;
            }

            return ifex;
        }
        return null;
    }

    /* 
        body = '{', { expr, ';' }, '}'
     */
    private Object[] body() {
        Vector<Object> exprs = new Vector<>();
        expect(NanoMorphoLexer.DELIM, "{");
        Object ex = expr();
        while (ex != null) { 
            exprs.add(ex);
            expect(NanoMorphoLexer.DELIM, ";");
            ex = expr();
        }
        expect(NanoMorphoLexer.DELIM, "}");

        return new Object[] {"BODY", exprs.toArray()};
    }


    /**
     * Þáttar forritstexta sem finnst í skjali @fileName
     * @param fileName
     * @return true ef gilt forrit
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Object[] parse(String fileName)throws FileNotFoundException, IOException{
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
