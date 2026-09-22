package com.lattesvision.backend.service;

import com.lattesvision.backend.dto.YearRange;
import com.lattesvision.backend.model.Artigo;
import com.lattesvision.backend.model.Banca;
import com.lattesvision.backend.model.CapituloLivro;
import com.lattesvision.backend.model.ClassificacaoQualis;
import com.lattesvision.backend.model.Livro;
import com.lattesvision.backend.model.Orientacao;
import com.lattesvision.backend.model.Pesquisador;
import com.lattesvision.backend.model.ProducaoTecnica;
import com.lattesvision.backend.model.Projeto;
import com.lattesvision.backend.model.TrabalhoEvento;
import com.lattesvision.backend.repository.PesquisadorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/** Gera exportações tabulares a partir dos dados já persistidos, sem sincronizar currículos. */
@Service
public class CsvExportService {

    private static final String[] HEADER = {
            "Pesquisador", "ID Lattes", "Categoria", "Tipo", "Ano", "Título", "Descrição", "Autores",
            "Pessoa relacionada", "Status", "Veículo/Evento/Periódico", "ISSN", "Qualis", "Status Qualis",
            "Ciclo Qualis", "Fonte Qualis", "Editora", "ISBN", "Edição", "Volume", "Páginas",
            "Livro de origem", "Organizadores", "Natureza"
    };

    private final PesquisadorRepository pesquisadorRepository;

    public CsvExportService(PesquisadorRepository pesquisadorRepository) {
        this.pesquisadorRepository = pesquisadorRepository;
    }

    @Transactional(readOnly = true)
    public void validarPesquisadorDisponivel(String idLattes) {
        buscarPesquisador(idLattes);
    }

    @Transactional(readOnly = true)
    public void validarGrupoDisponivel(List<String> idsLattes) {
        for (String idLattes : idsLattes.stream().distinct().toList()) {
            buscarPesquisador(idLattes);
        }
    }

    @Transactional(readOnly = true)
    public void exportarPesquisador(String idLattes, YearRange yearRange, Writer writer) throws IOException {
        escreverCabecalho(writer);
        escreverPesquisador(buscarPesquisador(idLattes), yearRange, writer);
    }

    @Transactional(readOnly = true)
    public void exportarGrupo(List<String> idsLattes, YearRange yearRange, Writer writer) throws IOException {
        escreverCabecalho(writer);
        for (String idLattes : idsLattes.stream().distinct().toList()) {
            escreverPesquisador(buscarPesquisador(idLattes), yearRange, writer);
        }
    }

