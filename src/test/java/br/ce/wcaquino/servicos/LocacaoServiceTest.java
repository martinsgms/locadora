package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
import static br.ce.wcaquino.matchers.MatchersProprios.ehAmanha;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHoje;
import static br.ce.wcaquino.utils.DataUtils.isMesmaData;
import static br.ce.wcaquino.utils.DataUtils.obterDataComDiferencaDias;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.LocacaoBuilder;
import br.ce.wcaquino.dao.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoServiceTest {

	@Rule
	public ErrorCollector error = new ErrorCollector();
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@InjectMocks
	private LocacaoService service;
	
	@Mock
	private SPCService spcService;
	@Mock
	private LocacaoDAO dao;
	@Mock
	private EmailService emailService;

	private Usuario usuario;
	public List<Filme> filmes;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
	    
		usuario = umUsuario().build();
		filmes = Arrays.asList(umFilme().build());
	}
	
	@Test
	public void deveAlugarFilme() throws Exception {
		Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		//acao
		Locacao locacao = service.alugarFilme(usuario, filmes);
		
		//verificacao
		error.checkThat(locacao.getValor(), is(equalTo(4.0)));
		
		error.checkThat(isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
		error.checkThat(locacao.getDataLocacao(), ehHoje());
		
		error.checkThat(isMesmaData(locacao.getDataRetorno(), obterDataComDiferencaDias(1)), is(true));
		assertThat(locacao.getDataRetorno(), ehAmanha());
	}
	
	@Test(expected = FilmeSemEstoqueException.class)
	public void naoDeveAlugarFilmeSemEstoque() throws Exception{
		List<Filme> filmes = Arrays.asList(FilmeBuilder.umFilme().semEstoque().build());
		service.alugarFilme(usuario, filmes);
	}
	
	@Test
	public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException{

	    try {
			service.alugarFilme(null, filmes);
			Assert.fail();
		} catch (LocadoraException e) {
			assertThat(e.getMessage(), is("Usuario vazio"));
		}
	}

	@Test
	public void naoDeveAlugarFilmeSemFilme() throws FilmeSemEstoqueException, LocadoraException{
		
		exception.expect(LocadoraException.class);
		exception.expectMessage("Filme vazio");
		
		service.alugarFilme(usuario, null);
	}
	
	@Test
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws FilmeSemEstoqueException, LocadoraException{
		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
		
		
		Locacao retorno = service.alugarFilme(usuario, filmes);
		
		boolean ehSegunda = DataUtils.verificarDiaSemana(retorno.getDataRetorno(), Calendar.MONDAY);
		Assert.assertTrue(ehSegunda);
		
		
//		assertThat(retorno.getDataRetorno(), new DiaSemanaMatcher(Calendar.MONDAY));
//		assertThat(retorno.getDataRetorno(), caiem(Calendar.TUESDAY));
		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
	}
	
	@Test
	public void naoDeveAlugarFilmeParaNegativado() throws Exception {
	    
	    
	    exception.expect(LocadoraException.class);
	    exception.expectMessage("Usuário Negativado");
	    
	    when(spcService.possuiNegativacao(usuario)).thenReturn(true);
	    
	    service.alugarFilme(usuario, filmes);
	    
	    verify(spcService).possuiNegativacao(usuario);
    }
	
	@Test
	public void deveEnviarEmailParaLocacoesAtrasadas() {
	    List<Locacao> locacoes = Arrays.asList(LocacaoBuilder.umaLocacao().atrasada().build());
	    
	    when(dao.obterLocacoesPendentes()).thenReturn(locacoes);
	    
	    service.notificarAtrasos();
	    
	    verify(emailService).notificarAtraso(usuario);
	    verifyNoMoreInteractions(emailService);
    }
	
	@Test
	public void naoDeveEnviarEmailParaLocacoesEmDia() {
        List<Locacao> locacoes = Arrays.asList(LocacaoBuilder.umaLocacao().build());
        
        when(dao.obterLocacoesPendentes()).thenReturn(locacoes);
        
        service.notificarAtrasos();
        
        verify(emailService, never()).notificarAtraso(usuario);
        verifyNoMoreInteractions(emailService);
	}
	
	@Test
    public void devePercorrerListaNotificandoApenasUsuariosAtrasados() throws Exception {
        
        List<Locacao> locacoes = Arrays.asList(
                LocacaoBuilder.umaLocacao().comUsuario(usuario).atrasada().build(),
                LocacaoBuilder.umaLocacao().comUsuario(usuario).atrasada().build(),
                LocacaoBuilder.umaLocacao().comUsuario(usuario).build(),
                LocacaoBuilder.umaLocacao().comUsuario(usuario).atrasada().build()
                );
        
        when(dao.obterLocacoesPendentes()).thenReturn(locacoes);
        
        service.notificarAtrasos();
        
        verify(emailService, atLeast(3)).notificarAtraso(Mockito.any(Usuario.class));
        
    }
	
	@Test
	public void deveTratarErroSpc() throws Exception {
	    
	    exception.expect(LocadoraException.class);
	    exception.expectMessage("SPC fora de serviço");
	    
	    when(spcService.possuiNegativacao(usuario)).thenThrow(new Exception("SPC fora de serviço"));
	    service.alugarFilme(usuario, filmes);
    }
	
	
//	public static void main(String[] args) {
//        new BuilderMaster().gerarCodigoClasse(Locacao.class);
//    }
}
