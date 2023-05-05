package br.com.ada.testeautomatizado.service;

import br.com.ada.testeautomatizado.dto.VeiculoDTO;
import br.com.ada.testeautomatizado.model.Veiculo;
import br.com.ada.testeautomatizado.repository.VeiculoRepository;
import br.com.ada.testeautomatizado.util.ValidacaoPlaca;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ValidacaoPlaca validacaoPlaca;

    public Veiculo cadastrar(VeiculoDTO veiculoDTO) {
       Veiculo veiculo = veiculoDTO.toEntity();
       return  veiculoRepository.save(veiculo);
    }

    public Veiculo deletarVeiculoPelaPlaca(String placa) {
        veiculoRepository.findByPlaca(placa);
        return veiculoRepository.deleteByPlaca(placa);
    }

    public Veiculo atualizar(Long id, Veiculo veiculo) {
        veiculoRepository.findByPlaca(veiculo.getPlaca());
        return veiculoRepository.save(veiculo);
    }

    public List<Veiculo> listarTodos() {
        return veiculoRepository.findAll();
    }

    private Optional<Veiculo> buscarVeiculoPelaPlaca(String placa) {
        return this.veiculoRepository.findByPlaca(placa);
    }
    public Optional<Veiculo> acharPorId(Long id) {
        return veiculoRepository.findById(id);
    }
}

