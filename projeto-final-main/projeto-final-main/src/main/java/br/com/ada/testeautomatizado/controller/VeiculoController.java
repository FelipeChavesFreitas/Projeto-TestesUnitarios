package br.com.ada.testeautomatizado.controller;


import br.com.ada.testeautomatizado.dto.VeiculoDTO;
import br.com.ada.testeautomatizado.model.Veiculo;
import br.com.ada.testeautomatizado.service.VeiculoService;
import br.com.ada.testeautomatizado.util.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/veiculo")
public class VeiculoController {

    @Autowired
    private VeiculoService veiculoService;


    @GetMapping("/todos")
    public List<Veiculo> listarTodos(){
        return this.veiculoService.listarTodos();
    }

    @PostMapping("/")
    public Veiculo cadastrar(@RequestBody VeiculoDTO veiculoDTO) {
        return this.veiculoService.cadastrar(veiculoDTO);
    }

    @DeleteMapping("/{placa}")
    public Veiculo deletarVeiculoPelaPlaca(@PathVariable("placa") String placa) {
        return this.veiculoService.deletarVeiculoPelaPlaca(placa);
    }

    @PutMapping("{id}")
    public Veiculo atualizar(@PathVariable Long id, @RequestBody VeiculoDTO veiculoDTO) {
        Veiculo veiculo = veiculoDTO.toEntity();
        return this.veiculoService.atualizar(id, veiculo);
    }

}
