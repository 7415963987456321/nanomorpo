import java.io.FileNotFoundException;
import java.io.IOException;

public class NanoMorphoCompiler {

    private int labelCount = 0;

    public int newLabel() {
        labelCount++;
        return labelCount;
    }

    public void emit(String line, Object... args) {
        System.out.println(String.format(line, args));
    }

    public void generateProgram(String name, Object[] program) {
        // program = {function, ....}
        emit("\"%s.mexe\" = main in", name);
        emit("!{{");
        for (int i = 0; i < program.length; i++)
            generateFunction((Object[]) program[i]);
        emit("}}*BASIS;");
    }

    public void generateFunction(Object[] f) {
        // f = {name, argcount, varcount, expressions}
        String name = (String) f[0];
        int argc = (Integer) f[1];
        int varc = (Integer) f[2];
        Object [] exprs = (Object[]) f[3];
        emit("#\"%s[f%d]\" =", name, argc);
        emit("[");
        if (varc > 0) {
            emit("(MakeVal null)");
            for (int i = 0; i < varc; i++)
                emit("(Push)");
        }
        for(int i = 0; i < exprs.length; i++)
            generateExpression((Object[]) exprs[i]);
        //Expressions
        emit("(Return)"); //Just in case;
        emit("];");
    }

    public void generateExpression(Object[] expr) {
        String code = (String) expr[0];
        switch(code) {
            case "FETCH":
                emit("(Fetch %d)", expr[1]);
                return;
            case "STORE":
                generateExpression((Object []) expr[2]);
                emit("(Store %d)", expr[1]);
                return;
            case "LITERAL":
                emit("(MakeVal %s)", (String) expr[1]);
                return;
            case "IF":
                int labElse = newLabel();
                int labEnd = newLabel();
                generateJump((Object[]) expr[1], 0, labElse);
                generateExpression((Object[]) expr[2]);
                emit("(Go %s)", labEnd);
                emit("_%d:", labElse);
                if (null != (Object) expr[3]) {
                    generateExpression((Object[]) expr[3]);
                }
                emit("_%d:", labEnd);
                return;
            case "WHILE":
                int loopLabel = newLabel();
                int endLabel = newLabel();
                emit("_%d:", loopLabel);
                generateExpression((Object[]) expr[1]);
                emit("(GoFalse _%d)", endLabel);
                generateExpression((Object[]) expr[2]);
                emit("(Go _%d)", loopLabel);
                emit("_%d:", endLabel);
                return;
            case "BODY":
                Object[] exprs = (Object[]) expr[1];
                for (int i = 0; i < exprs.length; i++) {
                    generateExpression((Object[]) exprs[i]);
                }
                return;
            case "CALL":
                Object[] args = (Object[]) expr[2];
                for(int i = 0; i < args.length; i++) {
                    if(i==0) generateExpression((Object[]) args[i]);
                    else generateExpressionP((Object[]) args[i]);
                }
                emit("(Call #\"%s[f%d]\" %d)", expr[1], args.length, args.length);
                return;
            case "RETURN":
                generateExpressionR((Object[]) expr[1]);
                return;
            case "AND":
                int lab = newLabel();
                generateExpression((Object[]) expr[1]);
                emit("(GoFalse _%d)", lab);
                generateExpression((Object[]) expr[2]);
                emit("_%d:", lab);
                return;
            case "OR":
                int lab2 = newLabel();
                generateExpression((Object[]) expr[1]);
                emit("(GoTrue _%d)", lab2);
                generateExpression((Object[]) expr[2]);
                emit("_%d:", lab2);
                return;
            case "NOT":
                generateExpression((Object[]) expr[1]);
                emit("(Not)");
                return;
            default:
                throw new Error(String.format("Unknown command code: %s", code));
        }
    }

