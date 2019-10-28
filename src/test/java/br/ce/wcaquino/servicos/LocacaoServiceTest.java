package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.builders.FilmeBuilder.umFilme;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.caiNumaSegunda;
import static br.ce.wcaquino.matchers.MatchersProprios.ehHoje;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.LocacaoBuilder;
import br.ce.wcaquino.dao.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.matchers.MatchersProprios;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoServiceTest {

	@Rule
	public ErrorCollector errorColector = new ErrorCollector();
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@InjectMocks
	@Spy
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
		
	    doReturn(DataUtils.obterData(28, 10, 2019)).when(service).obterData();
	    
//	    PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(DataUtils.obterData(28, 10, 2019));
		
		Locacao locacao = service.alugarFilme(usuario, filmes);
		
		errorColector.checkThat(locacao.getValor(), is(equalTo(4.0)));
		
		errorColector.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), DataUtils.obterData(28, 10, 2019)), is(true));
		errorColector.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterData(29, 10, 2019)), is(true));
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
	public void deveDevolverNaSegundaAoAlugarNoSabado() throws Exception {
//	    O USO DO POWER MOCK DISPENSA O USO DESTAS PRÉ-CONDIÇÕESS
//		Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));
	    
	    doReturn(DataUtils.obterData(26, 10, 2019)).when(service).obterData();
	    
		Locacao retorno = service.alugarFilme(usuario, filmes);
		
		assertThat(retorno.getDataRetorno(), caiNumaSegunda());
		assertThat(DataUtils.isMesmaData(retorno.getDataRetorno(), DataUtils.obterData(28, 10, 2019)), is(true));
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
	
	@Test
	public void deveProrrogarUmaLocacao() {
	    Locacao locacao = LocacaoBuilder.umaLocacao().build();
	    
	    service.prorrogarLocacao(locacao, 3);
	    
	    ArgumentCaptor<Locacao> argC = ArgumentCaptor.forClass(Locacao.class);
	    verify(dao).salvar(argC.capture());
	    
	    Locacao novaLocacao = argC.getValue();
	    
	    errorColector.checkThat(novaLocacao.getDataRetorno(), MatchersProprios.ehDaquiNDias(3));
	    errorColector.checkThat(novaLocacao.getUsuario().getNome(), is("gabriel"));
	    errorColector.checkThat(novaLocacao.getFilmes(), is(locacao.getFilmes()));
	    errorColector.checkThat(novaLocacao.getValor(), is(12.00));
	    errorColector.checkThat(novaLocacao.getDataLocacao(), ehHoje());
    }
	
	@Test
    public void deveCalcularValorLocacao() throws Exception {

	    Class<LocacaoService> clazz = LocacaoService.class;
	    Method method = clazz.getDeclaredMethod("calcularValorLocacao", List.class);
        method.setAccessible(true);
        
        Double valor = (Double) method.invoke(service, filmes);
        
        assertThat(valor, is(4.00));
    }
//	public static void main(String[] args) {
//        new BuilderMaster().gerarCodigoClasse(Locacao.class);
//    }
}
