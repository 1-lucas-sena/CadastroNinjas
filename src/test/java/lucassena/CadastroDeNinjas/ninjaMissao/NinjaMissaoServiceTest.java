package lucassena.CadastroDeNinjas.ninjaMissao;

import jakarta.persistence.EntityManager;
import lucassena.CadastroDeNinjas.exceptions.RecursoNaoEncontradoException;
import lucassena.CadastroDeNinjas.exceptions.RegraDeNegocioException;
import lucassena.CadastroDeNinjas.missoes.MissoesModel;
import lucassena.CadastroDeNinjas.missoes.MissoesRepository;
import lucassena.CadastroDeNinjas.missoes.StatusMissao;
import lucassena.CadastroDeNinjas.ninjas.NinjaModel;
import lucassena.CadastroDeNinjas.ninjas.NinjaRepository;
import lucassena.CadastroDeNinjas.util.Rank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NinjaMissaoServiceTest {

    @Autowired
    private NinjaMissaoService ninjaMissaoService;

    @Autowired
    private NinjaRepository ninjaRepository;

    @Autowired
    private MissoesRepository missoesRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveIniciarMissaoQuandoNinjaForQualificado() {

        // ARRANGE - Preparar os dados
        MissoesModel missao = criarMissao(
                "Missão de teste",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = criarNinja(
                "Ninja de teste",
                "ninja.teste.integracao@example.com",
                25,
                Rank.A
        );

        // ACT - Executar a operação
        ninjaMissaoService.atribuirMissao(
                ninja.getId(),
                missao.getId()
        );

        // ASSERT - Verificar o resultado persistido
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missao.getId())
                .orElseThrow();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninja.getId())
                .orElseThrow();

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missaoAtualizada.getStatus()
        );

        assertNotNull(ninjaAtualizado.getMissoes());

        assertEquals(
                missaoAtualizada.getId(),
                ninjaAtualizado.getMissoes().getId()
        );
    }

    @Test
    void deveManterMissaoDisponivelQuandoNinjaNaoForQualificado() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão de Rank A",
                Rank.A,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = criarNinja(
                "Ninja Rank D",
                "ninja.rank.d@example.com",
                22,
                Rank.D
        );

        // ACT
        ninjaMissaoService.atribuirMissao(
                ninja.getId(),
                missao.getId()
        );

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missao.getId())
                .orElseThrow();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninja.getId())
                .orElseThrow();

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );

        assertNotNull(ninjaAtualizado.getMissoes());

        assertEquals(
                missaoAtualizada.getId(),
                ninjaAtualizado.getMissoes().getId()
        );
    }

    @Test
    void deveDisponibilizarMissaoAoRemoverUltimoNinjaQualificado() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão de remoção",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = criarNinja(
                "Ninja qualificado",
                "ninja.remocao@example.com",
                25,
                Rank.A
        );

        ninjaMissaoService.atribuirMissao(
                ninja.getId(),
                missao.getId()
        );

        // Confirmar a pré-condição
        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(missao.getId())
                        .orElseThrow()
                        .getStatus()
        );

        // ACT
        ninjaMissaoService.removerMissao(ninja.getId());

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missao.getId())
                .orElseThrow();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninja.getId())
                .orElseThrow();

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );

        assertNull(ninjaAtualizado.getMissoes());
    }

    @Test
    void deveManterMissaoEmAndamentoQuandoOutroNinjaQualificadoPermanece() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão com dois ninjas",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja1 = criarNinja(
                "Ninja Rank A",
                "ninja1.remocao@example.com",
                25,
                Rank.A
        );

        NinjaModel ninja2 = criarNinja(
                "Ninja Rank S",
                "ninja2.remocao@example.com",
                28,
                Rank.S
        );

        ninjaMissaoService.atribuirMissao(ninja1.getId(), missao.getId());
        ninjaMissaoService.atribuirMissao(ninja2.getId(), missao.getId());

        // Confirmar a pré-condição
        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(missao.getId())
                        .orElseThrow()
                        .getStatus()
        );

        // ACT
        ninjaMissaoService.removerMissao(ninja1.getId());

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missao.getId())
                .orElseThrow();

        NinjaModel ninja1Atualizado = ninjaRepository
                .findById(ninja1.getId())
                .orElseThrow();

        NinjaModel ninja2Atualizado = ninjaRepository
                .findById(ninja2.getId())
                .orElseThrow();

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missaoAtualizada.getStatus()
        );

        assertNull(ninja1Atualizado.getMissoes());

        assertNotNull(ninja2Atualizado.getMissoes());

        assertEquals(
                missaoAtualizada.getId(),
                ninja2Atualizado.getMissoes().getId()
        );
    }

    @Test
    void naoDeveTransferirUltimoNinjaQualificadoDeMissaoEmAndamento() {

        // ARRANGE
        MissoesModel missaoOriginal = criarMissao(
                "Missão original", Rank.B, StatusMissao.DISPONIVEL
        );

        MissoesModel missaoDestino = criarMissao(
                "Missão destino", Rank.A, StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = criarNinja(
                "Ninja de transferência",
                "ninja.transferencia.bloqueada@example.com",
                25, Rank.A
        );

        ninjaMissaoService.atribuirMissao(
                ninja.getId(), missaoOriginal.getId()
        );

        Long ninjaId = ninja.getId();
        Long missaoDestinoId = missaoDestino.getId();

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(missaoOriginal.getId())
                        .orElseThrow().getStatus()
        );

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaMissaoService.transferirMissao(
                        ninjaId, missaoDestinoId
                )
        );

        assertEquals(
                "Transferência não permitida: o ninja é o último qualificado da missão em andamento",
                excecao.getMessage()
        );

        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId).orElseThrow();

        MissoesModel originalAtualizada = missoesRepository
                .findById(missaoOriginal.getId()).orElseThrow();

        MissoesModel destinoAtualizada = missoesRepository
                .findById(missaoDestinoId).orElseThrow();

        assertNotNull(ninjaAtualizado.getMissoes());

        assertEquals(
                originalAtualizada.getId(),
                ninjaAtualizado.getMissoes().getId()
        );

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                originalAtualizada.getStatus()
        );

        assertEquals(
                StatusMissao.DISPONIVEL,
                destinoAtualizada.getStatus()
        );
    }

    @Test
    void deveTransferirNinjaQuandoOutroQualificadoPermaneceNaMissaoOriginal() {

        // ARRANGE
        MissoesModel missaoOriginal = criarMissao(
                "Missão original de transferência",
                Rank.B, StatusMissao.DISPONIVEL
        );

        MissoesModel missaoDestino = criarMissao(
                "Missão destino de transferência",
                Rank.A, StatusMissao.DISPONIVEL
        );

        NinjaModel ninja1 = criarNinja(
                "Ninja transferido",
                "ninja.transferido@example.com",
                25, Rank.A
        );

        NinjaModel ninja2 = criarNinja(
                "Ninja que permanece",
                "ninja.permanece@example.com",
                28, Rank.S
        );

        ninjaMissaoService.atribuirMissao(
                ninja1.getId(), missaoOriginal.getId()
        );

        ninjaMissaoService.atribuirMissao(
                ninja2.getId(), missaoOriginal.getId()
        );

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(missaoOriginal.getId())
                        .orElseThrow().getStatus()
        );

        // ACT
        ninjaMissaoService.transferirMissao(
                ninja1.getId(), missaoDestino.getId()
        );

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninja1Atualizado = ninjaRepository
                .findById(ninja1.getId()).orElseThrow();

        NinjaModel ninja2Atualizado = ninjaRepository
                .findById(ninja2.getId()).orElseThrow();

        MissoesModel originalAtualizada = missoesRepository
                .findById(missaoOriginal.getId()).orElseThrow();

        MissoesModel destinoAtualizada = missoesRepository
                .findById(missaoDestino.getId()).orElseThrow();

        assertNotNull(ninja1Atualizado.getMissoes());
        assertEquals(
                destinoAtualizada.getId(),
                ninja1Atualizado.getMissoes().getId()
        );

        assertNotNull(ninja2Atualizado.getMissoes());
        assertEquals(
                originalAtualizada.getId(),
                ninja2Atualizado.getMissoes().getId()
        );

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                originalAtualizada.getStatus()
        );

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                destinoAtualizada.getStatus()
        );
    }

    @Test
    void naoDeveTransferirNinjaSemMissao() {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Ninja sem missão",
                "ninja.sem.missao@example.com",
                25, Rank.A
        );

        MissoesModel missaoDestino = criarMissao(
                "Missão destino", Rank.B, StatusMissao.DISPONIVEL
        );

        Long ninjaId = ninja.getId();
        Long missaoDestinoId = missaoDestino.getId();

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaMissaoService.transferirMissao(
                        ninjaId, missaoDestinoId
                )
        );

        assertEquals(
                "Ninja não possui uma missão",
                excecao.getMessage()
        );

        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId).orElseThrow();

        MissoesModel destinoAtualizada = missoesRepository
                .findById(missaoDestinoId).orElseThrow();

        assertNull(ninjaAtualizado.getMissoes());

        assertEquals(
                StatusMissao.DISPONIVEL,
                destinoAtualizada.getStatus()
        );
    }

    @Test
    void naoDeveTransferirNinjaParaMesmaMissao() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão de origem e destino",
                Rank.B, StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = criarNinja(
                "Ninja da mesma missão",
                "ninja.mesma.missao@example.com",
                25, Rank.A
        );

        Long ninjaId = ninja.getId();
        Long missaoId = missao.getId();

        ninjaMissaoService.atribuirMissao(ninjaId, missaoId);

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(missaoId)
                        .orElseThrow().getStatus()
        );

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaMissaoService.transferirMissao(
                        ninjaId, missaoId
                )
        );

        assertEquals(
                "Ninja já está atribuído a esta missão",
                excecao.getMessage()
        );

        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId).orElseThrow();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId).orElseThrow();

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
    void naoDeveTransferirNinjaParaMissaoInativa() {

        // ARRANGE
        MissoesModel missaoOriginal = criarMissao(
                "Missão original ativa",
                Rank.B, StatusMissao.DISPONIVEL
        );

        MissoesModel missaoDestino = criarMissao(
                "Missão destino inativa",
                Rank.A, StatusMissao.INATIVA
        );

        NinjaModel ninja = criarNinja(
                "Ninja transferencia inativa",
                "ninja.transferencia.inativa@example.com",
                25, Rank.A
        );

        Long ninjaId = ninja.getId();
        Long missaoOriginalId = missaoOriginal.getId();
        Long missaoDestinoId = missaoDestino.getId();

        ninjaMissaoService.atribuirMissao(ninjaId, missaoOriginalId);

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(missaoOriginalId)
                        .orElseThrow().getStatus()
        );

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaMissaoService.transferirMissao(
                        ninjaId, missaoDestinoId
                )
        );

        assertEquals(
                "Não é possível atribuir ninja a uma missão inativa",
                excecao.getMessage()
        );

        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId).orElseThrow();

        MissoesModel originalAtualizada = missoesRepository
                .findById(missaoOriginalId).orElseThrow();

        MissoesModel destinoAtualizada = missoesRepository
                .findById(missaoDestinoId).orElseThrow();

        assertNotNull(ninjaAtualizado.getMissoes());

        assertEquals(
                missaoOriginalId,
                ninjaAtualizado.getMissoes().getId()
        );

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                originalAtualizada.getStatus()
        );

        assertEquals(
                StatusMissao.INATIVA,
                destinoAtualizada.getStatus()
        );

        assertFalse(
                ninjaRepository.existsByMissoes_Id(missaoDestinoId)
        );
    }

    @Test
    void naoDeveTransferirNinjaParaMissaoConcluida() {

        // ARRANGE
        MissoesModel missaoOriginal = criarMissao(
                "Missão original para transferência",
                Rank.B, StatusMissao.DISPONIVEL
        );

        MissoesModel missaoDestino = criarMissao(
                "Missão destino concluída",
                Rank.A, StatusMissao.CONCLUIDA
        );

        NinjaModel ninja = criarNinja(
                "Ninja transferencia concluida",
                "ninja.transferencia.concluida@example.com",
                25, Rank.A
        );

        Long ninjaId = ninja.getId();
        Long missaoOriginalId = missaoOriginal.getId();
        Long missaoDestinoId = missaoDestino.getId();

        ninjaMissaoService.atribuirMissao(ninjaId, missaoOriginalId);

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(missaoOriginalId)
                        .orElseThrow().getStatus()
        );

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaMissaoService.transferirMissao(
                        ninjaId, missaoDestinoId
                )
        );

        assertEquals(
                "Não é possível atribuir ninja a uma missão concluída",
                excecao.getMessage()
        );

        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId).orElseThrow();

        MissoesModel originalAtualizada = missoesRepository
                .findById(missaoOriginalId).orElseThrow();

        MissoesModel destinoAtualizada = missoesRepository
                .findById(missaoDestinoId).orElseThrow();

        assertNotNull(ninjaAtualizado.getMissoes());

        assertEquals(
                missaoOriginalId,
                ninjaAtualizado.getMissoes().getId()
        );

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                originalAtualizada.getStatus()
        );

        assertEquals(
                StatusMissao.CONCLUIDA,
                destinoAtualizada.getStatus()
        );

        assertFalse(
                ninjaRepository.existsByMissoes_Id(missaoDestinoId)
        );
    }

    @Test
    void deveDisponibilizarMissaoAoExcluirUltimoNinjaQualificado() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão de exclusão",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = criarNinja(
                "Ninja para exclusão",
                "ninja.exclusao.qualificado@example.com",
                25,
                Rank.A
        );

        Long ninjaId = ninja.getId();
        Long missaoId = missao.getId();

        ninjaMissaoService.atribuirMissao(ninjaId, missaoId);

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(missaoId)
                        .orElseThrow().getStatus()
        );

        // ACT
        ninjaMissaoService.deletarNinja(ninjaId);

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        assertFalse(ninjaRepository.existsById(ninjaId));

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );

        assertFalse(ninjaRepository.existsByMissoes_Id(missaoId));
    }

    @Test
    void deveManterMissaoEmAndamentoAoExcluirNinjaSeOutroQualificadoPermanece() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão com dois ninjas para exclusão",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja1 = criarNinja(
                "Ninja para excluir",
                "ninja.exclusao.primeiro@example.com",
                25,
                Rank.A
        );

        NinjaModel ninja2 = criarNinja(
                "Ninja que permanece",
                "ninja.exclusao.segundo@example.com",
                28,
                Rank.S
        );

        Long missaoId = missao.getId();
        Long ninja1Id = ninja1.getId();
        Long ninja2Id = ninja2.getId();

        ninjaMissaoService.atribuirMissao(ninja1Id, missaoId);
        ninjaMissaoService.atribuirMissao(ninja2Id, missaoId);

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(missaoId)
                        .orElseThrow().getStatus()
        );

        // ACT
        ninjaMissaoService.deletarNinja(ninja1Id);

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        assertFalse(ninjaRepository.existsById(ninja1Id));

        NinjaModel ninjaRestante = ninjaRepository
                .findById(ninja2Id)
                .orElseThrow();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertNotNull(ninjaRestante.getMissoes());

        assertEquals(
                missaoId,
                ninjaRestante.getMissoes().getId()
        );

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missaoAtualizada.getStatus()
        );

        assertTrue(ninjaRepository.existsByMissoes_Id(missaoId));
    }

    @Test
    void deveInativarMissaoEDesvincularTodosOsNinjas() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão para inativação",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja1 = criarNinja(
                "Ninja qualificado",
                "ninja.inativacao.a@example.com",
                25,
                Rank.A
        );

        NinjaModel ninja2 = criarNinja(
                "Ninja não qualificado",
                "ninja.inativacao.d@example.com",
                22,
                Rank.D
        );

        Long missaoId = missao.getId();
        Long ninja1Id = ninja1.getId();
        Long ninja2Id = ninja2.getId();

        ninjaMissaoService.atribuirMissao(ninja1Id, missaoId);
        ninjaMissaoService.atribuirMissao(ninja2Id, missaoId);

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(missaoId)
                        .orElseThrow().getStatus()
        );

        // ACT
        ninjaMissaoService.inativarMissao(missaoId);

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        NinjaModel ninja1Atualizado = ninjaRepository
                .findById(ninja1Id)
                .orElseThrow();

        NinjaModel ninja2Atualizado = ninjaRepository
                .findById(ninja2Id)
                .orElseThrow();

        assertEquals(
                StatusMissao.INATIVA,
                missaoAtualizada.getStatus()
        );

        assertNull(ninja1Atualizado.getMissoes());
        assertNull(ninja2Atualizado.getMissoes());

        assertFalse(ninjaRepository.existsByMissoes_Id(missaoId));

        assertTrue(ninjaRepository.existsById(ninja1Id));
        assertTrue(ninjaRepository.existsById(ninja2Id));
    }

    @Test
    void naoDeveInativarMissaoConcluida() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão já concluída",
                Rank.B,
                StatusMissao.CONCLUIDA
        );

        Long missaoId = missao.getId();

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaMissaoService.inativarMissao(missaoId)
        );

        assertEquals(
                "Não é possível inativar uma missão concluída",
                excecao.getMessage()
        );

        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(
                StatusMissao.CONCLUIDA,
                missaoAtualizada.getStatus()
        );

        assertTrue(missoesRepository.existsById(missaoId));
    }

    @Test
    void deveConcluirMissaoEDesvincularTodosOsNinjas() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão para conclusão",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja1 = criarNinja(
                "Ninja qualificado",
                "ninja.conclusao.a@example.com",
                25,
                Rank.A
        );

        NinjaModel ninja2 = criarNinja(
                "Ninja não qualificado",
                "ninja.conclusao.d@example.com",
                22,
                Rank.D
        );

        Long missaoId = missao.getId();
        Long ninja1Id = ninja1.getId();
        Long ninja2Id = ninja2.getId();

        ninjaMissaoService.atribuirMissao(ninja1Id, missaoId);
        ninjaMissaoService.atribuirMissao(ninja2Id, missaoId);

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(missaoId)
                        .orElseThrow().getStatus()
        );

        // ACT
        ninjaMissaoService.concluirMissao(missaoId);

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        NinjaModel ninja1Atualizado = ninjaRepository
                .findById(ninja1Id)
                .orElseThrow();

        NinjaModel ninja2Atualizado = ninjaRepository
                .findById(ninja2Id)
                .orElseThrow();

        assertEquals(
                StatusMissao.CONCLUIDA,
                missaoAtualizada.getStatus()
        );

        assertNull(ninja1Atualizado.getMissoes());
        assertNull(ninja2Atualizado.getMissoes());

        assertFalse(ninjaRepository.existsByMissoes_Id(missaoId));

        assertTrue(ninjaRepository.existsById(ninja1Id));
        assertTrue(ninjaRepository.existsById(ninja2Id));
    }

    @Test
    void naoDeveConcluirMissaoDisponivel() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão disponível",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        Long missaoId = missao.getId();

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaMissaoService.concluirMissao(missaoId)
        );

        assertEquals(
                "Só é possível concluir uma missão em andamento",
                excecao.getMessage()
        );

        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );

        assertTrue(missoesRepository.existsById(missaoId));
    }

    @Test
    void naoDeveAtribuirNinjaAMissaoInativa() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão inativa para atribuição",
                Rank.B,
                StatusMissao.INATIVA
        );

        NinjaModel ninja = criarNinja(
                "Ninja sem missão",
                "ninja.atribuicao.inativa@example.com",
                25,
                Rank.A
        );

        Long ninjaId = ninja.getId();
        Long missaoId = missao.getId();

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaMissaoService.atribuirMissao(ninjaId, missaoId)
        );

        assertEquals(
                "Não é possível atribuir ninja a uma missão inativa",
                excecao.getMessage()
        );

        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId).orElseThrow();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId).orElseThrow();

        assertNull(ninjaAtualizado.getMissoes());

        assertEquals(
                StatusMissao.INATIVA,
                missaoAtualizada.getStatus()
        );

        assertFalse(ninjaRepository.existsByMissoes_Id(missaoId));
    }

    @Test
    void naoDeveAtribuirNinjaAMissaoConcluida() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão concluída para atribuição",
                Rank.B,
                StatusMissao.CONCLUIDA
        );

        NinjaModel ninja = criarNinja(
                "Ninja para missão concluída",
                "ninja.atribuicao.concluida@example.com",
                25,
                Rank.A
        );

        Long ninjaId = ninja.getId();
        Long missaoId = missao.getId();

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaMissaoService.atribuirMissao(ninjaId, missaoId)
        );

        assertEquals(
                "Não é possível atribuir ninja a uma missão concluída",
                excecao.getMessage()
        );

        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId).orElseThrow();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId).orElseThrow();

        assertNull(ninjaAtualizado.getMissoes());

        assertEquals(
                StatusMissao.CONCLUIDA,
                missaoAtualizada.getStatus()
        );

        assertFalse(ninjaRepository.existsByMissoes_Id(missaoId));
    }

    @Test
    void naoDeveAtribuirOutraMissaoParaNinjaJaVinculado() {

        // ARRANGE
        MissoesModel missaoOriginal = criarMissao(
                "Missão original do ninja",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        MissoesModel segundaMissao = criarMissao(
                "Segunda missão",
                Rank.A,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = criarNinja(
                "Ninja com missão existente",
                "ninja.atribuicao.duplicada@example.com",
                25,
                Rank.A
        );

        Long ninjaId = ninja.getId();
        Long missaoOriginalId = missaoOriginal.getId();
        Long segundaMissaoId = segundaMissao.getId();

        ninjaMissaoService.atribuirMissao(ninjaId, missaoOriginalId);

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(missaoOriginalId)
                        .orElseThrow().getStatus()
        );

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaMissaoService.atribuirMissao(
                        ninjaId, segundaMissaoId
                )
        );

        assertEquals(
                "Ninja já possui uma missão",
                excecao.getMessage()
        );

        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId).orElseThrow();

        MissoesModel originalAtualizada = missoesRepository
                .findById(missaoOriginalId).orElseThrow();

        MissoesModel segundaAtualizada = missoesRepository
                .findById(segundaMissaoId).orElseThrow();

        assertNotNull(ninjaAtualizado.getMissoes());

        assertEquals(
                missaoOriginalId,
                ninjaAtualizado.getMissoes().getId()
        );

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                originalAtualizada.getStatus()
        );

        assertEquals(
                StatusMissao.DISPONIVEL,
                segundaAtualizada.getStatus()
        );

        assertFalse(ninjaRepository.existsByMissoes_Id(segundaMissaoId));
    }

    @Test
    void naoDeveRemoverMissaoDeNinjaSemVinculo() {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Ninja sem vínculo",
                "ninja.sem.vinculo@example.com",
                25,
                Rank.A
        );

        Long ninjaId = ninja.getId();

        assertNull(ninja.getMissoes());

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaMissaoService.removerMissao(ninjaId)
        );

        assertEquals(
                "Ninja não possui uma missão",
                excecao.getMessage()
        );

        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId).orElseThrow();

        assertTrue(ninjaRepository.existsById(ninjaId));
        assertNull(ninjaAtualizado.getMissoes());
    }

    @Test
    void naoDeveConcluirMissaoInativa() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão inativa",
                Rank.B,
                StatusMissao.INATIVA
        );

        Long missaoId = missao.getId();

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaMissaoService.concluirMissao(missaoId)
        );

        assertEquals(
                "Só é possível concluir uma missão em andamento",
                excecao.getMessage()
        );

        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId).orElseThrow();

        assertEquals(
                StatusMissao.INATIVA,
                missaoAtualizada.getStatus()
        );

        assertTrue(missoesRepository.existsById(missaoId));
    }

    @Test
    void naoDeveConcluirMissaoJaConcluida() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão já concluída",
                Rank.B,
                StatusMissao.CONCLUIDA
        );

        Long missaoId = missao.getId();

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaMissaoService.concluirMissao(missaoId)
        );

        assertEquals(
                "Só é possível concluir uma missão em andamento",
                excecao.getMessage()
        );

        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId).orElseThrow();

        assertEquals(
                StatusMissao.CONCLUIDA,
                missaoAtualizada.getStatus()
        );

        assertTrue(missoesRepository.existsById(missaoId));
    }


    @Test
    void deveExcluirNinjaSemMissao() {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Ninja sem missão para exclusão",
                "ninja.exclusao.sem.missao@example.com",
                25,
                Rank.A
        );

        Long ninjaId = ninja.getId();

        assertTrue(ninjaRepository.existsById(ninjaId));
        assertNull(ninja.getMissoes());

        // ACT
        assertDoesNotThrow(
                () -> ninjaMissaoService.deletarNinja(ninjaId)
        );

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        assertFalse(ninjaRepository.existsById(ninjaId));
    }


    @Test
    void devePermitirInativarMissaoJaInativa() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão já inativa",
                Rank.B,
                StatusMissao.INATIVA
        );

        Long missaoId = missao.getId();

        assertEquals(
                StatusMissao.INATIVA,
                missao.getStatus()
        );

        // ACT
        assertDoesNotThrow(
                () -> ninjaMissaoService.inativarMissao(missaoId)
        );

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId).orElseThrow();

        assertEquals(
                StatusMissao.INATIVA,
                missaoAtualizada.getStatus()
        );

        assertFalse(ninjaRepository.existsByMissoes_Id(missaoId));
    }

    @Test
    void naoDeveAtribuirMissaoParaNinjaInexistente() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão para ninja inexistente",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        Long missaoId = missao.getId();
        Long ninjaIdInexistente = obterNinjaIdInexistente();

        // ACT + ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ninjaMissaoService.atribuirMissao(
                        ninjaIdInexistente, missaoId
                )
        );

        assertEquals("Ninja não encontrado", excecao.getMessage());

        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId).orElseThrow();

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );

        assertFalse(ninjaRepository.existsByMissoes_Id(missaoId));
    }

    @Test
    void naoDeveAtribuirMissaoInexistente() {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Ninja para missão inexistente",
                "ninja.missao.inexistente@example.com",
                25,
                Rank.A
        );

        Long ninjaId = ninja.getId();
        Long missaoIdInexistente = obterMissaoIdInexistente();

        // ACT + ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ninjaMissaoService.atribuirMissao(
                        ninjaId, missaoIdInexistente
                )
        );

        assertEquals("Missão não encontrada", excecao.getMessage());

        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId).orElseThrow();

        assertTrue(ninjaRepository.existsById(ninjaId));
        assertNull(ninjaAtualizado.getMissoes());
    }

    @Test
    void naoDeveRemoverMissaoDeNinjaInexistente() {

        // ARRANGE
        Long ninjaIdInexistente = obterNinjaIdInexistente();

        // ACT + ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ninjaMissaoService.removerMissao(ninjaIdInexistente)
        );

        assertEquals("Ninja não encontrado", excecao.getMessage());

        assertFalse(ninjaRepository.existsById(ninjaIdInexistente));
    }

    @Test
    void naoDeveTransferirNinjaInexistente() {

        // ARRANGE
        MissoesModel missaoDestino = criarMissao(
                "Missão destino para ninja inexistente",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        Long missaoDestinoId = missaoDestino.getId();
        Long ninjaIdInexistente = obterNinjaIdInexistente();

        // ACT + ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ninjaMissaoService.transferirMissao(
                        ninjaIdInexistente, missaoDestinoId
                )
        );

        assertEquals("Ninja não encontrado", excecao.getMessage());

        entityManager.flush();
        entityManager.clear();

        MissoesModel destinoAtualizada = missoesRepository
                .findById(missaoDestinoId).orElseThrow();

        assertEquals(
                StatusMissao.DISPONIVEL,
                destinoAtualizada.getStatus()
        );

        assertFalse(ninjaRepository.existsByMissoes_Id(missaoDestinoId));
    }

    @Test
    void naoDeveTransferirNinjaParaMissaoInexistente() {

        // ARRANGE
        MissoesModel missaoOriginal = criarMissao(
                "Missão original válida",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = criarNinja(
                "Ninja para transferência inexistente",
                "ninja.transferencia.inexistente@example.com",
                25,
                Rank.A
        );

        Long ninjaId = ninja.getId();
        Long missaoOriginalId = missaoOriginal.getId();

        ninjaMissaoService.atribuirMissao(ninjaId, missaoOriginalId);

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(missaoOriginalId)
                        .orElseThrow().getStatus()
        );

        Long missaoDestinoId = obterMissaoIdInexistente();

        // ACT + ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ninjaMissaoService.transferirMissao(
                        ninjaId, missaoDestinoId
                )
        );

        assertEquals("Missão não encontrada", excecao.getMessage());

        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId).orElseThrow();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoOriginalId).orElseThrow();

        assertNotNull(ninjaAtualizado.getMissoes());

        assertEquals(
                missaoOriginalId,
                ninjaAtualizado.getMissoes().getId()
        );

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missaoAtualizada.getStatus()
        );

        assertFalse(missoesRepository.existsById(missaoDestinoId));
    }

    @Test
    void naoDeveExcluirNinjaInexistente() {

        // ARRANGE
        Long ninjaIdInexistente = obterNinjaIdInexistente();
        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ninjaMissaoService.deletarNinja(ninjaIdInexistente)
        );

        assertEquals("Ninja não encontrado", excecao.getMessage());

        entityManager.flush();
        entityManager.clear();

        assertFalse(ninjaRepository.existsById(ninjaIdInexistente));
        assertEquals(quantidadeAntes, ninjaRepository.count());
    }

    @Test
    void naoDeveInativarMissaoInexistente() {

        // ARRANGE
        Long missaoIdInexistente = obterMissaoIdInexistente();
        long quantidadeAntes = missoesRepository.count();

        // ACT + ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ninjaMissaoService.inativarMissao(missaoIdInexistente)
        );

        assertEquals("Missão não encontrada", excecao.getMessage());

        entityManager.flush();
        entityManager.clear();

        assertFalse(missoesRepository.existsById(missaoIdInexistente));
        assertEquals(quantidadeAntes, missoesRepository.count());
    }

    @Test
    void naoDeveConcluirMissaoInexistente() {

        // ARRANGE
        Long missaoIdInexistente = obterMissaoIdInexistente();
        long quantidadeAntes = missoesRepository.count();

        // ACT + ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ninjaMissaoService.concluirMissao(missaoIdInexistente)
        );

        assertEquals("Missão não encontrada", excecao.getMessage());

        entityManager.flush();
        entityManager.clear();

        assertFalse(missoesRepository.existsById(missaoIdInexistente));
        assertEquals(quantidadeAntes, missoesRepository.count());
    }

    @Test
    void deveDisponibilizarMissaoQuandoRestaApenasNinjaNaoQualificado() {

        // ARRANGE - Criar missão de Rank B
        MissoesModel missao = criarMissao(
                "Missão com ninja não qualificado restante",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninjaQualificado = criarNinja(
                "Ninja qualificado",
                "ninja.qualificado.restante@example.com",
                25,
                Rank.A
        );

        NinjaModel ninjaNaoQualificado = criarNinja(
                "Ninja não qualificado",
                "ninja.nao.qualificado.restante@example.com",
                22,
                Rank.D
        );

        Long missaoId = missao.getId();
        Long ninjaQualificadoId = ninjaQualificado.getId();
        Long ninjaNaoQualificadoId = ninjaNaoQualificado.getId();

        // Vincular os dois ninjas à missão
        ninjaMissaoService.atribuirMissao(
                ninjaQualificadoId,
                missaoId
        );

        ninjaMissaoService.atribuirMissao(
                ninjaNaoQualificadoId,
                missaoId
        );

        // Confirmar que a missão está em andamento
        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missoesRepository.findById(missaoId)
                        .orElseThrow()
                        .getStatus()
        );

        // ACT - Remover apenas o ninja qualificado
        ninjaMissaoService.removerMissao(ninjaQualificadoId);

        // ASSERT - Consultar novamente o banco
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        NinjaModel qualificadoAtualizado = ninjaRepository
                .findById(ninjaQualificadoId)
                .orElseThrow();

        NinjaModel naoQualificadoAtualizado = ninjaRepository
                .findById(ninjaNaoQualificadoId)
                .orElseThrow();

        // Missão volta a ficar disponível
        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );

        // Ninja qualificado foi desvinculado
        assertNull(qualificadoAtualizado.getMissoes());

        // Ninja não qualificado permanece vinculado
        assertNotNull(naoQualificadoAtualizado.getMissoes());

        assertEquals(
                missaoId,
                naoQualificadoAtualizado.getMissoes().getId()
        );

        // Ainda existe ninja na missão
        assertTrue(ninjaRepository.existsByMissoes_Id(missaoId));
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

    private Long obterNinjaIdInexistente() {
        Long id = ninjaRepository.findAll().stream()
                .map(NinjaModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(ninjaRepository.existsById(id));

        return id;
    }

    private Long obterMissaoIdInexistente() {
        Long id = missoesRepository.findAll().stream()
                .map(MissoesModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(missoesRepository.existsById(id));

        return id;
    }
}
