import java.io.FileNotFoundException;
import java.io.IOException;

public class NanoMorphoCompiler {

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
        emit("#\"%s [f%d]\" =", name, argc);
        emit("[");
        for(int i = 0; i < exprs.length; i++)
            generateExpressions((Object[]) exprs[i]);
        //Expressions
        emit("];");
    }

    public void generateExpressions(Object[] expression) {
        emit("EXPR");
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