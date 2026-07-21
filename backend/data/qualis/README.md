# Base Qualis Periódicos 2021-2024

## Arquivo original

- Arquivo: `raw/qualis_periodicos_2021_2024_oficial.xlsx`
- Fonte: Plataforma Sucupira/CAPES
- Consulta: `CLASSIFICAÇÕES DE PERIÓDICOS QUADRIÊNIO 2021-2024`, sem filtros adicionais
- URL de consulta: <https://sucupira-legado.capes.gov.br/sucupira/public/consultas/coleta/veiculoPublicacaoQualis/listaConsultaGeralPeriodicos.jsf>
- Data do download: 2026-07-21
- Data interna de geração do XLSX: 2026-07-07T19:46:07Z
- SHA-256: `2899a8d6ef66cbc78602f408efd7e8ba907f0e3ae282802473b3fad964363bab`

O arquivo desta pasta deve ser preservado sem alterações como evidência da fonte usada na importação.

## Estrutura encontrada

O XLSX possui uma aba chamada `RelatorioQualis` e quatro colunas:

1. `ISSN`
2. `Título`
3. `Área de Avaliação`
4. `Estrato`

A coluna `Área de Avaliação` não identifica explicitamente a área-mãe. Um mesmo periódico aparece uma vez para cada área de avaliação associada a ele.

## Resultado da inspeção

- Linhas de dados: 171.154
- Áreas de avaliação: 50
- ISSNs distintos: 33.347
- ISSNs distintos com dígito verificador válido: 33.313
- ISSNs distintos com dígito verificador inválido: 34
- ISSNs vazios: 0
- Duplicatas exatas: 0
- ISSNs associados a mais de um estrato: 0
- ISSNs associados a mais de um título: 0
- Maior quantidade de áreas associadas a um ISSN: 50

Quantidade de periódicos distintos por estrato:

| Estrato | Quantidade |
| --- | ---: |
| A1 | 5.910 |
| A2 | 4.761 |
| A3 | 4.067 |
| A4 | 3.716 |
| B1 | 3.392 |
| B2 | 3.287 |
| B3 | 2.847 |
| B4 | 2.794 |
| C | 2.573 |

A base não possui registros `B5`, mas possui registros `C`. O estado "não classificado" deve permanecer diferente do estrato oficial `C`.

## Consequências para a importação

- A classificação pode ser deduplicada por ISSN normalizado e ciclo, pois não existem conflitos de título ou estrato para o mesmo ISSN.
- As 171.154 linhas não devem gerar 171.154 classificações. A tabela principal deve conter aproximadamente 33.347 registros.
- `areaMae` não pode ser preenchida a partir deste arquivo e não deve ser inferida.
- As áreas de avaliação podem ser preservadas em uma tabela associativa futura, caso sejam necessárias no produto.
- A validação do dígito verificador deve produzir aviso, não descarte automático, porque 34 ISSNs inválidos constam na fonte oficial.
- O estrato deve aceitar `A1`, `A2`, `A3`, `A4`, `B1`, `B2`, `B3`, `B4` e `C`. `B5` não faz parte desta base.

## Importação no backend

O backend possui um importador streaming baseado em Apache POI. A planilha é processada linha a linha e apenas os 33.347 periódicos deduplicados permanecem em memória antes da persistência.

A importação é automática somente quando o banco ainda não possui os 33.347 periódicos esperados. Basta iniciar o PostgreSQL e executar normalmente a aplicação pelo IntelliJ ou Maven.

O backend localiza o arquivo tanto quando o diretório de trabalho é `backend/` quanto quando é a raiz do repositório. Para desabilitar explicitamente a inicialização automática, use:

```bash
QUALIS_IMPORT_ENABLED=false mvn spring-boot:run
```

O processo:

1. valida o cabeçalho e a aba `RelatorioQualis`;
2. deduplica as linhas por ISSN normalizado;
3. interrompe a importação se o mesmo ISSN possuir título ou estrato conflitante;
4. insere registros novos e atualiza apenas registros alterados;
5. mantém os ISSNs oficiais com dígito verificador inválido, registrando a quantidade como aviso;
6. reclassifica os artigos que já estavam persistidos.

Nos startups seguintes, o backend verifica a quantidade persistida e ignora a leitura do XLSX quando a base está completa. Novas sincronizações classificam os artigos automaticamente por correspondência exata do ISSN.

### Regra de referência

A base 2021-2024 é aplicada como referência única a artigos de qualquer ano. O estrato representa a classificação do periódico nessa base, não uma classificação histórica do artigo no ano de publicação.
