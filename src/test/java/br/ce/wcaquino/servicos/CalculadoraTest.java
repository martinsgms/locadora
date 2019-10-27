package br.ce.wcaquino.servicos;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import br.ce.wcaquino.exceptions.NaoPodeDividirPorZeroException;

public class CalculadoraTest {
	
	private Calculadora calc;
	
	@Mock
	private Calculadora calcMock;
	@Spy
	private Calculadora calcSpy;
	
	
	@Before
	public void setup(){
		calc = new Calculadora();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void deveSomarDoisValores(){
		//cenario
		int a = 5;
		int b = 3;
		
		//acao
		int resultado = calc.somar(a, b);
		
		//verificacao
		Assert.assertEquals(8, resultado);
		
	}
	
	@Test
	public void deveSubtrairDoisValores(){
		//cenario
		int a = 8;
		int b = 5;
		
		//acao
		int resultado = calc.subtrair(a, b);
		
		//verificacao
		Assert.assertEquals(3, resultado);
		
	}
	
	@Test
	public void deveDividirDoisValores() throws NaoPodeDividirPorZeroException{
		//cenario
		int a = 6;
		int b = 3;
		
		//acao
		int resultado = calc.divide(a, b);
		
		//verificacao
		Assert.assertEquals(2, resultado);
	}
	
	@Test(expected = NaoPodeDividirPorZeroException.class)
	public void deveLancarExcecaoAoDividirPorZero() throws NaoPodeDividirPorZeroException{
		int a = 10;
		int b = 0;
		
		calc.divide(a, b);
	}
	
	@Test
	public void testMatcherMock() {
	    Calculadora calcMock = mock(Calculadora.class);
	    
	    ArgumentCaptor<Integer> argC = ArgumentCaptor.forClass(Integer.class);
	    
//	    utilizando matcher any
//	    when(calcMock.somar(eq(1), anyInt())).thenReturn(10);
	    
//	    capturando os argumentos passados 
	    when(calcMock.somar(argC.capture(), argC.capture())).thenReturn(10);
	    
	    assertEquals(10, calcMock.somar(1, 5));
//	    System.out.println(argC.getAllValues());
    }
	
	@Test
	public void deveTestarMockAndSpy() {
	    when(calcMock.somar(2, 1)).thenReturn(10);
	    
//	    when(calcSpy.somar(1, 1)).thenReturn(20);
	    doReturn(100).when(calcSpy).somar(1, 2);
	    doNothing().when(calcSpy).imprime();
	    
	    System.out.println(calcMock.somar(2, 2));
	    System.out.println(calcSpy.somar(1, 2));
	    
	    calcMock.imprime();
	    calcSpy.imprime();
	    
	}
	
}
