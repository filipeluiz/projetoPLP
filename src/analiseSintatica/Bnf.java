package analiseSintatica;

import analiseLexico.Classification;
import analiseLexico.Lexic;
import analiseLexico.Token;

import java.io.FileInputStream;
import java.io.IOException;

public class Bnf {
    private Token lk;
    private final Lexic lexico;
    private final Repository table;
    private int scope;

    public Bnf(FileInputStream file) throws IOException {
        this.lexico = new Lexic(file);
        this.table = new Repository();
    }

    public void nextToken() throws IOException {
        this.lk = this.lexico.scan();
    }

    private int tipo() throws IOException {
        // <tipo> -> int | float | char | literal
        int type = -1;
        if((this.lk.getTypeToken() == Classification.INT.ordinal()) || (this.lk.getTypeToken() == Classification.FLOAT.ordinal()) || (this.lk.getTypeToken() == Classification.CHAR.ordinal()) || this.lk.getTypeToken() == Classification.LITERAL.ordinal()){
            type = this.lk.getTypeToken();
            this.nextToken();
        }
        return type;
    }

    private Token id() throws IOException {
        Token lk = null;
        if(this.lk.getTypeToken() == Classification.ID.ordinal()){
            lk = this.lk;
            this.nextToken();
        }
        return lk;
    }

    public void programa() throws IOException {
        // <programa> -> int main"("")" <bloco>
        this.nextToken(); // vai para INT
        this.nextToken(); // Vai para MAIN
        this.nextToken(); // Vai para (
        this.nextToken(); // Vai para )
        this.nextToken(); // Vai para {

        this.bloco();
    }

    public void bloco() throws IOException {
        //<bloco> -> “{“ {<decl_var>}* {<comando>}* “}”
        this.nextToken(); // sai chave aberta

        while((this.lk.getTypeToken() == Classification.INT.ordinal()) ||
                (this.lk.getTypeToken() == Classification.FLOAT.ordinal()) ||
                (this.lk.getTypeToken() == Classification.CHAR.ordinal())) {
            this.declVar();
        }

        while((this.lk.getTypeToken() == Classification.ID.ordinal()) ||
                (this.lk.getTypeToken() == Classification.WHILE.ordinal()) ||
                (this.lk.getTypeToken() == Classification.FOR.ordinal()) ||
                (this.lk.getTypeToken() == Classification.IF.ordinal()) ||
                (this.lk.getTypeToken() == Classification.PRINTF.ordinal())) {
            this.comando();
        }

        this.nextToken(); // sai fecha chave
    }

    public void declVar() throws IOException{
        //<decl_var> -> <tipo> <id> {,<id>}* ";"
        Symbol symbols = new Symbol(); // Para guardar variáveis na memória(Repositório)
        int type = this.tipo();
        Token id = this.id();

        symbols.setType(type);
        symbols.setLexema(id.getLexema());
        symbols.setScope(this.scope);

        this.table.push(symbols);

        while(this.lk.getTypeToken() == Classification.VIRGULA.ordinal()) {
            this.nextToken(); // Sai virgula
            symbols = new Symbol();
            id = this.id();

            symbols.setType(type);
            symbols.setLexema(id.getLexema());
            symbols.setScope(this.scope);

            this.table.push(symbols);
        }

        if(this.lk.getTypeToken() == Classification.ATRIBUICAO.ordinal()) {
            this.nextToken(); // sai atribuição
            symbols.setValue(this.lk.getLexema());
            this.nextToken(); // sai valor
        }

        this.nextToken(); // Sai ponto virgula
    }

    public void comando() throws IOException {
        // <comando> -> <comando_básico> | <iteração> | if "("<expr_relacional>")" <comando> {else <comando>}? | printf "({<literal>,}?<expr_arit>)" ";"
        if (this.lk.getTypeToken() == Classification.ID.ordinal() || this.lk.getTypeToken() == Classification.ABERTACHAVES.ordinal()) {
            this.comandoBasico();
        }
        else if(this.lk.getTypeToken() == Classification.WHILE.ordinal() || this.lk.getTypeToken() == Classification.FOR.ordinal()) {
            this.iteracao();
        }
        else if(this.lk.getTypeToken() == Classification.IF.ordinal()) {
            boolean bool;
            this.nextToken(); // sai IF
            this.nextToken(); // sai parênteses aberta

            bool = this.expr_relacional();

            if(bool) {
                this.comando();
            }
            else {
                while(this.lk.getTypeToken() != Classification.ELSE.ordinal()){
                    this.nextToken();
                }
                this.nextToken();
                this.comando();
            }
        }
        else {
            // printf "("{<fator>,}?<expr_arit>")" ";"
            Symbol literal = null, exprArit = null;
            this.nextToken(); // sai printf
            this.nextToken(); // sai parênteses aberta

            if(this.lk.getTypeToken() == Classification.LITERAL.ordinal()) {
                literal = this.fator();
                if(this.lk.getTypeToken() == Classification.VIRGULA.ordinal()) {
                    this.nextToken(); // Sair virgula
                    exprArit = this.exprArit();
                }
                this.nextToken(); // sai parênteses fechada
            }
            else {
                exprArit = this.exprArit();
                this.nextToken(); // sai parênteses fechada
            }
            this.print(literal, exprArit);
            this.nextToken(); // sai ponto virgula
        }
    }

