
package lucassena.CadastroDeNinjas.missoes;

import jakarta.persistence.EntityManager;
import lucassena.CadastroDeNinjas.ninjas.NinjaModel;
import lucassena.CadastroDeNinjas.ninjas.NinjaRepository;
import lucassena.CadastroDeNinjas.util.Rank;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MissoesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MissoesRepository missoesRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private NinjaRepository ninjaRepository;

    @Test
    void deveListarMissoesComStatus200() throws Exception {

        // ARRANGE
        MissoesModel missao1 = criarMissao(
                "Missão de resgate",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        MissoesModel missao2 = criarMissao(
                "Missão de reconhecimento",
                Rank.A,
                StatusMissao.INATIVA
        );

        // ACT + ASSERT
        mockMvc.perform(get("/missoes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.length()").value(2))

                // Conferir a primeira missão pelo ID
                .andExpect(jsonPath(
                        "$[?(@.id == " + missao1.getId() + ")].nome"
                ).value(org.hamcrest.Matchers.hasItem("Missão de resgate")))

                // Conferir a segunda missão pelo ID
                .andExpect(jsonPath(
                        "$[?(@.id == " + missao2.getId() + ")].nome"
                ).value(org.hamcrest.Matchers.hasItem("Missão de reconhecimento")));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremMissoes() throws Exception {

        // ARRANGE
        assertEquals(0, missoesRepository.count());

        // ACT + ASSERT
        mockMvc.perform(get("/missoes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(content().json("[]"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveBuscarMissaoPorIdComStatus200() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão de proteção",
                Rank.A,
                StatusMissao.DISPONIVEL
        );

        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(get("/missoes/{id}", missaoId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").value(missaoId))
                .andExpect(jsonPath("$.nome")
                        .value("Missão de proteção"))
                .andExpect(jsonPath("$.rank").value("A"))
                .andExpect(jsonPath("$.status").value("DISPONIVEL"));
    }

    @Test
    void naoDeveBuscarMissaoPorIdInexistente() throws Exception {

        // ARRANGE
        Long missaoIdInexistente = missoesRepository.findAll()
                .stream()
                .map(MissoesModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(
                missoesRepository.existsById(missaoIdInexistente)
        );

        long quantidadeAntes = missoesRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        get("/missoes/{id}", missaoIdInexistente)
                )
                .andExpect(status().isNotFound());

        // ASSERT - Verificar que nenhum registro foi criado
        assertEquals(
                quantidadeAntes,
                missoesRepository.count()
        );
    }


    @Test
    void deveCriarMissaoComStatus201() throws Exception {

        // ARRANGE
        MissoesRequestDTO dto = new MissoesRequestDTO(
                "Missão de resgate",
                Rank.B
        );

        long quantidadeAntes = missoesRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        post("/missoes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Missão de resgate"))
                .andExpect(jsonPath("$.rank").value("B"))
                .andExpect(jsonPath("$.status").value("DISPONIVEL"));

        // ASSERT - Confirmar persistência
        entityManager.flush();
        entityManager.clear();

        assertEquals(
                quantidadeAntes + 1,
                missoesRepository.count()
        );

        MissoesModel missaoSalva = missoesRepository.findAll()
                .stream()
                .filter(missao ->
                        missao.getNome().equals("Missão de resgate"))
                .findFirst()
                .orElseThrow();

        assertEquals(Rank.B, missaoSalva.getRank());
        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoSalva.getStatus()
        );
    }

    @Test
    void deveCriarMissaoRankSComStatusInicialDisponivel() throws Exception {

        // ARRANGE
        MissoesRequestDTO dto = new MissoesRequestDTO(
                "Missão de alto risco",
                Rank.S
        );

        // ACT + ASSERT
        mockMvc.perform(
                        post("/missoes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Missão de alto risco"))
                .andExpect(jsonPath("$.rank").value("S"))
                .andExpect(jsonPath("$.status").value("DISPONIVEL"));

        // ASSERT - Confirmar persistência
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoSalva = missoesRepository.findAll()
                .stream()
                .filter(missao ->
                        missao.getNome().equals("Missão de alto risco"))
                .findFirst()
                .orElseThrow();

        assertEquals(Rank.S, missaoSalva.getRank());
        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoSalva.getStatus()
        );
    }

    @Test
    void naoDeveCriarMissaoSemCamposObrigatorios() throws Exception {

        // ARRANGE
        String json = "{}";

        long quantidadeAntes = missoesRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        post("/missoes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.errors.nome").exists())
                .andExpect(jsonPath("$.errors.rank").exists());

        // ASSERT - Nenhuma missão deve ser criada
        assertEquals(
                quantidadeAntes,
                missoesRepository.count()
        );
    }

    @Test
    void naoDeveCriarMissaoComNomeInvalido() throws Exception {

        // ARRANGE
        MissoesRequestDTO dto = new MissoesRequestDTO(
                "AB",
                Rank.A
        );

        long quantidadeAntes = missoesRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        post("/missoes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.errors.nome")
                        .value("Nome deve ter entre 3 e 100 caracteres"));

        // ASSERT - Verificar que nada foi persistido
        entityManager.flush();
        entityManager.clear();

        assertEquals(
                quantidadeAntes,
                missoesRepository.count()
        );
    }


    @Test
    void deveAtualizarMissaoComStatus200() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão original",
                Rank.D,
                StatusMissao.DISPONIVEL
        );

        Long missaoId = missao.getId();

        MissoesRequestDTO dto = new MissoesRequestDTO(
                "Missão atualizada",
                Rank.A
        );

        // ACT + ASSERT
        mockMvc.perform(
                        put("/missoes/{id}", missaoId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(missaoId))
                .andExpect(jsonPath("$.nome").value("Missão atualizada"))
                .andExpect(jsonPath("$.rank").value("A"))
                .andExpect(jsonPath("$.status").value("DISPONIVEL"));

        // ASSERT - Confirmar persistência
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals("Missão atualizada", missaoAtualizada.getNome());
        assertEquals(Rank.A, missaoAtualizada.getRank());
        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );
    }

    @Test
    void naoDeveAtualizarMissaoInexistente() throws Exception {

        // ARRANGE
        Long missaoIdInexistente = missoesRepository.findAll()
                .stream()
                .map(MissoesModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(missoesRepository.existsById(missaoIdInexistente));

        MissoesRequestDTO dto = new MissoesRequestDTO(
                "Missão inexistente",
                Rank.B
        );

        long quantidadeAntes = missoesRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        put("/missoes/{id}", missaoIdInexistente)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());

        // ASSERT - Confirmar que nada foi criado
        entityManager.flush();
        entityManager.clear();

        assertEquals(quantidadeAntes, missoesRepository.count());
        assertFalse(missoesRepository.existsById(missaoIdInexistente));
    }

    @Test
    void naoDeveAtualizarMissaoComDadosInvalidos() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão original",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        Long missaoId = missao.getId();

        MissoesRequestDTO dto = new MissoesRequestDTO(
                "AB",
                null
        );

        // ACT + ASSERT
        mockMvc.perform(
                        put("/missoes/{id}", missaoId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Erro de validação"))
                .andExpect(jsonPath("$.errors.nome").exists())
                .andExpect(jsonPath("$.errors.rank").exists());

        // ASSERT - Garantir que nada foi alterado
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoSalva = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals("Missão original", missaoSalva.getNome());
        assertEquals(Rank.B, missaoSalva.getRank());
        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoSalva.getStatus()
        );
    }

    @Test
    void deveIniciarMissaoAoDiminuirRankExigidoViaHttp() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão de rank alto",
                Rank.A,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = new NinjaModel();
        ninja.setNome("Ninja Rank B");
        ninja.setEmail("controller.diminuir.rank@example.com");
        ninja.setIdade(25);
        ninja.setRank(Rank.B);
        ninja.setMissoes(missao);

        ninja = ninjaRepository.saveAndFlush(ninja);

        Long missaoId = missao.getId();
        Long ninjaId = ninja.getId();

        MissoesRequestDTO dto = new MissoesRequestDTO(
                "Missão com exigência reduzida",
                Rank.C
        );

        // ACT + ASSERT
        mockMvc.perform(
                        put("/missoes/{id}", missaoId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(missaoId))
                .andExpect(jsonPath("$.nome")
                        .value("Missão com exigência reduzida"))
                .andExpect(jsonPath("$.rank").value("C"))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));

        // ASSERT - Confirmar persistência
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertEquals(Rank.C, missaoAtualizada.getRank());
        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missaoAtualizada.getStatus()
        );

        // O vínculo deve ser preservado
        assertNotNull(ninjaAtualizado.getMissoes());
        assertEquals(
                missaoId,
                ninjaAtualizado.getMissoes().getId()
        );
    }

    @Test
    void deveDisponibilizarMissaoAoAumentarRankExigidoViaHttp() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão de exigência baixa",
                Rank.C,
                StatusMissao.EM_ANDAMENTO
        );

        NinjaModel ninja = new NinjaModel();
        ninja.setNome("Ninja Rank B");
        ninja.setEmail("controller.aumentar.rank@example.com");
        ninja.setIdade(25);
        ninja.setRank(Rank.B);
        ninja.setMissoes(missao);

        ninja = ninjaRepository.saveAndFlush(ninja);

        Long missaoId = missao.getId();
        Long ninjaId = ninja.getId();

        MissoesRequestDTO dto = new MissoesRequestDTO(
                "Missão com exigência maior",
                Rank.A
        );

        // ACT + ASSERT
        mockMvc.perform(
                        put("/missoes/{id}", missaoId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(missaoId))
                .andExpect(jsonPath("$.nome")
                        .value("Missão com exigência maior"))
                .andExpect(jsonPath("$.rank").value("A"))
                .andExpect(jsonPath("$.status").value("DISPONIVEL"));

        // ASSERT - Confirmar persistência
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertEquals(Rank.A, missaoAtualizada.getRank());
        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );

        // O ninja continua vinculado à missão
        assertNotNull(ninjaAtualizado.getMissoes());
        assertEquals(
                missaoId,
                ninjaAtualizado.getMissoes().getId()
        );
    }


    @Test
    void deveExcluirMissaoSemNinjasComStatus204() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão para exclusão",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        Long missaoId = missao.getId();

        long quantidadeAntes = missoesRepository.count();

        assertTrue(missoesRepository.existsById(missaoId));
        assertFalse(ninjaRepository.existsByMissoes_Id(missaoId));

        // ACT + ASSERT
        mockMvc.perform(delete("/missoes/{id}", missaoId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // ASSERT - Confirmar exclusão no banco
        entityManager.flush();
        entityManager.clear();

        assertFalse(missoesRepository.existsById(missaoId));

        assertEquals(
                quantidadeAntes - 1,
                missoesRepository.count()
        );
    }

    @Test
    void naoDeveExcluirMissaoComNinjaVinculado() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão com ninja vinculado",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = new NinjaModel();
        ninja.setNome("Ninja Vinculado");
        ninja.setEmail("exclusao.controller@example.com");
        ninja.setIdade(25);
        ninja.setRank(Rank.D);
        ninja.setMissoes(missao);

        ninja = ninjaRepository.saveAndFlush(ninja);

        Long missaoId = missao.getId();
        Long ninjaId = ninja.getId();

        long quantidadeAntes = missoesRepository.count();

        // ACT + ASSERT
        mockMvc.perform(delete("/missoes/{id}", missaoId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "Não é possível excluir uma missão que possui ninjas vinculados"
                ));

        // ASSERT - Confirmar que nada foi excluído
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoSalva = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoSalva.getStatus()
        );

        assertNotNull(ninjaSalvo.getMissoes());
        assertEquals(
                missaoId,
                ninjaSalvo.getMissoes().getId()
        );

        assertEquals(
                quantidadeAntes,
                missoesRepository.count()
        );
    }

    @Test
    void naoDeveExcluirMissaoInexistente() throws Exception {

        // ARRANGE
        Long missaoIdInexistente = missoesRepository.findAll()
                .stream()
                .map(MissoesModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(missoesRepository.existsById(missaoIdInexistente));

        long quantidadeAntes = missoesRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        delete("/missoes/{id}", missaoIdInexistente)
                )
                .andExpect(status().isNotFound());

        // ASSERT - Nenhum registro deve ser excluído
        entityManager.flush();
        entityManager.clear();

        assertEquals(
                quantidadeAntes,
                missoesRepository.count()
        );

        assertFalse(
                missoesRepository.existsById(missaoIdInexistente)
        );
    }


    @Test
    void deveAtivarMissaoInativaComStatus200() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão para ativação",
                Rank.B,
                StatusMissao.INATIVA
        );

        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(patch("/missoes/{id}/ativar", missaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(missaoId))
                .andExpect(jsonPath("$.nome")
                        .value("Missão para ativação"))
                .andExpect(jsonPath("$.rank").value("B"))
                .andExpect(jsonPath("$.status")
                        .value("DISPONIVEL"));

        // ASSERT - Confirmar persistência no banco
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtivada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtivada.getStatus()
        );

        assertEquals("Missão para ativação", missaoAtivada.getNome());
        assertEquals(Rank.B, missaoAtivada.getRank());
    }

    @Test
    void naoDeveAtivarMissaoJaDisponivel() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão já disponível",
                Rank.C,
                StatusMissao.DISPONIVEL
        );

        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(patch("/missoes/{id}/ativar", missaoId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Só é possível ativar uma missão inativa"));

        // ASSERT - Confirmar que o status não mudou
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoSalva = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoSalva.getStatus()
        );

        assertEquals("Missão já disponível", missaoSalva.getNome());
        assertEquals(Rank.C, missaoSalva.getRank());
    }

    @ParameterizedTest
    @EnumSource(
            value = StatusMissao.class,
            names = {"EM_ANDAMENTO", "CONCLUIDA"}
    )
    void naoDeveAtivarMissaoEmAndamentoOuConcluida(
            StatusMissao statusInicial) throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão com ativação proibida",
                Rank.A,
                statusInicial
        );

        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(patch("/missoes/{id}/ativar", missaoId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Só é possível ativar uma missão inativa"));

        // ASSERT - Confirmar que o status foi preservado
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoSalva = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(statusInicial, missaoSalva.getStatus());
        assertEquals(
                "Missão com ativação proibida",
                missaoSalva.getNome()
        );
        assertEquals(Rank.A, missaoSalva.getRank());
    }

    @Test
    void naoDeveAtivarMissaoInexistente() throws Exception {

        // ARRANGE - Encontrar um ID inexistente
        Long missaoIdInexistente = missoesRepository.findAll()
                .stream()
                .map(MissoesModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(
                missoesRepository.existsById(missaoIdInexistente)
        );

        long quantidadeAntes = missoesRepository.count();

        // ACT + ASSERT - Tentar ativar missão inexistente
        mockMvc.perform(
                        patch("/missoes/{id}/ativar", missaoIdInexistente)
                )
                .andExpect(status().isNotFound());

        // ASSERT - Confirmar que nada foi alterado
        entityManager.flush();
        entityManager.clear();

        assertFalse(
                missoesRepository.existsById(missaoIdInexistente)
        );

        assertEquals(
                quantidadeAntes,
                missoesRepository.count()
        );
    }


    @Test
    void deveRetornarContratoJsonCorretoAoConsultarMissao()
            throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão de reconhecimento",
                Rank.A,
                StatusMissao.DISPONIVEL
        );

        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(get("/missoes/{id}", missaoId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").value(missaoId))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome")
                        .value("Missão de reconhecimento"))
                .andExpect(jsonPath("$.nome").isString())
                .andExpect(jsonPath("$.rank").value("A"))
                .andExpect(jsonPath("$.rank").isString())
                .andExpect(jsonPath("$.status").value("DISPONIVEL"))
                .andExpect(jsonPath("$.status").isString());
    }

    @Test
    void naoDeveConsultarMissaoComIdEmFormatoInvalido()
            throws Exception {

        // ARRANGE
        String idInvalido = "abc";

        long quantidadeAntes = missoesRepository.count();

        // ACT + ASSERT
        mockMvc.perform(get("/missoes/{id}", idInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));

        // ASSERT
        assertEquals(
                quantidadeAntes,
                missoesRepository.count()
        );
    }
















    // METODO AUXILIAR
    private MissoesModel criarMissao(
            String nome,
            Rank rank,
            StatusMissao status) {

        MissoesModel missao = new MissoesModel();

        missao.setNome(nome);
        missao.setRank(rank);
        missao.setStatus(status);

        return missoesRepository.saveAndFlush(missao);
    }
}
