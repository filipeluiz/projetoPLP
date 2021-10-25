package analiseSintatica;

import analiseLexico.Classification;
import analiseLexico.Lexic;
import analiseLexico.Token;

import java.io.FileInputStream;
import java.io.IOException;

public class BNF {
    private Token lk, loop;
    private final Lexic lexico;
    private final Repository table;
    private Symbol symbols;
    private int scope;
    private boolean bool = true;

    public BNF(FileInputStream file) throws IOException {
        this.lexico = new Lexic(file);
        this.table = new Repository();
    }

    public void nextToken() throws IOException {
        this.lk = this.lexico.scan();
    }

    public void programa() throws IOException {
        // <programa> -> int main"("")" <bloco>

        this.nextToken(); // vai para INT
        this.nextToken(); // Vai para MAIN
        this.nextToken(); // Vai para (
        this.nextToken(); // Vai para )
        this.nextToken(); // Vai para {

        this.bloco(); // Dentro bloco
//        System.out.println(this.table.getTable());
    }

    private void bloco() throws IOException {
        //<bloco> -> “{“ {<decl_var>}* {<comando>}* “}”

        this.scope++; // Dentro de bloco, ou seja um escopo
        this.nextToken(); // Sai chave {

        while((this.lk.getTypeToken() == Classification.INT.ordinal()) ||
                (this.lk.getTypeToken() == Classification.FLOAT.ordinal()) ||
                (this.lk.getTypeToken() == Classification.CHAR.ordinal())) {
            this.declVar();
        }

        while(this.lk.getTypeToken() == Classification.ABERTACHAVES.ordinal() ||
                this.lk.getTypeToken() == Classification.ID.ordinal() ||
                this.lk.getTypeToken() == Classification.WHILE.ordinal() ||
                this.lk.getTypeToken() == Classification.DO.ordinal() ||
                this.lk.getTypeToken() == Classification.IF.ordinal()) {
            this.comando();
        }
        System.out.println(this.lk);
        while(this.lk.getTypeToken() == Classification.PRINTF.ordinal()) {
            this.print();
        }

        if(this.lk.getTypeToken() != Classification.FECHACHAVES.ordinal()) {
//            throw new ErroParserException("Não encontrado '}'", this.scanner.getLine(), this.scanner.getColumn());
            System.out.println("Não encontrado '}'" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
        }

        if(this.scope > 1){
            while(this.table.getTable().lastElement().getScope() == this.scope) {
                if(this.table.getTable().size() > 1) {
                    this.table.pop();
                }
                else {
                    break;
                }
            }
        }
        this.scope--;
        System.out.println(this.lk);
        this.nextToken();
        System.out.println(this.table.getTable());
    }

    private int tipo() throws IOException {
        int type = -1;
        if((this.lk.getTypeToken() == Classification.INT.ordinal()) || (this.lk.getTypeToken() == Classification.FLOAT.ordinal()) || (this.lk.getTypeToken() == Classification.CHAR.ordinal()) || this.lk.getTypeToken() == Classification.LITERAL.ordinal()){
            // sucesso
            type = this.lk.getTypeToken();
            this.nextToken();
        }
        return type;
    }

    private void id() throws IOException {
        if(this.lk.getTypeToken() == Classification.ID.ordinal()){
            this.nextToken();
        }
    }

    private void declVar() throws IOException {
        //<decl_var> -> <tipo> <id> {,<id>}* ";"
        // exemplo int x; ou int x,y,z;
        this.symbols = new Symbol();

        int type = this.tipo();
        this.symbols.setType(type);
        this.symbols.setScope(this.scope);
        this.symbols.setLexema(this.lk.getLexema());
        System.out.println(this.lk);
        this.id();
        System.out.println(this.lk);
        if(this.lk.getTypeToken() == Classification.VIRGULA.ordinal()) {
            this.table.push(this.symbols);
            while(this.lk.getTypeToken() == Classification.VIRGULA.ordinal()) {
                this.symbols = new Symbol();
                this.nextToken(); // Sai virgula
                this.symbols.setType(type);
                this.symbols.setLexema(this.lk.getLexema());
                this.symbols.setScope(this.scope);
                this.id();

                this.table.push(this.symbols);
            }
        }
        else {
            if(this.lk.getTypeToken() == Classification.ABERTACOLCHETES.ordinal()) {
                this.nextToken(); // Sai colchetes
                if(this.lk.getTypeToken() == Classification.TIPOINT.ordinal()) {
                    this.symbols.setValue(fator().getLexema());
                }
                this.nextToken(); // Sai colchetes
            }
            this.table.push(this.symbols);
        }

        if(this.lk.getTypeToken() == Classification.PONTOVIRGULA.ordinal()) {
            this.nextToken();
        }
    }

    private void comando() throws IOException {
        // <comando> -> <comando_básico> | <iteração> | if "("<expr_relacional>")" <comando> {else <comando>}?
        if(this.lk.getTypeToken() == Classification.ID.ordinal() || this.lk.getTypeToken() == Classification.ABERTACHAVES.ordinal()) {
            this.comandoBasico();
        }
        else if(this.lk.getTypeToken() == Classification.WHILE.ordinal() || this.lk.getTypeToken() == Classification.DO.ordinal()) {
            loop = this.lk;
            while(bool) {
                this.iteracao();
                if(bool) {
                    this.lk.setLexema(loop.getLexema());
                    this.lk.setTypeToken(loop.getTypeToken());
                }
            }
        }
        else if(this.lk.getTypeToken() == Classification.IF.ordinal()) {
            this.nextToken();
            if(this.lk.getTypeToken() != Classification.ABERTAPARENTESES.ordinal()){
//                throw new ErroParserException("Não encontrado '('", this.scanner.getLine(), this.scanner.getColumn());
                System.out.println("Não encontrado '('" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
            }
            this.nextToken();
            bool = this.exprRelacional();

            if(this.lk.getTypeToken() != Classification.FECHAPARENTESES.ordinal()){
//                throw new ErroParserException("Não encontrado ')'", this.scanner.getLine(), this.scanner.getColumn());
                System.out.println("Não encontrado ')'" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
            }
            this.nextToken();
            if(bool) {
                this.comando();
                while(this.lk.getTypeToken() != Classification.FECHACHAVES.ordinal()) {
                    this.nextToken();
                }
            }
            else {
                while(this.lk.getTypeToken() != Classification.ELSE.ordinal()) {
                    this.nextToken();
                }
                bool = true;
            }
            System.out.println(this.lk);
            if(this.lk.getTypeToken() == Classification.ELSE.ordinal()){
                this.nextToken();
                this.comando();
            }
            System.out.println(this.lk);
        } else {
//            throw new ErroParserException("A declaração tem corpo vazio.", this.scanner.getLine(), this.scanner.getColumn());
            System.out.println("A declaração tem corpo vazio." + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
        }
    }

    private void comandoBasico() throws IOException {
        // <comando_básico> -> <atribuição> | <bloco>
        if(this.lk.getTypeToken() == Classification.ID.ordinal()) {
            this.atribuicao();
        }
        else if(this.lk.getTypeToken() == Classification.ABERTACHAVES.ordinal()) {
            if(bool) {
                this.bloco();
            }
            else {
                nextToken();
            }
        }
        else {
//            throw new ErroParserException("Não encontrada a declaração", this.scanner.getLine(), this.scanner.getColumn());
            System.out.println("Não encontrada a declaração" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
        }
    }

    private void iteracao() throws IOException {
        // <iteração> -> while "("<expr_relacional>")" <comando> | do <comando> while "("<expr_relacional>")"";"

        if(this.lk.getTypeToken() == Classification.WHILE.ordinal()){
            this.nextToken();
            if(this.lk.getTypeToken() != Classification.ABERTAPARENTESES.ordinal()){
//                throw new ErroParserException("Não encontrado '('", this.scanner.getLine(), this.scanner.getColumn());
                System.out.println("Não encontrado '('" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
            }
            this.nextToken();
            bool = this.exprRelacional();

            if(this.lk.getTypeToken() != Classification.FECHAPARENTESES.ordinal()){
//                throw new ErroParserException("Não encontrado ')'", this.scanner.getLine(), this.scanner.getColumn());
                System.out.println("Não encontrado ')'" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
            }

            this.nextToken();

            if(bool) {
                this.comando();
            } else {
                while(this.lk.getTypeToken() != Classification.FECHACHAVES.ordinal()) {
                    this.nextToken();
                }
            }
        }
        else if(this.lk.getTypeToken() == Classification.DO.ordinal()) {
            this.nextToken();
            this.comando();
            if(this.lk.getTypeToken() != Classification.WHILE.ordinal()){
//                throw new ErroParserException("Não encontrado 'while'", this.scanner.getLine(), this.scanner.getColumn());
                System.out.println("Não encontrado 'while'" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
            }

            this.nextToken();
            if(this.lk.getTypeToken() != Classification.ABERTAPARENTESES.ordinal()){
//                throw new ErroParserException("Não encontrado '('", this.scanner.getLine(), this.scanner.getColumn());
                System.out.println("Não encontrado '('" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
            }
            this.nextToken();
            this.exprRelacional();

            if(this.lk.getTypeToken() != Classification.FECHAPARENTESES.ordinal()){
//                throw new ErroParserException("Não encontrado ')'", this.scanner.getLine(), this.scanner.getColumn());
                System.out.println("Não encontrado ')'" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
            }
            this.nextToken();
            if(this.lk.getTypeToken() != Classification.PONTOVIRGULA.ordinal()){
//                throw new ErroParserException("Não encontrado ';'", this.scanner.getLine(), this.scanner.getColumn());
                System.out.println("Não encontrado ';'" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
            }

            this.nextToken();
        }
    }

    private boolean exprRelacional() throws IOException {
        // <expr_relacional> -> <expr_arit> <op_relacional> <expr_arit>
        int a1 = Integer.parseInt(this.exprArit().getValue().toString());
        String op = this.opRelacional().getLexema().toString();
        int a2 = Integer.parseInt(this.exprArit().getValue().toString());

        if(op.equalsIgnoreCase(">=")) {
            return a1 >= a2;
        }
        else if(op.equalsIgnoreCase("<=")) {
            return a1 <= a2;
        }
        else if(op.equalsIgnoreCase(">")) {
            return a1 > a2;
        }
        else if(op.equalsIgnoreCase("<")) {
            return a1 < a2;
        }
        else if(op.equalsIgnoreCase("==")) {
            return a1 == a2;
        }
        else if(op.equalsIgnoreCase("!=")) {
            return a1 != a2;
        }

        return false;
    }

    private Token opRelacional() throws IOException {
        //<expr_relacional> -> <expr_arit> <op_relacional> <expr_arit>
        Token op = null;

        if(this.lk.getTypeToken() == Classification.IGUAL.ordinal()) {
            op = this.lk;
            this.nextToken();
        }
        else if(this.lk.getTypeToken() == Classification.DIFERENTE.ordinal()) {
            op = this.lk;
            this.nextToken();
        }
        else if(this.lk.getTypeToken() == Classification.MENOR.ordinal()) {
            op = this.lk;
            this.nextToken();
        }
        else if(this.lk.getTypeToken() == Classification.MAIOR.ordinal()) {
            op = this.lk;
            this.nextToken();
        }
        else if(this.lk.getTypeToken() == Classification.MENORIGUAL.ordinal()) {
            op = this.lk;
            this.nextToken();
        }
        else if(this.lk.getTypeToken() == Classification.MAIORIGUAL.ordinal()) {
            op = this.lk;
            this.nextToken();
        }
        else if(this.lk.getTypeToken() == Classification.FECHAPARENTESES.ordinal()) {
//            throw new ErroParserException("início ilegal de expressão.", this.scanner.getLine(), this.scanner.getColumn());
            System.out.println("Início ilegal de expressão.");
        }
        else {
//            throw new ErroParserException("Não pode ser convertido para booleano", this.scanner.getLine(), this.scanner.getColumn());
            System.out.println("Não pode ser convertido para booleano" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
        }
        return op;
    }

    private void atribuicao() throws IOException {
        // <atribuição> -> <id> "=" <expr_arit> ";"
        Symbol a1, a2;

        a1 = new Symbol();
        a1.setLexema(this.lk.getLexema());
        a1.setScope(this.scope);
        a1 = this.table.search(a1);

        this.id();
        this.nextToken(); // Sai atribuição

        a2 = this.exprArit();

        if(!this.table.compatible(a1, a2)) {
//            throw new ErroSemanticException("Tipo incompativel", erro, this.scanner.getLine(), this.scanner.getColumn());
            System.out.println("Tipo incompativel" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
        }

        a1.setValue(a2.getLexema());
        this.nextToken(); // Sai ponto virgula
    }

    private Symbol  exprArit() throws IOException {
        Symbol expr1, expr2;
        StringBuffer temporary = new StringBuffer();
        Token op;
        int value1, value2, total;

        expr1 = this.termo();

        while(this.lk.getTypeToken() == Classification.ADICAO.ordinal() || this.lk.getTypeToken() == Classification.SUBTRACAO.ordinal()) {
            op = this.lk;
            this.nextToken();
            expr2 = this.termo();

            expr1.setType(this.table.checkType(expr1, op.getTypeToken() ,expr2));

            if(expr1.getType() == -1) {
//                throw new ErroSemanticException("Variável não é compativel", erro, this.scanner.getLine(), this.scanner.getColumn());
                System.out.println("Variável não é compativel" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
            }
//            expr1.setLexema(this.lk.getLexema());

            if(op.getTypeToken() == Classification.ADICAO.ordinal()) {
                value1 = Integer.parseInt(expr1.getLexema().toString());
                value2 = Integer.parseInt(expr2.getLexema().toString());
                total = value1 + value2;
                temporary.append(total);
                expr1.setLexema(temporary);
                expr1.setValue(temporary);
            }
            else if(op.getTypeToken() == Classification.SUBTRACAO.ordinal()) {
                if(expr1.getValue() != null) {
                    value1 = Integer.parseInt(expr1.getValue().toString());
                }
                else {
                    value1 = Integer.parseInt(expr1.getLexema().toString());
                }

                if(expr2.getValue() != null) {
                    value2 = Integer.parseInt(expr2.getValue().toString());
                }
                else {
                    value2 = Integer.parseInt(expr2.getLexema().toString());
                }

                total = value1 - value2;
                temporary.append(total);
                expr1.setLexema(temporary);
                expr1.setValue(temporary);
            }
        }
        return expr1;
    }

    private Symbol termo() throws IOException {
        Symbol aux1, aux2;
        StringBuffer temporary = new StringBuffer();
//        StringBuffer erro;
        Token op;
        int type, value1, value2, total;

        aux1 = this.fator();

        while(this.lk.getTypeToken() == Classification.MULTIPLICACAO.ordinal() || this.lk.getTypeToken() == Classification.DIVISAO.ordinal()) {
            op = this.lk;
            this.nextToken();
//            erro = this.lk.getLexema();
            aux2 = this.fator();

            type = this.table.checkType(aux1,op.getTypeToken(),aux2);
            aux1.setType(type);

            if(aux1.getType() == -1) {
//                throw new ErroSemanticException("Variável não é compativel", erro, this.scanner.getLine(), this.scanner.getColumn());
                System.out.println("Variável não é compativel" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
            }

//            aux1.setLexema(this.lk.getLexema());

            if(op.getTypeToken() == Classification.MULTIPLICACAO.ordinal()) {
                value1 = Integer.parseInt(aux1.getLexema().toString());
                value2 = Integer.parseInt(aux2.getLexema().toString());
                total = value1 * value2;
                temporary.append(total);
                aux1.setLexema(temporary);
                aux1.setValue(temporary);
            }
            else if(op.getTypeToken() == Classification.DIVISAO.ordinal()) {
                value1 = Integer.parseInt(aux1.getLexema().toString());
                value2 = Integer.parseInt(aux2.getLexema().toString());
                total = value1 / value2;
                temporary.append(total);
                aux1.setLexema(temporary);
                aux1.setValue(temporary);
            }
        }
        return aux1;
    }

    private Symbol fator() throws IOException {
        // <fator> -> “(“ <expr_arit> “)” | <id> | <float> | <inteiro> | <char> | <literal> | <incremento> | <decremento>
        Symbol symbol = new Symbol();

        // “(“ <expr_arit> “)”
        if(this.lk.getTypeToken() == Classification.ABERTAPARENTESES.ordinal()) {
            this.nextToken();
            symbol = this.exprArit();
            if(this.lk.getTypeToken() != Classification.FECHAPARENTESES.ordinal()) {
//                throw new ErroParserException("Não encontrado ')'", this.scanner.getLine(), this.scanner.getColumn());
                System.out.println("Não encontrado ')'" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
            }
            this.nextToken();
        }
        else if(this.lk.getTypeToken() == Classification.ID.ordinal()) {
            symbol.setLexema(this.lk.getLexema());
            symbol.setScope(this.scope);
            if(this.table.search(symbol) == null) {
//                throw new ErroSemanticException("Variável não foi declarada", this.scanner.getLexema(),this.scanner.getLine(),this.scanner.getColumn());
                System.out.println("Variável não foi declarada" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
            }

            symbol.setType(this.table.search(symbol).getType());
            symbol.setValue(this.table.search(symbol).getValue());

            this.id();
        }
        else if(this.lk.getTypeToken() == Classification.TIPOFLOAT.ordinal()) {
            symbol.setType(this.lk.getTypeToken());
            symbol.setLexema(this.lk.getLexema());
            symbol.setValue(this.lk.getLexema());
            symbol.setScope(this.scope);
            this.nextToken();
        }
        else if(this.lk.getTypeToken() == Classification.TIPOINT.ordinal()) {
            symbol.setType(this.lk.getTypeToken());
            symbol.setLexema(this.lk.getLexema());
            symbol.setValue(this.lk.getLexema());
            symbol.setScope(this.scope);
            this.nextToken();
        }
        else if(this.lk.getTypeToken() == Classification.TIPOCHAR.ordinal()) {
            symbol.setType(this.lk.getTypeToken());
            symbol.setLexema(this.lk.getLexema());
            symbol.setScope(this.scope);
            this.nextToken();
        }
        else if(this.lk.getTypeToken() == Classification.LITERAL.ordinal()) {
            symbol.setType(this.lk.getTypeToken());
            symbol.setLexema(this.lk.getLexema());
            symbol.setScope(this.scope);
            this.nextToken();
        }
        else {
//            throw new ErroParserException("Não encontrado expressão", this.scanner.getLine(), this.scanner.getColumn());
            System.out.println("Não encontrado expressão" + " " + this.lk + " " + this.lexico.getLine() + " " + this.lexico.getColumn());
        }
        return symbol;
    }

    // print a tela
    private void print() throws IOException {
        if(this.lk.getTypeToken() == Classification.PRINTF.ordinal()) {
            this.nextToken(); // Sai printf
            this.nextToken(); // Sai (
            String literal = "";
            String value = "";
            // Code here
            if(this.lk.getTypeToken() == Classification.LITERAL.ordinal()) {
                literal = this.lk.getLexema().toString();
                this.nextToken(); //Sai Literal

                if(this.lk.getTypeToken() == Classification.VIRGULA.ordinal()) {
                    this.nextToken();
                    value = this.lk.getLexema().toString();
                }
                convertPrint(literal, value);
            }
            else {
                Symbol a1 = new Symbol();
                StringBuffer stringBuffer = new StringBuffer(this.lk.getLexema());
                a1.setLexema(stringBuffer);
                a1.setScope(this.scope);
                a1 = this.table.search(a1);
                convertPrint(literal, a1.getValue().toString());
            }
        }
    }

    private void convertPrint(String literal, String value) throws IOException {
        String aux, type;

        if (!literal.equalsIgnoreCase("") && value.equalsIgnoreCase("")) {
            // Com literal e sem value
            String msg = literal.substring(1, literal.length() - 1);
            String newMsg = "";
            int indice = msg.indexOf("\\n");

            if (indice > 0) {
                newMsg = msg.substring(0, indice);
                msg = msg.substring(indice + 2, msg.length());

                System.out.println(newMsg);

                while (msg.contains("\\n")) {
                    indice = msg.indexOf("\\n");
                    newMsg = msg.substring(0, indice);
                    msg = msg.substring(indice + 2, msg.length());
                    System.out.println(newMsg);
                }
            }
            System.out.println(msg);
        } else if (literal.equalsIgnoreCase("") && !value.equalsIgnoreCase("")) {
            // sem literal e com value
            System.out.print(value);
            this.nextToken();
        } else {
            // com literal e com value
            type = literal.substring(literal.length() - 3, literal.length() - 1);
            if (type.equalsIgnoreCase("%f") || type.equalsIgnoreCase("%d") || type.equalsIgnoreCase("%i")) {
                aux = literal.replace(type, "");
            } else {
                aux = literal;
            }

            aux = aux.substring(1, aux.length() - 1);

            Symbol a1 = new Symbol();
            StringBuffer stringBuffer = new StringBuffer(value);
            a1.setLexema(stringBuffer);
            a1.setScope(this.scope);
            a1 = this.table.search(a1);

            if (type.equalsIgnoreCase("%f")) {
                aux = aux + a1.getValue() + ".000000";
                System.out.print(aux);
            } else {
                System.out.print(aux + a1.getValue());
            }

            this.nextToken();
        }
        System.out.println(this.lk);
        this.nextToken();
        this.nextToken();
        this.nextToken();
    }


}
