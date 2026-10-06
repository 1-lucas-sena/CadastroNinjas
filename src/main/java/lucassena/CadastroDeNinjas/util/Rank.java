package lucassena.CadastroDeNinjas.util;

public enum Rank {
    D,
    C,
    B,
    A,
    S;

    public boolean eMaiorOuIgual(Rank outro) {
        return this.ordinal() >= outro.ordinal();
    }
}