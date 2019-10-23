package br.ce.wcaquino.builders;

import br.ce.wcaquino.entidades.Filme;

public class FilmeBuilder {

    private FilmeBuilder() {}
    
    private Filme filme;
    
    public static FilmeBuilder umFilme() {
        FilmeBuilder filmeBuilder = new FilmeBuilder();
        filmeBuilder.filme = new Filme("batman", 10, 4.00);
        return filmeBuilder;
    }
    
    public FilmeBuilder semEstoque() {
        filme.setEstoque(0);
        return this;
    }
    
    public Filme build() {
        return filme;
    }
}
