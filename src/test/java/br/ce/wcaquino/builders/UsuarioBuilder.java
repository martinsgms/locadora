package br.ce.wcaquino.builders;

import br.ce.wcaquino.entidades.Usuario;

public class UsuarioBuilder {

    public Usuario usuario;

    private UsuarioBuilder() {}
    
    public static UsuarioBuilder umUsuario() {
        UsuarioBuilder usuarioBuilder = new UsuarioBuilder();
        
        usuarioBuilder.usuario = new Usuario("gabriel");
        return usuarioBuilder;
    }
    
    public UsuarioBuilder comNome(String nome) {
        usuario.setNome(nome);
        return this;
    }
    
    public Usuario build() {
        return usuario;
    }
    
    
}
