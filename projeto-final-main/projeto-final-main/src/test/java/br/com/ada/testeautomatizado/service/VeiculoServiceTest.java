package br.com.ada.testeautomatizado.service;

import br.com.ada.testeautomatizado.dto.VeiculoDTO;
import br.com.ada.testeautomatizado.exception.PlacaInvalidaException;
import br.com.ada.testeautomatizado.model.Veiculo;
import br.com.ada.testeautomatizado.repository.VeiculoRepository;
import br.com.ada.testeautomatizado.util.Response;
import br.com.ada.testeautomatizado.util.ValidacaoPlaca;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Spy
    private ValidacaoPlaca validacaoPlaca;

    @InjectMocks
    private VeiculoService veiculoService;

    @org.junit.Test
    public void cadastrarVeiculoComSucesso() {


        VeiculoDTO veiculoDTO = VeiculoDTO.builder()
                .placa("ABC-1234")
                .modelo("Uno")
                .marca("Fiat")
                .disponivel(true)
                .dataFabricacao(LocalDate.of(2020, 1, 1))
                .build();

        Veiculo veiculoSalvo = veiculoDTO.toEntity();

        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculoSalvo);


        validacaoPlaca.isPlacaValida(veiculoDTO.getPlaca());
        Veiculo veiculoCadastrado = veiculoService.cadastrar(veiculoDTO);



        assertThat(veiculoCadastrado, equalTo(veiculoSalvo));
        verify(validacaoPlaca).isPlacaValida(eq(veiculoCadastrado.getPlaca()));
        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @org.junit.Test
    public void testCadastrarPlacaInvalidaException() {
        VeiculoDTO veiculoDTO = VeiculoDTO.builder()
                .placa("123")
                .modelo("Modelo")
                .marca("Marca")
                .disponivel(true)
                .dataFabricacao(LocalDate.now())
                .build();

        Exception exception = Assertions.assertThrows(PlacaInvalidaException.class, () -> {
            validacaoPlaca.isPlacaValida(veiculoDTO.getPlaca());
        });

        String expectedMessage = "Placa inválida!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @org.junit.Test
    public void testDeletarVeiculoPelaPlaca() {
        VeiculoDTO veiculoDTO = VeiculoDTO.builder()
                .placa("123")
                .modelo("Modelo")
                .marca("Marca")
                .disponivel(true)
                .dataFabricacao(LocalDate.now())
                .build();

        Veiculo veiculo = veiculoDTO.toEntity();
        veiculoRepository.save(veiculo);


        Mockito.when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.ofNullable(veiculo));
        Mockito.when(veiculoRepository.deleteByPlaca("ABC1234")).thenReturn(veiculo);


        Veiculo veiculoDeletado = veiculoService.deletarVeiculoPelaPlaca("ABC1234");


        Mockito.verify(veiculoRepository, Mockito.times(1)).findByPlaca("ABC1234");
        Mockito.verify(veiculoRepository, Mockito.times(1)).deleteByPlaca("ABC1234");

        Assertions.assertEquals(veiculo, veiculoDeletado);
    }

}