    private Pesquisador buscarPesquisador(String idLattes) {
        return pesquisadorRepository.findById(idLattes)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pesquisador nao encontrado localmente para exportacao. Analise o curriculo antes de exportar."
                ));
    }

    private void escreverCabecalho(Writer writer) throws IOException {
        writer.write('\uFEFF');
        escreverLinha(writer, (Object[]) HEADER);
    }

    private void escreverPesquisador(Pesquisador pesquisador, YearRange yearRange, Writer writer) throws IOException {
        String nome = pesquisador.getNomeCompleto();
        String idLattes = pesquisador.getIdLattes();

        for (Artigo artigo : pesquisador.getArtigos()) {
            if (!yearRange.includes(artigo.getAno())) continue;
            ClassificacaoQualis qualis = artigo.getClassificacaoQualis();
            escreverLinha(writer, nome, idLattes, "Artigo em periódico", null, artigo.getAno(),
                    valorOu(artigo.getTitulo(), artigo.getCitacaoCompleta()), artigo.getCitacaoCompleta(), null,
                    null, null, artigo.getPeriodico(), artigo.getIssn(),
                    qualis != null ? qualis.getEstrato() : null, artigo.getStatusQualis(),
                    cicloQualis(qualis), qualis != null ? qualis.getFonte() : null,
                    null, null, null, null, null, null, null, null);
        }

        for (TrabalhoEvento trabalho : pesquisador.getTrabalhosEventos()) {
            if (!yearRange.includes(trabalho.getAno())) continue;
            escreverLinha(writer, nome, idLattes, "Trabalho em evento", trabalho.getTipo(), trabalho.getAno(),
                    trabalho.getTitulo(), null, trabalho.getAutores(), null, null, trabalho.getNomeEvento(),
                    null, null, null, null, null, null, null, null, null, null, null, null, null);
        }

        for (Orientacao orientacao : pesquisador.getOrientacoes()) {
            if (!yearRange.includes(orientacao.getAno())) continue;
            escreverLinha(writer, nome, idLattes, "Orientação", orientacao.getTipo(), orientacao.getAno(),
                    orientacao.getTituloTrabalho(), null, null, orientacao.getNomeOrientado(), orientacao.getStatus(),
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        }

        for (Projeto projeto : pesquisador.getProjetos()) {
            if (!yearRange.includes(projeto.getAno())) continue;
            escreverLinha(writer, nome, idLattes, "Projeto", projeto.getTipo(), projeto.getAno(), projeto.getTitulo(),
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        }

        for (Banca banca : pesquisador.getBancas()) {
            if (!yearRange.includes(banca.getAno())) continue;
            escreverLinha(writer, nome, idLattes, "Participação em banca", banca.getTipo(), banca.getAno(),
                    banca.getTituloTrabalho(), null, null, banca.getNomeCandidato(), null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        }

        for (ProducaoTecnica producao : pesquisador.getProducoesTecnicas()) {
            if (!yearRange.includes(producao.getAno())) continue;
            escreverLinha(writer, nome, idLattes, "Produção técnica", producao.getCategoria(), producao.getAno(),
                    producao.getTitulo(), producao.getDescricaoBruta(), producao.getAutoresTexto(), null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null, producao.getNatureza());
        }

        for (Livro livro : pesquisador.getLivros()) {
            if (!yearRange.includes(livro.getAno())) continue;
            escreverLinha(writer, nome, idLattes, "Livro", null, livro.getAno(), livro.getTitulo(),
                    livro.getDescricaoBruta(), livro.getAutores(), null, null, null, null, null, null, null, null,
                    livro.getEditora(), livro.getIsbn(), livro.getEdicao(), livro.getVolume(), livro.getPaginas(),
                    null, null, null);
        }

        for (CapituloLivro capitulo : pesquisador.getCapitulosLivros()) {
            if (!yearRange.includes(capitulo.getAno())) continue;
            escreverLinha(writer, nome, idLattes, "Capítulo de livro", null, capitulo.getAno(), capitulo.getTitulo(),
                    capitulo.getDescricaoBruta(), capitulo.getAutores(), null, null, null, null, null, null, null, null,
                    capitulo.getEditora(), capitulo.getIsbn(), capitulo.getEdicao(), capitulo.getVolume(), capitulo.getPaginas(),
                    capitulo.getTituloLivro(), capitulo.getOrganizadores(), null);
        }
    }

    private String cicloQualis(ClassificacaoQualis classificacao) {
        if (classificacao == null || classificacao.getCicloInicio() == null || classificacao.getCicloFim() == null) {
            return null;
        }
        return classificacao.getCicloInicio() + "-" + classificacao.getCicloFim();
    }

    private String valorOu(String principal, String alternativo) {
        return principal == null || principal.isBlank() ? alternativo : principal;
    }

    private void escreverLinha(Writer writer, Object... valores) throws IOException {
        for (int indice = 0; indice < valores.length; indice++) {
            if (indice > 0) writer.write(';');
            writer.write('"');
            writer.write(escapar(String.valueOf(valores[indice] == null ? "" : valores[indice])));
            writer.write('"');
        }
        writer.write(System.lineSeparator());
    }

    private String escapar(String valor) {
        String valorSeguro = valor;
        String semEspacosIniciais = valor.stripLeading();
        if (!semEspacosIniciais.isEmpty() && "=+-@".indexOf(semEspacosIniciais.charAt(0)) >= 0) {
            valorSeguro = "'" + valor;
        }
        return valorSeguro.replace("\"", "\"\"");
    }
}