    public void generateJump(Object[] expr, int labTrue, int labFalse) {
        String code = (String) expr[0];
        switch (code) {
            case "LITERAL":
                String literal = (String) expr[1];
                if (literal.equals("false") || literal.equals("null") ) {
                    if (labFalse != 0) emit("(Go _%d)", labTrue);
                    return;
                }
                if (labTrue != 0) emit("(Go _%d)", labTrue);
                return;
            default:
                generateExpression(expr);
                if (labTrue != 0) emit("(GoTrue _%d)", labTrue);
                if (labFalse !=0) emit("(GoFalse _%d)", labFalse);
        }
    }

    public void generateJumpP(Object[] expr, int labTrue, int labFalse) {
        String code = (String) expr[0];
        switch (code) {
            case "LITERAL":
                String literal = (String) expr[1];
                emit("(Push)");
                if (literal.equals("false") || literal.equals("null") ) {
                    if (labFalse != 0) emit("(Go _%d)", labTrue);
                    return;
                }
                if (labTrue != 0) emit("(Go _%d)", labTrue);
                return;
            default:
                generateExpressionP(expr);
                if (labTrue != 0) emit("(GoTrue _%d)", labTrue);
                if (labFalse !=0) emit("(GoFalse _%d)", labFalse);
        }
    }

    public void generateExpressionR(Object[] expr) {
        String code = (String) expr[0];
        switch(code) {
            case "FETCH":
                emit("(FetchR %d)", expr[1]);
                return;
            case "STORE":
                generateExpression((Object []) expr[2]);
                emit("(StoreR %d)", expr[1]);
                return;
            case "LITERAL":
                emit("(MakeValR %s)", (String) expr[1]);
                return;
            case "IF":
                int labElse = newLabel();
                int labEnd = newLabel();
                generateJump((Object[]) expr[1], 0, labElse);
                generateExpressionR((Object[]) expr[2]);
                emit("_%d:", labElse);
                if ((Object) expr[2] != null) {
                    generateExpressionR((Object[]) expr[3]);
                }
                return;
            case "CALL":
                Object[] args = (Object[]) expr[2];
                for(int i = 0; i < args.length; i++) {
                    if(i==0) generateExpression((Object[]) args[i]);
                    else generateExpressionP((Object[]) args[i]);
                }
                emit("(CallR #\"%s[f%d]\" %d)", expr[1], args.length, args.length);
                return;
            default:
                generateExpression(expr);
                emit("(Return)");
                return;
        }
    }

    public void generateExpressionP(Object[] expr) {
        String code = (String) expr[0];
        switch(code) {
            case "FETCH":
                emit("(FetchP %d)", expr[1]);
                return;
            case "STORE":
                generateExpression((Object []) expr[2]);
                emit("(StoreP %d)", expr[1]);
                return;
            case "LITERAL":
                emit("(MakeValP %s)", (String) expr[1]);
                return;
            case "IF":
                int labElse = newLabel();
                int labEnd = newLabel();
                generateJump((Object[]) expr[1], 0, labElse);
                generateExpression((Object[]) expr[2]);
                emit("(Go %s)", labEnd);
                emit("_%d:", labElse);
                if ((Object) expr[3] != null) {
                    generateExpression((Object[]) expr[3]);
                }
                emit("_%d:", labEnd);
                return;
            case "CALL":
                Object[] args = (Object[]) expr[2];
                for(int i = 0; i < args.length; i++) 
                    generateExpressionP((Object[]) args[i]);
                if (args.length == 0) emit("(Push)");
                emit("(Call #\"%s[f%d]\" %d)", expr[1], args.length, args.length);
                return;
        }
    }

    public static void main(String[] args) {
        NanoMorphoCompiler compiler = new NanoMorphoCompiler();
        NanoMorphoParser parser = new NanoMorphoParser();
        String name = args[0].substring(args[0].lastIndexOf("/")+1, args[0].lastIndexOf("."));

        try {
            Object[] program = parser.parse(args[0]);
            compiler.generateProgram(name, program);
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
        } catch (IOException e) {
            System.err.println("Error reading file");
        }
    }
}