    public void comandoBasico() throws IOException{
        if(this.lk.getTypeToken() == Classification.ID.ordinal()) {
            this.atribuicao();
        }
        else {
            this.bloco();
        }
    }

    public void iteracao() {
        // Ter que pensar como resolver repetido das instruções
    }

    public boolean expr_relacional() throws IOException {
        // <expr_relacional> -> <expr_arit> <op_relacional> <expr_arit>
        Symbol expr1, expr2;
        String op = "";
        int a1, a2;
        boolean bool = false;

        expr1 = this.exprArit();

        op = this.opRelacional();

        expr2 = this.exprArit();
        a1 = Integer.parseInt(expr1.getValue().toString());
        a2 = Integer.parseInt(expr2.getValue().toString());

        if(op.equalsIgnoreCase(">=")) {
            bool = a1 >= a2;
        }
        else if(op.equalsIgnoreCase("<=")) {
            bool = a1 <= a2;
        }
        else if(op.equalsIgnoreCase(">")) {
            bool = a1 > a2;
        }
        else if(op.equalsIgnoreCase("<")) {
            bool = a1 < a2;
        }
        else if(op.equalsIgnoreCase("==")) {
            bool = a1 == a2;
        }
        else if(op.equalsIgnoreCase("!=")) {
            bool = a1 != a2;
        }

        this.nextToken(); // Sai parênteses
        return bool;
    }

    public String opRelacional() throws IOException {
        StringBuffer op = this.lk.getLexema();

        if(this.lk.getTypeToken() == Classification.MENOR.ordinal()) {
            op = this.lk.getLexema();
        }
        else if(this.lk.getTypeToken() == Classification.MENORIGUAL.ordinal()) {
            op = this.lk.getLexema();
        }
        else if(this.lk.getTypeToken() == Classification.MAIOR.ordinal()) {
            op = this.lk.getLexema();
        }
        else if(this.lk.getTypeToken() == Classification.MAIORIGUAL.ordinal()) {
            op = this.lk.getLexema();
        }
        else if(this.lk.getTypeToken() == Classification.DIFERENTE.ordinal()) {
            op = this.lk.getLexema();
        }
        else if(this.lk.getTypeToken() == Classification.IGUAL.ordinal()) {
            op = this.lk.getLexema();
        }

        this.nextToken(); // Sai operações

        return op.toString();
    }

