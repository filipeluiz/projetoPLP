import analiseLexico.Lexic;
import analiseSintatica.BNF;

import java.io.FileInputStream;
import java.io.IOException;

public class Interpretador {
    public static void main(String[] args) throws IOException {
        FileInputStream file = new FileInputStream(args[0]);
//        Lexic lexico = new Lexic(file);
//
//        while(!lexico.EOF()) {
//            System.out.println(lexico.scan());
//        }

        BNF bnf = new BNF(file);
        bnf.programa();
    }
}
