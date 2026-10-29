
package lucassena.CadastroDeNinjas.ninjaMissao;

import jakarta.persistence.EntityManager;
import lucassena.CadastroDeNinjas.missoes.MissoesModel;
import lucassena.CadastroDeNinjas.missoes.MissoesRepository;
import lucassena.CadastroDeNinjas.missoes.StatusMissao;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NinjaMissaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NinjaRepository ninjaRepository;

    @Autowired
    private MissoesRepository missoesRepository;

    @Autowired
    private EntityManager entityManager;

    // MÉTODOS AUXILIARES

    private NinjaModel criarNinja(
            String nome,
            String email,
            int idade,
            Rank rank) {

        NinjaModel ninja = new NinjaModel();
        ninja.setNome(nome);
        ninja.setEmail(email);
        ninja.setIdade(idade);
        ninja.setRank(rank);

        return ninjaRepository.saveAndFlush(ninja);
    }

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


    @Test
    void deveAtribuirMissaoANinjaQualificadoComStatus200() throws Exception {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Naruto Uzumaki",
                "naruto.atribuicao@example.com",
                25,
                Rank.A
        );

        MissoesModel missao = criarMissao(
                "Missão de resgate",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        Long ninjaId = ninja.getId();
        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(
                        patch(
                                "/ninjas/{ninjaId}/missoes/{missaoId}",
                                ninjaId,
                                missaoId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").value(ninjaId))
                .andExpect(jsonPath("$.nome").value("Naruto Uzumaki"))
                .andExpect(jsonPath("$.rank").value("A"))
                .andExpect(jsonPath("$.missaoId").value(missaoId));

        // ASSERT - Confirmar alterações persistidas
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertNotNull(ninjaAtualizado.getMissoes());

        assertEquals(
                missaoId,
                ninjaAtualizado.getMissoes().getId()
        );

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missaoAtualizada.getStatus()
        );
    }

    @Test
    void deveAtribuirMissaoANinjaNaoQualificadoSemIniciarMissao() throws Exception {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Konohamaru Sarutobi",
                "konohamaru.atribuicao@example.com",
                16,
                Rank.D
        );

        MissoesModel missao = criarMissao(
                "Missão de alto risco",
                Rank.A,
                StatusMissao.DISPONIVEL
        );

        Long ninjaId = ninja.getId();
        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(
                        patch(
                                "/ninjas/{ninjaId}/missoes/{missaoId}",
                                ninjaId,
                                missaoId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ninjaId))
                .andExpect(jsonPath("$.missaoId").value(missaoId));

        // ASSERT - Verificar persistência
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertNotNull(ninjaAtualizado.getMissoes());
        assertEquals(
                missaoId,
                ninjaAtualizado.getMissoes().getId()
        );

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );
    }

    @Test
    void naoDeveAtribuirMissaoANinjaQueJaPossuiMissao() throws Exception {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Sasuke Uchiha",
                "sasuke.atribuido@example.com",
                20,
                Rank.A
        );

        MissoesModel missaoAtual = criarMissao(
                "Missão atual",
                Rank.B,
                StatusMissao.EM_ANDAMENTO
        );

        MissoesModel novaMissao = criarMissao(
                "Nova missão",
                Rank.A,
                StatusMissao.DISPONIVEL
        );

        ninja.setMissoes(missaoAtual);
        ninjaRepository.saveAndFlush(ninja);

        Long ninjaId = ninja.getId();
        Long missaoAtualId = missaoAtual.getId();
        Long novaMissaoId = novaMissao.getId();

        // ACT + ASSERT
        mockMvc.perform(
                        patch(
                                "/ninjas/{ninjaId}/missoes/{missaoId}",
                                ninjaId,
                                novaMissaoId
                        )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Ninja já possui uma missão"));

        // ASSERT - O vínculo original deve continuar
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertNotNull(ninjaSalvo.getMissoes());
        assertEquals(
                missaoAtualId,
                ninjaSalvo.getMissoes().getId()
        );

        assertEquals(
                StatusMissao.DISPONIVEL,
                missoesRepository.findById(novaMissaoId)
                        .orElseThrow()
                        .getStatus()
        );
    }

    @Test
    void naoDeveAtribuirMissaoANinjaInexistente() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão disponível",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        Long missaoId = missao.getId();

        Long ninjaIdInexistente = ninjaRepository.findAll()
                .stream()
                .map(NinjaModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(
                ninjaRepository.existsById(ninjaIdInexistente)
        );

        // ACT + ASSERT
        mockMvc.perform(
                        patch(
                                "/ninjas/{ninjaId}/missoes/{missaoId}",
                                ninjaIdInexistente,
                                missaoId
                        )
                )
                .andExpect(status().isNotFound());

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        assertFalse(
                ninjaRepository.existsById(ninjaIdInexistente)
        );

        assertEquals(
                StatusMissao.DISPONIVEL,
                missoesRepository.findById(missaoId)
                        .orElseThrow()
                        .getStatus()
        );
    }

    @Test
    void naoDeveAtribuirMissaoInexistente() throws Exception {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Sakura Haruno",
                "sakura.missao.inexistente@example.com",
                19,
                Rank.B
        );

        Long ninjaId = ninja.getId();

        Long missaoIdInexistente = missoesRepository.findAll()
                .stream()
                .map(MissoesModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(
                missoesRepository.existsById(missaoIdInexistente)
        );

        // ACT + ASSERT
        mockMvc.perform(
                        patch(
                                "/ninjas/{ninjaId}/missoes/{missaoId}",
                                ninjaId,
                                missaoIdInexistente
                        )
                )
                .andExpect(status().isNotFound());

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertNull(ninjaSalvo.getMissoes());

        assertFalse(
                missoesRepository.existsById(missaoIdInexistente)
        );
    }

    @ParameterizedTest
    @EnumSource(
            value = StatusMissao.class,
            names = {"INATIVA", "CONCLUIDA"}
    )
    void naoDeveAtribuirMissaoInativaOuConcluida(StatusMissao statusInicial) throws Exception {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Hinata Hyuga",
                "hinata.atribuicao@example.com",
                19,
                Rank.A
        );

        MissoesModel missao = criarMissao(
                "Missão indisponível",
                Rank.B,
                statusInicial
        );

        Long ninjaId = ninja.getId();
        Long missaoId = missao.getId();

        String mensagemEsperada =
                statusInicial == StatusMissao.INATIVA
                        ? "Não é possível atribuir ninja a uma missão inativa"
                        : "Não é possível atribuir ninja a uma missão concluída";

        // ACT + ASSERT
        mockMvc.perform(
                        patch(
                                "/ninjas/{ninjaId}/missoes/{missaoId}",
                                ninjaId,
                                missaoId
                        )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value(mensagemEsperada));

        // ASSERT - Nenhum vínculo deve ser criado
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        MissoesModel missaoSalva = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertNull(ninjaSalvo.getMissoes());
        assertEquals(statusInicial, missaoSalva.getStatus());
    }


    @Test
    void deveRemoverMissaoDeNinjaVinculadoComStatus200() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão de reconhecimento",
                Rank.A,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = criarNinja(
                "Konohamaru Sarutobi",
                "konohamaru.remocao@example.com",
                18,
                Rank.D
        );

        ninja.setMissoes(missao);
        ninjaRepository.saveAndFlush(ninja);

        Long ninjaId = ninja.getId();
        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(delete("/ninjas/{ninjaId}/missao", ninjaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ninjaId))
                .andExpect(jsonPath("$.nome").value("Konohamaru Sarutobi"))
                .andExpect(jsonPath("$.missaoId").value(
                        org.hamcrest.Matchers.nullValue()
                ));

        // ASSERT - Verificar persistência
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertNull(ninjaAtualizado.getMissoes());

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );
    }

    @Test
    void deveDisponibilizarMissaoAoRemoverUltimoNinjaQualificado() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão de resgate",
                Rank.B,
                StatusMissao.EM_ANDAMENTO
        );

        NinjaModel ninja = criarNinja(
                "Naruto Uzumaki",
                "naruto.ultimo.qualificado@example.com",
                25,
                Rank.A
        );

        ninja.setMissoes(missao);
        ninjaRepository.saveAndFlush(ninja);

        Long ninjaId = ninja.getId();
        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(delete("/ninjas/{ninjaId}/missao", ninjaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ninjaId))
                .andExpect(jsonPath("$.missaoId").value(
                        org.hamcrest.Matchers.nullValue()
                ));

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertNull(ninjaAtualizado.getMissoes());

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );
    }

    @Test
    void deveManterMissaoEmAndamentoQuandoOutroNinjaEstaQualificado() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão em equipe",
                Rank.B,
                StatusMissao.EM_ANDAMENTO
        );

        NinjaModel ninjaRemovido = criarNinja(
                "Naruto Uzumaki",
                "naruto.equipe@example.com",
                25,
                Rank.A
        );

        NinjaModel ninjaRestante = criarNinja(
                "Sasuke Uchiha",
                "sasuke.equipe@example.com",
                25,
                Rank.S
        );

        ninjaRemovido.setMissoes(missao);
        ninjaRestante.setMissoes(missao);

        ninjaRepository.saveAndFlush(ninjaRemovido);
        ninjaRepository.saveAndFlush(ninjaRestante);

        Long ninjaRemovidoId = ninjaRemovido.getId();
        Long ninjaRestanteId = ninjaRestante.getId();
        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(
                        delete("/ninjas/{ninjaId}/missao", ninjaRemovidoId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ninjaRemovidoId))
                .andExpect(jsonPath("$.missaoId").value(
                        org.hamcrest.Matchers.nullValue()
                ));

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaSemMissao = ninjaRepository
                .findById(ninjaRemovidoId)
                .orElseThrow();

        NinjaModel ninjaComMissao = ninjaRepository
                .findById(ninjaRestanteId)
                .orElseThrow();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertNull(ninjaSemMissao.getMissoes());

        assertNotNull(ninjaComMissao.getMissoes());
        assertEquals(
                missaoId,
                ninjaComMissao.getMissoes().getId()
        );

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missaoAtualizada.getStatus()
        );
    }

    @Test
    void naoDeveRemoverMissaoDeNinjaSemVinculo() throws Exception {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Sakura Haruno",
                "sakura.sem.missao@example.com",
                19,
                Rank.B
        );

        Long ninjaId = ninja.getId();

        assertNull(ninja.getMissoes());

        // ACT + ASSERT
        mockMvc.perform(delete("/ninjas/{ninjaId}/missao", ninjaId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Ninja não possui uma missão"));

        // ASSERT - Confirmar que nenhum vínculo foi alterado
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertNull(ninjaSalvo.getMissoes());
        assertEquals("Sakura Haruno", ninjaSalvo.getNome());
    }

    @Test
    void naoDeveRemoverMissaoDeNinjaInexistente() throws Exception {

        // ARRANGE
        Long ninjaIdInexistente = ninjaRepository.findAll()
                .stream()
                .map(NinjaModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(
                ninjaRepository.existsById(ninjaIdInexistente)
        );

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        delete("/ninjas/{ninjaId}/missao", ninjaIdInexistente)
                )
                .andExpect(status().isNotFound());

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        assertFalse(
                ninjaRepository.existsById(ninjaIdInexistente)
        );

        assertEquals(
                quantidadeAntes,
                ninjaRepository.count()
        );
    }


    @Test
    void deveTransferirNinjaEntreMissoesComStatus200() throws Exception {

        // ARRANGE
        MissoesModel missaoOrigem = criarMissao(
                "Missão de origem",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        MissoesModel missaoDestino = criarMissao(
                "Missão de destino",
                Rank.C,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = criarNinja(
                "Naruto Uzumaki",
                "naruto.transferencia@example.com",
                25,
                Rank.A
        );

        ninja.setMissoes(missaoOrigem);
        ninjaRepository.saveAndFlush(ninja);

        Long ninjaId = ninja.getId();
        Long origemId = missaoOrigem.getId();
        Long destinoId = missaoDestino.getId();

        // ACT + ASSERT
        mockMvc.perform(
                        patch(
                                "/ninjas/{ninjaId}/transferencias/{novaMissaoId}",
                                ninjaId,
                                destinoId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ninjaId))
                .andExpect(jsonPath("$.missaoId").value(destinoId));

        // ASSERT - Verificar persistência
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        MissoesModel origemAtualizada = missoesRepository
                .findById(origemId)
                .orElseThrow();

        MissoesModel destinoAtualizada = missoesRepository
                .findById(destinoId)
                .orElseThrow();

        assertEquals(
                destinoId,
                ninjaAtualizado.getMissoes().getId()
        );

        assertEquals(
                StatusMissao.DISPONIVEL,
                origemAtualizada.getStatus()
        );

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                destinoAtualizada.getStatus()
        );
    }

    @Test
    void deveTransferirNinjaQuandoExisteOutroQualificadoNaOrigem() throws Exception {

        // ARRANGE
        MissoesModel origem = criarMissao(
                "Missão de origem",
                Rank.B,
                StatusMissao.EM_ANDAMENTO
        );

        MissoesModel destino = criarMissao(
                "Missão de destino",
                Rank.C,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninjaTransferido = criarNinja(
                "Naruto Uzumaki",
                "naruto.transferido@example.com",
                25,
                Rank.A
        );

        NinjaModel ninjaRestante = criarNinja(
                "Sasuke Uchiha",
                "sasuke.restante@example.com",
                25,
                Rank.S
        );

        ninjaTransferido.setMissoes(origem);
        ninjaRestante.setMissoes(origem);

        ninjaRepository.saveAndFlush(ninjaTransferido);
        ninjaRepository.saveAndFlush(ninjaRestante);

        Long ninjaId = ninjaTransferido.getId();
        Long ninjaRestanteId = ninjaRestante.getId();
        Long origemId = origem.getId();
        Long destinoId = destino.getId();

        // ACT + ASSERT
        mockMvc.perform(
                        patch(
                                "/ninjas/{ninjaId}/transferencias/{novaMissaoId}",
                                ninjaId,
                                destinoId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ninjaId))
                .andExpect(jsonPath("$.missaoId").value(destinoId));

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        NinjaModel transferido = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        NinjaModel restante = ninjaRepository
                .findById(ninjaRestanteId)
                .orElseThrow();

        assertEquals(destinoId, transferido.getMissoes().getId());
        assertEquals(origemId, restante.getMissoes().getId());

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(origemId)
                        .orElseThrow()
                        .getStatus()
        );

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(destinoId)
                        .orElseThrow()
                        .getStatus()
        );
    }

    @Test
    void naoDeveTransferirUltimoNinjaQualificadoDeMissaoEmAndamento() throws Exception {

        // ARRANGE
        MissoesModel origem = criarMissao(
                "Missão em andamento",
                Rank.B,
                StatusMissao.EM_ANDAMENTO
        );

        MissoesModel destino = criarMissao(
                "Nova missão",
                Rank.C,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = criarNinja(
                "Kakashi Hatake",
                "kakashi.ultimo@example.com",
                30,
                Rank.A
        );

        ninja.setMissoes(origem);
        ninjaRepository.saveAndFlush(ninja);

        Long ninjaId = ninja.getId();
        Long origemId = origem.getId();
        Long destinoId = destino.getId();

        // ACT + ASSERT
        mockMvc.perform(
                        patch(
                                "/ninjas/{ninjaId}/transferencias/{novaMissaoId}",
                                ninjaId,
                                destinoId
                        )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "Transferência não permitida: o ninja é o último qualificado da missão em andamento"
                ));

        // ASSERT - O vínculo original deve permanecer
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertEquals(origemId, ninjaSalvo.getMissoes().getId());

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(origemId)
                        .orElseThrow()
                        .getStatus()
        );

        assertEquals(
                StatusMissao.DISPONIVEL,
                missoesRepository.findById(destinoId)
                        .orElseThrow()
                        .getStatus()
        );
    }

    @Test
    void naoDeveTransferirNinjaSemMissao() throws Exception {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Sakura Haruno",
                "sakura.sem.transferencia@example.com",
                19,
                Rank.B
        );

        MissoesModel destino = criarMissao(
                "Missão de destino",
                Rank.C,
                StatusMissao.DISPONIVEL
        );

        Long ninjaId = ninja.getId();
        Long destinoId = destino.getId();

        // ACT + ASSERT
        mockMvc.perform(
                        patch(
                                "/ninjas/{ninjaId}/transferencias/{novaMissaoId}",
                                ninjaId,
                                destinoId
                        )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Ninja não possui uma missão"));

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertNull(ninjaSalvo.getMissoes());
    }

    @Test
    void naoDeveTransferirNinjaParaMesmaMissao() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão atual",
                Rank.B,
                StatusMissao.EM_ANDAMENTO
        );

        NinjaModel ninja = criarNinja(
                "Naruto Uzumaki",
                "naruto.mesma.missao@example.com",
                25,
                Rank.A
        );

        ninja.setMissoes(missao);
        ninjaRepository.saveAndFlush(ninja);

        Long ninjaId = ninja.getId();
        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(
                        patch(
                                "/ninjas/{ninjaId}/transferencias/{novaMissaoId}",
                                ninjaId,
                                missaoId
                        )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Ninja já está atribuído a esta missão"));

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertEquals(missaoId, ninjaSalvo.getMissoes().getId());
    }

    @ParameterizedTest
    @EnumSource(
            value = StatusMissao.class,
            names = {"INATIVA", "CONCLUIDA"}
    )
    void naoDeveTransferirNinjaParaMissaoInativaOuConcluida(StatusMissao statusDestino) throws Exception {

        // ARRANGE
        MissoesModel origem = criarMissao(
                "Missão de origem",
                Rank.B,
                StatusMissao.EM_ANDAMENTO
        );

        MissoesModel destino = criarMissao(
                "Missão de destino",
                Rank.C,
                statusDestino
        );

        NinjaModel ninja = criarNinja(
                "Sasuke Uchiha",
                "sasuke.transferencia.bloqueada@example.com",
                25,
                Rank.A
        );

        ninja.setMissoes(origem);
        ninjaRepository.saveAndFlush(ninja);

        Long ninjaId = ninja.getId();
        Long origemId = origem.getId();
        Long destinoId = destino.getId();

        String mensagemEsperada =
                statusDestino == StatusMissao.INATIVA
                        ? "Não é possível atribuir ninja a uma missão inativa"
                        : "Não é possível atribuir ninja a uma missão concluída";

        // ACT + ASSERT
        mockMvc.perform(
                        patch(
                                "/ninjas/{ninjaId}/transferencias/{novaMissaoId}",
                                ninjaId,
                                destinoId
                        )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(mensagemEsperada));

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertEquals(origemId, ninjaSalvo.getMissoes().getId());

        assertEquals(
                statusDestino,
                missoesRepository.findById(destinoId)
                        .orElseThrow()
                        .getStatus()
        );
    }

    @Test
    void naoDeveTransferirNinjaInexistente() throws Exception {

        // ARRANGE
        MissoesModel destino = criarMissao(
                "Missão de destino",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        Long destinoId = destino.getId();

        Long ninjaIdInexistente = ninjaRepository.findAll()
                .stream()
                .map(NinjaModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(ninjaRepository.existsById(ninjaIdInexistente));

        // ACT + ASSERT
        mockMvc.perform(
                        patch(
                                "/ninjas/{ninjaId}/transferencias/{novaMissaoId}",
                                ninjaIdInexistente,
                                destinoId
                        )
                )
                .andExpect(status().isNotFound());

        // ASSERT
        assertFalse(ninjaRepository.existsById(ninjaIdInexistente));
    }

    @Test
    void naoDeveTransferirNinjaParaMissaoInexistente() throws Exception {

        // ARRANGE
        MissoesModel origem = criarMissao(
                "Missão atual",
                Rank.B,
                StatusMissao.EM_ANDAMENTO
        );

        NinjaModel ninja = criarNinja(
                "Naruto Uzumaki",
                "naruto.destino.inexistente@example.com",
                25,
                Rank.A
        );

        ninja.setMissoes(origem);
        ninjaRepository.saveAndFlush(ninja);

        Long ninjaId = ninja.getId();
        Long origemId = origem.getId();

        Long missaoIdInexistente = missoesRepository.findAll()
                .stream()
                .map(MissoesModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(
                missoesRepository.existsById(missaoIdInexistente)
        );

        // ACT + ASSERT
        mockMvc.perform(
                        patch(
                                "/ninjas/{ninjaId}/transferencias/{novaMissaoId}",
                                ninjaId,
                                missaoIdInexistente
                        )
                )
                .andExpect(status().isNotFound());

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertEquals(origemId, ninjaSalvo.getMissoes().getId());
    }

    @Test
    void deveInativarMissaoDisponivelComStatus200() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão disponível",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(patch("/missoes/{id}/inativar", missaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(missaoId))
                .andExpect(jsonPath("$.nome").value("Missão disponível"))
                .andExpect(jsonPath("$.rank").value("B"))
                .andExpect(jsonPath("$.status").value("INATIVA"));

        // ASSERT - Confirmar persistência
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoSalva = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(StatusMissao.INATIVA, missaoSalva.getStatus());
    }

    @Test
    void deveInativarMissaoEmAndamentoERemoverTodosOsVinculos() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão em andamento",
                Rank.B,
                StatusMissao.EM_ANDAMENTO
        );

        NinjaModel ninja1 = criarNinja(
                "Naruto Uzumaki",
                "naruto.inativacao@example.com",
                25,
                Rank.A
        );

        NinjaModel ninja2 = criarNinja(
                "Sasuke Uchiha",
                "sasuke.inativacao@example.com",
                25,
                Rank.S
        );

        ninja1.setMissoes(missao);
        ninja2.setMissoes(missao);

        ninjaRepository.saveAndFlush(ninja1);
        ninjaRepository.saveAndFlush(ninja2);

        Long missaoId = missao.getId();
        Long ninja1Id = ninja1.getId();
        Long ninja2Id = ninja2.getId();

        // ACT + ASSERT
        mockMvc.perform(patch("/missoes/{id}/inativar", missaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(missaoId))
                .andExpect(jsonPath("$.status").value("INATIVA"));

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoSalva = missoesRepository
                .findById(missaoId).orElseThrow();

        NinjaModel ninja1Salvo = ninjaRepository
                .findById(ninja1Id).orElseThrow();

        NinjaModel ninja2Salvo = ninjaRepository
                .findById(ninja2Id).orElseThrow();

        assertEquals(StatusMissao.INATIVA, missaoSalva.getStatus());
        assertNull(ninja1Salvo.getMissoes());
        assertNull(ninja2Salvo.getMissoes());

        assertFalse(ninjaRepository.existsByMissoes_Id(missaoId));
    }

    @Test
    void deveInativarMissaoSemNinjasVinculados() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão sem participantes",
                Rank.C,
                StatusMissao.DISPONIVEL
        );

        Long missaoId = missao.getId();

        assertFalse(ninjaRepository.existsByMissoes_Id(missaoId));

        // ACT + ASSERT
        mockMvc.perform(patch("/missoes/{id}/inativar", missaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVA"));

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        assertEquals(
                StatusMissao.INATIVA,
                missoesRepository.findById(missaoId)
                        .orElseThrow()
                        .getStatus()
        );

        assertFalse(ninjaRepository.existsByMissoes_Id(missaoId));
    }

    @Test
    void naoDeveInativarMissaoConcluida() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão concluída",
                Rank.A,
                StatusMissao.CONCLUIDA
        );

        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(patch("/missoes/{id}/inativar", missaoId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "Não é possível inativar uma missão concluída"
                ));

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoSalva = missoesRepository
                .findById(missaoId).orElseThrow();

        assertEquals(StatusMissao.CONCLUIDA, missaoSalva.getStatus());
    }

    @Test
    void naoDeveInativarMissaoInexistente() throws Exception {

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
                        patch("/missoes/{id}/inativar", missaoIdInexistente)
                )
                .andExpect(status().isNotFound());

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        assertFalse(missoesRepository.existsById(missaoIdInexistente));
        assertEquals(quantidadeAntes, missoesRepository.count());
    }


    @Test
    void deveConcluirMissaoEmAndamentoComStatus200() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão de resgate",
                Rank.B,
                StatusMissao.EM_ANDAMENTO
        );

        NinjaModel ninja = criarNinja(
                "Naruto Uzumaki",
                "naruto.conclusao@example.com",
                25,
                Rank.A
        );

        ninja.setMissoes(missao);
        ninjaRepository.saveAndFlush(ninja);

        Long missaoId = missao.getId();
        Long ninjaId = ninja.getId();

        // ACT + ASSERT
        mockMvc.perform(patch("/missoes/{id}/concluir", missaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(missaoId))
                .andExpect(jsonPath("$.nome").value("Missão de resgate"))
                .andExpect(jsonPath("$.rank").value("B"))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));

        // ASSERT - Confirmar persistência
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoSalva = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertEquals(
                StatusMissao.CONCLUIDA,
                missaoSalva.getStatus()
        );

        assertNull(ninjaSalvo.getMissoes());
    }

    @Test
    void deveRemoverTodosOsVinculosAoConcluirMissao() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão em equipe",
                Rank.B,
                StatusMissao.EM_ANDAMENTO
        );

        NinjaModel ninja1 = criarNinja(
                "Naruto Uzumaki",
                "naruto.equipe.conclusao@example.com",
                25,
                Rank.A
        );

        NinjaModel ninja2 = criarNinja(
                "Sasuke Uchiha",
                "sasuke.equipe.conclusao@example.com",
                25,
                Rank.S
        );

        NinjaModel ninja3 = criarNinja(
                "Konohamaru Sarutobi",
                "konohamaru.equipe.conclusao@example.com",
                18,
                Rank.D
        );

        ninja1.setMissoes(missao);
        ninja2.setMissoes(missao);
        ninja3.setMissoes(missao);

        ninjaRepository.saveAllAndFlush(
                List.of(ninja1, ninja2, ninja3)
        );

        Long missaoId = missao.getId();
        Long ninja1Id = ninja1.getId();
        Long ninja2Id = ninja2.getId();
        Long ninja3Id = ninja3.getId();

        // ACT + ASSERT
        mockMvc.perform(patch("/missoes/{id}/concluir", missaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        assertEquals(
                StatusMissao.CONCLUIDA,
                missoesRepository.findById(missaoId)
                        .orElseThrow()
                        .getStatus()
        );

        assertNull(ninjaRepository.findById(ninja1Id)
                .orElseThrow().getMissoes());

        assertNull(ninjaRepository.findById(ninja2Id)
                .orElseThrow().getMissoes());

        assertNull(ninjaRepository.findById(ninja3Id)
                .orElseThrow().getMissoes());

        assertFalse(
                ninjaRepository.existsByMissoes_Id(missaoId)
        );
    }

    @ParameterizedTest
    @EnumSource(
            value = StatusMissao.class,
            names = {"DISPONIVEL", "INATIVA", "CONCLUIDA"}
    )
    void naoDeveConcluirMissaoQueNaoEstaEmAndamento(StatusMissao statusInicial) throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão com conclusão proibida",
                Rank.A,
                statusInicial
        );

        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(patch("/missoes/{id}/concluir", missaoId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "Só é possível concluir uma missão em andamento"
                ));

        // ASSERT - Confirmar que o status foi preservado
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoSalva = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(statusInicial, missaoSalva.getStatus());
        assertEquals(
                "Missão com conclusão proibida",
                missaoSalva.getNome()
        );
        assertEquals(Rank.A, missaoSalva.getRank());
    }

    @Test
    void naoDeveConcluirMissaoInexistente() throws Exception {

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
                        patch("/missoes/{id}/concluir", missaoIdInexistente)
                )
                .andExpect(status().isNotFound());

        // ASSERT
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
    void devePreservarNomeERankAoConcluirMissao() throws Exception {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão especial de reconhecimento",
                Rank.S,
                StatusMissao.EM_ANDAMENTO
        );

        NinjaModel ninja = criarNinja(
                "Kakashi Hatake",
                "kakashi.conclusao.dados@example.com",
                30,
                Rank.S
        );

        ninja.setMissoes(missao);
        ninjaRepository.saveAndFlush(ninja);

        Long missaoId = missao.getId();

        // ACT + ASSERT
        mockMvc.perform(patch("/missoes/{id}/concluir", missaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(missaoId))
                .andExpect(jsonPath("$.nome")
                        .value("Missão especial de reconhecimento"))
                .andExpect(jsonPath("$.rank").value("S"))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));

        // ASSERT - Verificar persistência
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoSalva = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(
                "Missão especial de reconhecimento",
                missaoSalva.getNome()
        );

        assertEquals(Rank.S, missaoSalva.getRank());
        assertEquals(StatusMissao.CONCLUIDA, missaoSalva.getStatus());

        assertFalse(
                ninjaRepository.existsByMissoes_Id(missaoId)
        );
    }


















}
