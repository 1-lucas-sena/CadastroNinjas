package lucassena.CadastroDeNinjas.missaoStatus;

import jakarta.persistence.EntityManager;
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
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MissaoStatusServiceTest {

    @Autowired
    private MissaoStatusService missaoStatusService;

    @Autowired
    private NinjaRepository ninjaRepository;

    @Autowired
    private MissoesRepository missoesRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveConsiderarNinjaQualificadoQuandoRankForSuperior() {

        // ARRANGE - Criar ninja de Rank A
        NinjaModel ninja = new NinjaModel();
        ninja.setRank(Rank.A);

        // Criar missão que exige Rank B
        MissoesModel missao = new MissoesModel();
        missao.setRank(Rank.B);

        // ACT - Verificar a qualificação
        boolean qualificado = missaoStatusService
                .ninjaQualificadoParaMissao(ninja, missao);

        // ASSERT - Ninja deve ser qualificado
        assertTrue(qualificado);
    }

    @Test
    void deveConsiderarNinjaQualificadoQuandoRankForIgual() {

        // ARRANGE - Criar ninja de Rank B
        NinjaModel ninja = new NinjaModel();
        ninja.setRank(Rank.B);

        // Criar missão que exige Rank B
        MissoesModel missao = new MissoesModel();
        missao.setRank(Rank.B);

        // ACT - Verificar a qualificação
        boolean qualificado = missaoStatusService
                .ninjaQualificadoParaMissao(ninja, missao);

        // ASSERT - Rank igual também é suficiente
        assertTrue(qualificado);
    }

    @Test
    void naoDeveConsiderarNinjaQualificadoQuandoRankForInferior() {

        // ARRANGE - Criar ninja de Rank D
        NinjaModel ninja = new NinjaModel();
        ninja.setRank(Rank.D);

        // Criar missão que exige Rank A
        MissoesModel missao = new MissoesModel();
        missao.setRank(Rank.A);

        // ACT - Verificar a qualificação
        boolean qualificado = missaoStatusService
                .ninjaQualificadoParaMissao(ninja, missao);

        // ASSERT - Ninja não possui rank suficiente
        assertFalse(qualificado);
    }

    @Test
    void deveIniciarMissaoQuandoExistirNinjaQualificado() {

        // ARRANGE - Criar missão disponível
        MissoesModel missao = criarMissao(
                "Missão com ninja qualificado",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = criarNinja(
                "Ninja qualificado",
                "status.qualificado@example.com",
                Rank.A
        );

        Long missaoId = missao.getId();

        // Vincular ninja diretamente à missão
        ninja.setMissoes(missao);
        ninjaRepository.saveAndFlush(ninja);

        // ACT - Reavaliar status
        missaoStatusService.reavaliarStatus(missao);

        // ASSERT - Consultar novamente o banco
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missaoAtualizada.getStatus()
        );

        assertTrue(ninjaRepository.existsByMissoes_Id(missaoId));
    }

    @Test
    void deveManterMissaoDisponivelQuandoNenhumNinjaForQualificado() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão sem ninja qualificado",
                Rank.A,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = criarNinja(
                "Ninja não qualificado",
                "status.naoqualificado@example.com",
                Rank.D
        );

        Long missaoId = missao.getId();

        // Vincular ninja não qualificado
        ninja.setMissoes(missao);
        ninjaRepository.saveAndFlush(ninja);

        // ACT
        missaoStatusService.reavaliarStatus(missao);

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );

        // O ninja continua vinculado, mesmo sem qualificação
        assertTrue(ninjaRepository.existsByMissoes_Id(missaoId));
    }

    @Test
    void deveDisponibilizarMissaoQuandoNaoExistiremNinjasVinculados() {

        // ARRANGE - Criar missão inicialmente em andamento
        MissoesModel missao = criarMissao(
                "Missão sem ninjas",
                Rank.B,
                StatusMissao.EM_ANDAMENTO
        );

        Long missaoId = missao.getId();

        // Confirmar que não existem ninjas vinculados
        assertFalse(ninjaRepository.existsByMissoes_Id(missaoId));

        // ACT - Reavaliar o status
        missaoStatusService.reavaliarStatus(missao);

        // ASSERT
        entityManager.flush();
        entityManager.clear();

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
    void devePreservarStatusDeMissaoInativa() {

        // ARRANGE - Criar missão inativa
        MissoesModel missao = criarMissao(
                "Missão inativa",
                Rank.B,
                StatusMissao.INATIVA
        );

        NinjaModel ninja = criarNinja(
                "Ninja qualificado",
                "status.inativa@example.com",
                Rank.A
        );

        Long missaoId = missao.getId();

        // Criar vínculo diretamente para testar a proteção do status
        ninja.setMissoes(missao);
        ninjaRepository.saveAndFlush(ninja);

        // ACT - Tentar reavaliar missão inativa
        missaoStatusService.reavaliarStatus(missao);

        // ASSERT - Reconsultar o banco
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(
                StatusMissao.INATIVA,
                missaoAtualizada.getStatus()
        );

        assertTrue(ninjaRepository.existsByMissoes_Id(missaoId));
    }

    @Test
    void devePreservarStatusDeMissaoConcluida() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão concluída",
                Rank.B,
                StatusMissao.CONCLUIDA
        );

        NinjaModel ninja = criarNinja(
                "Ninja qualificado",
                "status.concluida@example.com",
                Rank.A
        );

        Long missaoId = missao.getId();

        // Criar vínculo diretamente para testar a proteção
        ninja.setMissoes(missao);
        ninjaRepository.saveAndFlush(ninja);

        // ACT
        missaoStatusService.reavaliarStatus(missao);

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(
                StatusMissao.CONCLUIDA,
                missaoAtualizada.getStatus()
        );

        assertTrue(ninjaRepository.existsByMissoes_Id(missaoId));
    }

    @Test
    void deveIniciarMissaoQuandoPeloMenosUmNinjaForQualificado() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão com múltiplos ninjas",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja1 = criarNinja(
                "Ninja Rank D",
                "status.multiplo.d@example.com",
                Rank.D
        );

        NinjaModel ninja2 = criarNinja(
                "Ninja Rank C",
                "status.multiplo.c@example.com",
                Rank.C
        );

        NinjaModel ninja3 = criarNinja(
                "Ninja Rank A",
                "status.multiplo.a@example.com",
                Rank.A
        );

        Long missaoId = missao.getId();

        // Vincular os três ninjas
        ninja1.setMissoes(missao);
        ninja2.setMissoes(missao);
        ninja3.setMissoes(missao);

        ninjaRepository.saveAndFlush(ninja1);
        ninjaRepository.saveAndFlush(ninja2);
        ninjaRepository.saveAndFlush(ninja3);

        // ACT - Reavaliar o status
        missaoStatusService.reavaliarStatus(missao);

        // ASSERT - Consultar novamente o banco
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missaoAtualizada.getStatus()
        );

        assertEquals(
                3,
                ninjaRepository.findByMissoes_Id(missaoId).size()
        );
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void naoDeveReavaliarStatusSemTransacaoAtiva() {

        // ARRANGE - Criar uma missão apenas em memória
        MissoesModel missao = new MissoesModel();
        missao.setNome("Missão sem transação");
        missao.setRank(Rank.B);
        missao.setStatus(StatusMissao.DISPONIVEL);

        // ACT + ASSERT - Verificar exigência de transação
        assertThrows(
                IllegalTransactionStateException.class,
                () -> missaoStatusService.reavaliarStatus(missao)
        );

        // O status não deve ter sido alterado
        assertEquals(
                StatusMissao.DISPONIVEL,
                missao.getStatus()
        );
    }

    @Test
    void deveManterMissaoEmAndamentoQuandoExisteNinjaQualificado() {

        // ARRANGE - Criar missão em andamento
        MissoesModel missao = criarMissao(
                "Missão em andamento com ninja qualificado",
                Rank.B,
                StatusMissao.EM_ANDAMENTO
        );

        NinjaModel ninja = criarNinja(
                "Ninja qualificado",
                "status.mantem.andamento@example.com",
                Rank.A
        );

        Long missaoId = missao.getId();
        Long ninjaId = ninja.getId();

        // Vincular ninja à missão
        ninja.setMissoes(missao);
        ninjaRepository.saveAndFlush(ninja);

        // Confirmar a situação inicial
        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missao.getStatus()
        );

        // ACT - Reavaliar status
        missaoStatusService.reavaliarStatus(missao);

        // ASSERT - Consultar novamente o banco
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        // Missão permanece em andamento
        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missaoAtualizada.getStatus()
        );

        // Ninja continua vinculado
        assertNotNull(ninjaAtualizado.getMissoes());

        assertEquals(
                missaoId,
                ninjaAtualizado.getMissoes().getId()
        );
    }

    @Test
    void deveDisponibilizarMissaoQuandoRestamApenasNinjasNaoQualificados() {

        // ARRANGE - Criar missão em andamento
        MissoesModel missao = criarMissao(
                "Missão sem qualificados restantes",
                Rank.A,
                StatusMissao.EM_ANDAMENTO
        );

        NinjaModel ninja1 = criarNinja(
                "Ninja Rank D",
                "status.restante.d@example.com",
                Rank.D
        );

        NinjaModel ninja2 = criarNinja(
                "Ninja Rank C",
                "status.restante.c@example.com",
                Rank.C
        );

        Long missaoId = missao.getId();
        Long ninja1Id = ninja1.getId();
        Long ninja2Id = ninja2.getId();

        // Vincular os ninjas não qualificados
        ninja1.setMissoes(missao);
        ninja2.setMissoes(missao);

        ninjaRepository.saveAndFlush(ninja1);
        ninjaRepository.saveAndFlush(ninja2);

        // Confirmar a situação inicial
        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missao.getStatus()
        );

        assertEquals(
                2,
                ninjaRepository.findByMissoes_Id(missaoId).size()
        );

        // ACT - Reavaliar status
        missaoStatusService.reavaliarStatus(missao);

        // ASSERT - Consultar novamente o banco
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

        // Missão volta a ficar disponível
        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );

        // Ambos os ninjas continuam vinculados
        assertNotNull(ninja1Atualizado.getMissoes());
        assertNotNull(ninja2Atualizado.getMissoes());

        assertEquals(
                missaoId,
                ninja1Atualizado.getMissoes().getId()
        );

        assertEquals(
                missaoId,
                ninja2Atualizado.getMissoes().getId()
        );

        // Os dois vínculos permanecem no banco
        assertEquals(
                2,
                ninjaRepository.findByMissoes_Id(missaoId).size()
        );
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
            Rank rank) {

        NinjaModel ninja = new NinjaModel();
        ninja.setNome(nome);
        ninja.setEmail(email);
        ninja.setIdade(25);
        ninja.setRank(rank);

        return ninjaRepository.saveAndFlush(ninja);
    }

}