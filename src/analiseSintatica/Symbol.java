package analiseSintatica;

public class Symbol {
    private StringBuffer lexema;
    private StringBuffer value;
    private int type;
    private int scope;

    public Symbol(){}

    public Symbol(StringBuffer lx){
        this.lexema = lx;
    }

    public Symbol(StringBuffer lx, int type){
        this.lexema = lx;
        this.type = type;
    }

    public Symbol(StringBuffer lexema, StringBuffer value, int type) {
        this.lexema = lexema;
        this.value = value;
        this.type = type;
    }

    public StringBuffer getLexema() {
        return lexema;
    }

    public void setLexema(StringBuffer lexema) {
        this.lexema = lexema;
    }

    public StringBuffer getValue() {
        return value;
    }

    public void setValue(StringBuffer value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "lexema=" + lexema +
                ", value=" + value +
                ", type=" + type +
                ", scope=" + scope +
                '}';
    }
}