    public void atribuicao() throws IOException {
        // <atribuição> -> <id> "=" <expr_arit> ";"
        Symbol a1, a2;

        // <id>
        a1 = new Symbol();
        a1.setLexema(this.id().getLexema());
        a1.setScope(this.scope);
        a1 = this.table.search(a1);

        // "="
        this.nextToken(); // Sai atribuição

        // <expr_arit>
        a2 = this.exprArit();

        if(this.table.compatible(a1, a2)) {
            a1.setValue(a2.getLexema());
        } else {
            System.out.println("Tipo incompatível" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
        }

        // ";"
        this.nextToken(); // Sai ponto virgula
    }

    public Symbol exprArit() throws IOException {
        // <expr_arit> -> <termo><expr_arit2>
        StringBuffer temporary;
        Symbol termo = null, exprArit2 = null;

        if((this.lk.getTypeToken() == Classification.ABERTAPARENTESES.ordinal()) || (this.lk.getTypeToken() == Classification.ID.ordinal()) ||
           (this.lk.getTypeToken() == Classification.TIPOINT.ordinal()) || (this.lk.getTypeToken() == Classification.TIPOFLOAT.ordinal()) ||
           (this.lk.getTypeToken() == Classification.TIPOCHAR.ordinal()) || (this.lk.getTypeToken() == Classification.LITERAL.ordinal())) {
            termo = this.termo();
        }

        if((this.lk.getTypeToken() == Classification.ADICAO.ordinal())) {
            exprArit2 = this.exprArit2();

            temporary = new StringBuffer();

            if(termo.getType() != Classification.LITERAL.ordinal()) {
                int valor1 = Integer.parseInt(termo.getValue().toString());
                int valor2 = Integer.parseInt(exprArit2.getValue().toString());
                int total = valor1 + valor2;

                temporary.append(total);

                termo.setLexema(temporary);
                termo.setValue(temporary);
            }
        }
        else if((this.lk.getTypeToken() == Classification.SUBTRACAO.ordinal())) {
            exprArit2 = this.exprArit2();

            temporary = new StringBuffer();
            int valor1 = Integer.parseInt(termo.getValue().toString());
            int valor2 = Integer.parseInt(exprArit2.getValue().toString());
            int total = valor1 - valor2;

            temporary.append(total);

            termo.setLexema(temporary);
            termo.setValue(temporary);
        }
        return termo;
    }

    public Symbol exprArit2() throws IOException {
        //<expr_arit2> -> "+" <termo> <expr_arit2> | "-" <termo> <expr_arit2> | e
        Symbol termo, exprArit2;
        StringBuffer temporary;

        if(this.lk.getTypeToken() == Classification.ADICAO.ordinal()) {
            this.nextToken(); // sai adição
            termo = this.termo();
            exprArit2 = this.exprArit2();

            if(exprArit2 != null) {
                temporary = new StringBuffer();
                int valor1 = Integer.parseInt(termo.getValue().toString());
                int valor2 = Integer.parseInt(exprArit2.getValue().toString());
                int total = valor1 + valor2;

                temporary.append(total);

                termo.setLexema(temporary);
                termo.setValue(temporary);
            }
        }
        else if(this.lk.getTypeToken() == Classification.SUBTRACAO.ordinal()) {
            this.nextToken(); // sai subtração
            termo = this.termo();
            exprArit2 = this.exprArit2();

            if(exprArit2 != null) {
                temporary = new StringBuffer();
                int valor1 = Integer.parseInt(termo.getValue().toString());
                int valor2 = Integer.parseInt(exprArit2.getValue().toString());
                int total = valor1 - valor2;

                temporary.append(total);

                termo.setLexema(temporary);
                termo.setValue(temporary);
            }
            else {
                temporary = new StringBuffer();
                int valor1 = Integer.parseInt(termo.getValue().toString());
                temporary.append(valor1);
                termo.setLexema(temporary);
                termo.setValue(temporary);
            }
        }
        else {
            return null;
        }

        return termo;
    }

    public Symbol termo() throws IOException {
        // <termo> -> <fator> <termo2>
        Symbol fator = null, termo2 = null;
        StringBuffer temporary;

        if((this.lk.getTypeToken() == Classification.ABERTAPARENTESES.ordinal()) || (this.lk.getTypeToken() == Classification.ID.ordinal()) ||
           (this.lk.getTypeToken() == Classification.TIPOFLOAT.ordinal()) || (this.lk.getTypeToken() == Classification.TIPOINT.ordinal()) ||
           (this.lk.getTypeToken() == Classification.TIPOCHAR.ordinal()) || (this.lk.getTypeToken() == Classification.LITERAL.ordinal())) {
            fator = this.fator();
        }

        if(this.lk.getTypeToken() == Classification.MULTIPLICACAO.ordinal()) {
            termo2 = this.termo2();
            temporary = new StringBuffer();
            int valor1 = Integer.parseInt(fator.getValue().toString());
            int valor2 = Integer.parseInt(termo2.getValue().toString());
            int total = valor1 * valor2;

            temporary.append(total);

            fator.setLexema(temporary);
            fator.setValue(temporary);
        }
        else if(this.lk.getTypeToken() == Classification.DIVISAO.ordinal()) {
            termo2 = this.termo2();
            temporary = new StringBuffer();
            int valor1 = Integer.parseInt(fator.getValue().toString());
            int valor2 = Integer.parseInt(termo2.getValue().toString());
            int total = valor1 / valor2;

            temporary.append(total);

            fator.setLexema(temporary);
            fator.setValue(temporary);
        }

        return fator;
    }

    public Symbol termo2() throws IOException {
        //<termo2> -> "*" <fator> <termo2> | "/" <fator> <termo2> | e
        Symbol fator, termo2;
        StringBuffer temporary;

        if(this.lk.getTypeToken() == Classification.MULTIPLICACAO.ordinal()) {
            this.nextToken(); // sai multiplicação
            fator = this.fator();
            termo2 = this.termo2();

            if(termo2 != null) {
                temporary = new StringBuffer();
                int valor1 = Integer.parseInt(fator.getValue().toString());
                int valor2 = Integer.parseInt(termo2.getValue().toString());
                int total = valor1 * valor2;

                temporary.append(total);

                fator.setLexema(temporary);
                fator.setValue(temporary);
            }
        }
        else if(this.lk.getTypeToken() == Classification.DIVISAO.ordinal()) {
            this.nextToken(); // sai divisão
            fator = this.fator();
            termo2 = this.termo2();

            if(termo2 != null) {
                temporary = new StringBuffer();
                int valor1 = Integer.parseInt(fator.getValue().toString());
                int valor2 = Integer.parseInt(termo2.getValue().toString());
                int total = valor1 / valor2;

                temporary.append(total);

                fator.setLexema(temporary);
                fator.setValue(temporary);
            }
        }
        else {
            return null;
        }
        return fator;
    }

    public Symbol fator() throws IOException {
        //<fator> -> "(" <expr_arit> ")" | <id> | <float> | <inteiro> | <char> | <literal>
        Symbol result = new Symbol();

        if(this.lk.getTypeToken() == Classification.ABERTAPARENTESES.ordinal()) {
            this.nextToken(); // sai parênteses aberta
            result = this.exprArit();
            this.nextToken(); // sai parênteses fecha
        }
        else if(this.lk.getTypeToken() == Classification.ID.ordinal()) {
            result.setLexema(this.id().getLexema());
            result.setScope(this.scope);

            result.setType(this.table.search(result).getType());
            result.setValue(this.table.search(result).getValue());
        }
        else if(this.lk.getTypeToken() == Classification.TIPOFLOAT.ordinal()) {
            result.setType(this.lk.getTypeToken());
            result.setLexema(this.lk.getLexema());
            result.setValue(this.lk.getLexema());
            result.setScope(this.scope);
            this.nextToken();
        }
        else if(this.lk.getTypeToken() == Classification.TIPOINT.ordinal()) {
            result.setType(this.lk.getTypeToken());
            result.setLexema(this.lk.getLexema());
            result.setValue(this.lk.getLexema());
            result.setScope(this.scope);
            this.nextToken();
        }
        else if(this.lk.getTypeToken() == Classification.TIPOCHAR.ordinal()) {
            result.setType(this.lk.getTypeToken());
            result.setLexema(this.lk.getLexema());
            result.setValue(this.lk.getLexema());
            result.setScope(this.scope);
            this.nextToken();
        }
        else if(this.lk.getTypeToken() == Classification.LITERAL.ordinal()) {
            result.setType(this.lk.getTypeToken());
            result.setLexema(this.lk.getLexema());
            result.setValue(this.lk.getLexema());
            result.setScope(this.scope);
            this.nextToken();
        }

        return result;
    }

    public void print(Symbol literal, Symbol exprArit) {
        if(literal != null && exprArit == null) {
            // Somente Literal
            checkNewLine(literal.getValue().toString());
        }
        else if(literal == null && exprArit != null) {
            // Somente exprArit
            System.out.println(exprArit.getValue().toString());
        }
        else {
            // printf("Literal \n total é: %f", exprArit);
            // printf("Literal %i", exprArit);
            String literals = literal.getValue().toString();
            String aux, a1;
            String type = literals.substring(literals.length() - 3, literals.length() - 1);
            if (type.equalsIgnoreCase("%f") || type.equalsIgnoreCase("%d") || type.equalsIgnoreCase("%i")) {
                aux = literals.replace(type, "");
            } else {
                aux = literals;
            }

            if (type.equalsIgnoreCase("%f")) {
                this.checkNewLine(aux);
                System.out.print(exprArit.getValue().toString() + ".000000");
            } else {
                this.checkNewLine(aux);
                System.out.print(exprArit.getValue().toString());
            }
        }

    }

    public void checkNewLine(String s) {
        if(s != null) {
            if (s.contains("\\n")) {
                String msg = s.substring(1, s.length() - 1);
                String newMsg = "";
                int indice = msg.indexOf("\\n");

                if (indice > 0) {
                    newMsg = msg.substring(0, indice);
                    msg = msg.substring(indice + 2);

                    System.out.println(newMsg);

                    while (msg.contains("\\n")) {
                        indice = msg.indexOf("\\n");
                        newMsg = msg.substring(0, indice);
                        msg = msg.substring(indice + 2, msg.length());
                        System.out.println(newMsg);
                    }
                }
                System.out.print(msg);
            }
            else {
                String msg = s.substring(1, s.length() - 1);
                System.out.print(msg);
            }
        }
    }
}
