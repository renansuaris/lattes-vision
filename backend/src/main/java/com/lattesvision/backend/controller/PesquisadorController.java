package com.lattesvision.backend.controller;

import com.lattesvision.backend.model.Pesquisador;
import com.lattesvision.backend.repository.PesquisadorRepository;
import com.lattesvision.backend.service.ScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/lattes")
@CrossOrigin(origins = "*")
public class PesquisadorController {

    private static final Pattern PADRAO_ID_LATTES = Pattern.compile("^\\d{16}$");

    @Autowired
    private ScraperService scraperService;

    @Autowired
    private PesquisadorRepository repository;

    @GetMapping("/{id}")
    public ResponseEntity<Pesquisador> buscarPesquisador(
            @PathVariable String id) {
        validarIdLattes(id);
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pesquisador nao encontrado para o ID informado."
                ));
    }

    @GetMapping
    public ResponseEntity<List<Pesquisador>> listarTodos() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping("/sincronizar/{id}")
    public ResponseEntity<Pesquisador> sincronizarUm(
            @PathVariable String id) {
        validarIdLattes(id);
        Pesquisador pesquisadorSalvo = scraperService.sincronizarPesquisador(id);
        return ResponseEntity.ok(pesquisadorSalvo);
    }

    @PostMapping("/sincronizar-lote")
    public ResponseEntity<List<Pesquisador>> sincronizarLote(
            @RequestBody List<String> ids) {
        validarLote(ids);
        List<Pesquisador> resultados = scraperService.sincronizarLote(ids);
        return ResponseEntity.ok(resultados);
    }

    private void validarLote(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A lista de IDs Lattes nao pode ser vazia.");
        }

        ids.forEach(this::validarIdLattes);
    }

    private void validarIdLattes(String id) {
        if (id == null || !PADRAO_ID_LATTES.matcher(id).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID Lattes invalido. Use exatamente 16 digitos numericos.");
        }
    }
}
