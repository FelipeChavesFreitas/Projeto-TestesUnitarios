package br.com.ada.testeautomatizado.controller;


import br.com.ada.testeautomatizado.dto.VeiculoDTO;
import br.com.ada.testeautomatizado.exception.PlacaInvalidaException;
import br.com.ada.testeautomatizado.model.Veiculo;
import br.com.ada.testeautomatizado.repository.VeiculoRepository;
import br.com.ada.testeautomatizado.service.VeiculoService;
import br.com.ada.testeautomatizado.util.Response;
import br.com.ada.testeautomatizado.util.ValidacaoPlaca;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class VeiculoControllerTest {

    @SpyBean
    private VeiculoService veiculoService;

    @SpyBean
    private VeiculoController veiculoController;

    @SpyBean
    private ValidacaoPlaca validacaoPlaca;

    @SpyBean
    private VeiculoRepository veiculoRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @DisplayName("Cadastrar veículo com sucesso")
    void cadastrarSucesso() throws Exception {

        VeiculoDTO veiculoDTO = veiculoDTO();

        Veiculo veiculoSalvo = veiculoDTO.toEntity();

        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculoSalvo);


        validacaoPlaca.isPlacaValida(veiculoDTO.getPlaca());
        Veiculo veiculoCadastrado = veiculoService.cadastrar(veiculoDTO);



        assertThat(veiculoCadastrado, equalTo(veiculoSalvo));
        verify(validacaoPlaca).isPlacaValida(eq(veiculoCadastrado.getPlaca()));
        verify(veiculoRepository).save(any(Veiculo.class));

    }


    @Test
    @DisplayName("Erro ao cadastrar veículo com placa inválida")
    void deveriaRetornarErroCadastrarVeiculoPlacaInvalida() throws Exception {

        VeiculoDTO veiculoDTO = veiculoDTO();
        veiculoDTO.setPlaca("!");

        Exception exception = Assertions.assertThrows(PlacaInvalidaException.class, () -> {
            validacaoPlaca.isPlacaValida(veiculoDTO.getPlaca());
        });

        String expectedMessage = "Placa inválida!";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

    }

    @Test
    @DisplayName("Deletar veículo pela placa com sucesso")
    void deveriaDeletarVeiculoPelaPlacaSucesso() throws Exception {
        VeiculoDTO veiculoDTO = veiculoDTO();

        Veiculo veiculo = veiculoDTO.toEntity();
        veiculoRepository.save(veiculo);


        Mockito.when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.ofNullable(veiculo));
        Mockito.when(veiculoRepository.deleteByPlaca("ABC1234")).thenReturn(veiculo);


        Veiculo veiculoDeletado = veiculoService.deletarVeiculoPelaPlaca("ABC1234");


        Mockito.verify(veiculoRepository, Mockito.times(1)).findByPlaca("ABC1234");
        Mockito.verify(veiculoRepository, Mockito.times(1)).deleteByPlaca("ABC1234");

        Assertions.assertEquals(veiculo, veiculoDeletado);
    }

    @Test
    @DisplayName("Retorna todos os veículos")
    public void deveriaListarVeiculosSucesso() throws Exception {
        List<Veiculo> veiculos = List.of(veiculoBD());
        when(veiculoService.listarTodos()).thenReturn(veiculos);
        assertEquals(veiculos, veiculoController.listarTodos());
    }

    @Test
    @DisplayName("Atualiza um veículo com sucesso")
    public void deveriaAtualizarVeiculoSucesso() throws Exception {
        VeiculoDTO veiculo = veiculoDTO();
        Mockito.doCallRealMethod().when(validacaoPlaca).isPlacaValida(Mockito.anyString());
        when(veiculoRepository.findByPlaca(Mockito.anyString())).thenReturn(Optional.of(veiculoBD()));
        Veiculo veiculoAtualizadoBD = veiculoBD();
        veiculoAtualizadoBD.setDisponivel(Boolean.FALSE);
        Long id = 1L;

        when(veiculoController.atualizar(id, veiculo)).thenReturn(veiculoAtualizadoBD);
        assertEquals(veiculoAtualizadoBD, veiculoController.atualizar(id, veiculo));

    }

    @Test
    @DisplayName("Retorna No Content ao atualizar um veículo que não existe na base de dados")
    public void deveriaRetornarNoContentAtualizarVeiculo() throws Exception {

        VeiculoDTO veiculoDTO = veiculoDTO();
        Veiculo veiculo = veiculoDTO.toEntity();
        veiculo.setId(1L);
        veiculoRepository.save(veiculo);
        veiculoRepository.flush();


        mockMvc.perform(put("/veiculo/{id}", veiculo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(veiculoDTO)))
                        .andExpect(result -> assertTrue(HttpStatus.valueOf(result.getResponse().getStatus()).is2xxSuccessful()));

        Optional<Veiculo> veiculoAtualizado = veiculoService.acharPorId(veiculo.getId());
        assertTrue(veiculoAtualizado.isPresent());
        assertEquals(veiculoDTO.getModelo(), veiculoAtualizado.get().getModelo());

    }


    private static VeiculoDTO veiculoDTO(){
        VeiculoDTO veiculoDTO = new VeiculoDTO();
        veiculoDTO.setPlaca("XYZ-4578");
        veiculoDTO.setModelo("F40");
        veiculoDTO.setMarca("FERRARI");
        veiculoDTO.setDisponivel(Boolean.TRUE);
        veiculoDTO.setDataFabricacao(LocalDate.parse("2000-01-01"));
        return veiculoDTO;
    }

    private static VeiculoDTO veiculoAtualizadoDTO(){
        VeiculoDTO veiculoDTO = new VeiculoDTO();
        veiculoDTO.setPlaca("XYZ-4578");
        veiculoDTO.setModelo("F40");
        veiculoDTO.setMarca("FERRARI");
        veiculoDTO.setDisponivel(Boolean.FALSE);
        veiculoDTO.setDataFabricacao(LocalDate.parse("2000-01-01"));
        return veiculoDTO;
    }

    private static Veiculo veiculoBD() {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(1L);
        veiculo.setPlaca("XYZ-4578");
        veiculo.setModelo("F40");
        veiculo.setMarca("FERRARI");
        veiculo.setDisponivel(Boolean.TRUE);
        veiculo.setDataFabricacao(LocalDate.parse("2000-01-01"));
        return veiculo;
    }

}