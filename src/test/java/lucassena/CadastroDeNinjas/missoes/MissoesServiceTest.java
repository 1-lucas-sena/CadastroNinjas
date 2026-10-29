package lucassena.CadastroDeNinjas.missoes;

import jakarta.persistence.EntityManager;
import lucassena.CadastroDeNinjas.exceptions.RecursoNaoEncontradoException;
import lucassena.CadastroDeNinjas.exceptions.RegraDeNegocioException;
import lucassena.CadastroDeNinjas.ninjas.NinjaModel;
import lucassena.CadastroDeNinjas.ninjas.NinjaRepository;
import lucassena.CadastroDeNinjas.util.Rank;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MissoesServiceTest {

    @Autowired
    private MissoesService missoesService;

    @Autowired
    private MissoesRepository missoesRepository;

    @Autowired
    private NinjaRepository ninjaRepository;

    @Autowired
    private EntityManager entityManager;

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
    void deveListarMissoesCadastradas() {

        // ARRANGE - Criar duas missões
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

        // ACT - Listar missões
        List<MissoesResponseDTO> resultado = missoesService.listarMissoes();

        // ASSERT - Verificar quantidade
        assertEquals(2, resultado.size());

        // Verificar os dados da primeira missão
        assertTrue(resultado.stream()
                .anyMatch(missao ->
                        missao.getId().equals(missao1.getId())
                                && missao.getNome().equals("Missão de resgate")
                                && missao.getRank() == Rank.B
                                && missao.getStatus() == StatusMissao.DISPONIVEL
                ));

        // Verificar os dados da segunda missão
        assertTrue(resultado.stream()
                .anyMatch(missao ->
                        missao.getId().equals(missao2.getId())
                                && missao.getNome().equals("Missão de reconhecimento")
                                && missao.getRank() == Rank.A
                                && missao.getStatus() == StatusMissao.INATIVA
                ));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremMissoes() {

        // ARRANGE - Confirmar que não existem missões
        assertEquals(0, missoesRepository.count());

        // ACT
        List<MissoesResponseDTO> resultado = missoesService.listarMissoes();

        // ASSERT
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarMissaoPorIdExistente() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão de proteção",
                Rank.A,
                StatusMissao.DISPONIVEL
        );

        Long missaoId = missao.getId();

        // ACT
        MissoesResponseDTO resultado =
                missoesService.listarMissoesPorId(missaoId);

        // ASSERT
        assertNotNull(resultado);

        assertEquals(missaoId, resultado.getId());
        assertEquals("Missão de proteção", resultado.getNome());
        assertEquals(Rank.A, resultado.getRank());
        assertEquals(StatusMissao.DISPONIVEL, resultado.getStatus());
    }

    @Test
    void naoDeveBuscarMissaoPorIdInexistente() {

        // ARRANGE - Obter um ID inexistente
        Long missaoIdInexistente = missoesRepository.findAll().stream()
                .map(MissoesModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(missoesRepository.existsById(missaoIdInexistente));

        // ACT + ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> missoesService.listarMissoesPorId(missaoIdInexistente)
        );

        assertEquals(
                "Missão não encontrada",
                excecao.getMessage()
        );

        // Confirmar que a consulta não criou registros
        assertFalse(missoesRepository.existsById(missaoIdInexistente));
    }

    @Test
    void deveCriarMissaoComDadosValidos() {

        // ARRANGE - Preparar dados da missão
        MissoesRequestDTO dto = new MissoesRequestDTO(
                "Missão de resgate",
                Rank.B
        );

        long quantidadeAntes = missoesRepository.count();

        // ACT - Criar missão pelo service
        MissoesResponseDTO resultado =
                missoesService.criarMissao(dto);

        // ASSERT - Verificar o retorno
        assertNotNull(resultado);
        assertNotNull(resultado.getId());

        assertEquals("Missão de resgate", resultado.getNome());
        assertEquals(Rank.B, resultado.getRank());
        assertEquals(StatusMissao.DISPONIVEL, resultado.getStatus());

        // Verificar persistência
        entityManager.flush();
        entityManager.clear();

        assertEquals(
                quantidadeAntes + 1,
                missoesRepository.count()
        );

        MissoesModel missaoSalva = missoesRepository
                .findById(resultado.getId())
                .orElseThrow();

        assertEquals("Missão de resgate", missaoSalva.getNome());
        assertEquals(Rank.B, missaoSalva.getRank());
        assertEquals(StatusMissao.DISPONIVEL, missaoSalva.getStatus());
    }

    @Test
    void deveCriarMissoesComStatusInicialDisponivel() {

        // ARRANGE - Preparar missões com ranks diferentes
        MissoesRequestDTO missaoRankD = new MissoesRequestDTO(
                "Missão simples",
                Rank.D
        );

        MissoesRequestDTO missaoRankS = new MissoesRequestDTO(
                "Missão extremamente difícil",
                Rank.S
        );

        // ACT - Criar as duas missões
        MissoesResponseDTO resultadoD =
                missoesService.criarMissao(missaoRankD);

        MissoesResponseDTO resultadoS =
                missoesService.criarMissao(missaoRankS);

        // ASSERT - Verificar status inicial
        assertEquals(
                StatusMissao.DISPONIVEL,
                resultadoD.getStatus()
        );

        assertEquals(
                StatusMissao.DISPONIVEL,
                resultadoS.getStatus()
        );

        // Confirmar os dados persistidos
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoD = missoesRepository
                .findById(resultadoD.getId())
                .orElseThrow();

        MissoesModel missaoS = missoesRepository
                .findById(resultadoS.getId())
                .orElseThrow();

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoD.getStatus()
        );

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoS.getStatus()
        );

        assertEquals(Rank.D, missaoD.getRank());
        assertEquals(Rank.S, missaoS.getRank());
    }

    @Test
    void deveAtualizarNomeERankDaMissao() {

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

        // ACT
        MissoesResponseDTO resultado =
                missoesService.atualizarMissao(missaoId, dto);

        // ASSERT - Verificar o DTO
        assertEquals(missaoId, resultado.getId());
        assertEquals("Missão atualizada", resultado.getNome());
        assertEquals(Rank.A, resultado.getRank());
        assertEquals(StatusMissao.DISPONIVEL, resultado.getStatus());

        // Verificar os dados persistidos
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
    void naoDeveAtualizarMissaoInexistente() {

        // ARRANGE
        Long missaoIdInexistente = missoesRepository.findAll().stream()
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
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> missoesService.atualizarMissao(
                        missaoIdInexistente, dto
                )
        );

        assertEquals("Missão não encontrada", excecao.getMessage());

        entityManager.flush();
        entityManager.clear();

        assertEquals(quantidadeAntes, missoesRepository.count());
        assertFalse(missoesRepository.existsById(missaoIdInexistente));
    }

    @Test
    void deveIniciarMissaoAoDiminuirRankExigido() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão de rank alto",
                Rank.A,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = new NinjaModel();
        ninja.setNome("Ninja Rank B");
        ninja.setEmail("missao.diminuir.rank@example.com");
        ninja.setIdade(25);
        ninja.setRank(Rank.B);
        ninja.setMissoes(missao);

        ninja = ninjaRepository.saveAndFlush(ninja);

        Long missaoId = missao.getId();
        Long ninjaId = ninja.getId();

        assertEquals(
                StatusMissao.DISPONIVEL,
                missao.getStatus()
        );

        MissoesRequestDTO dto = new MissoesRequestDTO(
                "Missão com exigência reduzida",
                Rank.C
        );

        // ACT
        missoesService.atualizarMissao(missaoId, dto);

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId).orElseThrow();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId).orElseThrow();

        assertEquals(Rank.C, missaoAtualizada.getRank());
        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missaoAtualizada.getStatus()
        );

        assertNotNull(ninjaAtualizado.getMissoes());
        assertEquals(missaoId, ninjaAtualizado.getMissoes().getId());
    }

    @Test
    void deveDisponibilizarMissaoAoAumentarRankExigido() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão de exigência baixa",
                Rank.C,
                StatusMissao.EM_ANDAMENTO
        );

        NinjaModel ninja = new NinjaModel();
        ninja.setNome("Ninja Rank B");
        ninja.setEmail("missao.aumentar.rank@example.com");
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

        // ACT
        missoesService.atualizarMissao(missaoId, dto);

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId).orElseThrow();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId).orElseThrow();

        assertEquals(Rank.A, missaoAtualizada.getRank());
        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );

        // O ninja permanece vinculado
        assertNotNull(ninjaAtualizado.getMissoes());
        assertEquals(missaoId, ninjaAtualizado.getMissoes().getId());
    }

    @Test
    void deveManterMissaoEmAndamentoQuandoNinjaContinuaQualificado() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão em andamento",
                Rank.C,
                StatusMissao.EM_ANDAMENTO
        );

        NinjaModel ninja = new NinjaModel();
        ninja.setNome("Ninja Rank A");
        ninja.setEmail("missao.mantem.qualificado@example.com");
        ninja.setIdade(25);
        ninja.setRank(Rank.A);
        ninja.setMissoes(missao);

        ninja = ninjaRepository.saveAndFlush(ninja);

        Long missaoId = missao.getId();
        Long ninjaId = ninja.getId();

        MissoesRequestDTO dto = new MissoesRequestDTO(
                "Missão atualizada com ninja qualificado",
                Rank.B
        );

        // ACT
        MissoesResponseDTO resultado =
                missoesService.atualizarMissao(missaoId, dto);

        // ASSERT - Verificar retorno
        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                resultado.getStatus()
        );

        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId).orElseThrow();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId).orElseThrow();

        assertEquals("Missão atualizada com ninja qualificado",
                missaoAtualizada.getNome());

        assertEquals(Rank.B, missaoAtualizada.getRank());

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missaoAtualizada.getStatus()
        );

        assertNotNull(ninjaAtualizado.getMissoes());
        assertEquals(missaoId, ninjaAtualizado.getMissoes().getId());
    }

    @Test
    void deveAtualizarMissaoInativaSemAlterarStatus() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão inativa original",
                Rank.D,
                StatusMissao.INATIVA
        );

        Long missaoId = missao.getId();

        MissoesRequestDTO dto = new MissoesRequestDTO(
                "Missão inativa atualizada",
                Rank.A
        );

        // ACT
        MissoesResponseDTO resultado =
                missoesService.atualizarMissao(missaoId, dto);

        // ASSERT
        assertEquals(
                StatusMissao.INATIVA,
                resultado.getStatus()
        );

        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId).orElseThrow();

        assertEquals("Missão inativa atualizada", missaoAtualizada.getNome());
        assertEquals(Rank.A, missaoAtualizada.getRank());
        assertEquals(StatusMissao.INATIVA, missaoAtualizada.getStatus());
    }

    @Test
    void deveAtualizarMissaoConcluidaSemAlterarStatus() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão concluída original",
                Rank.B,
                StatusMissao.CONCLUIDA
        );

        Long missaoId = missao.getId();

        MissoesRequestDTO dto = new MissoesRequestDTO(
                "Missão concluída atualizada",
                Rank.S
        );

        // ACT
        MissoesResponseDTO resultado =
                missoesService.atualizarMissao(missaoId, dto);

        // ASSERT
        assertEquals(
                StatusMissao.CONCLUIDA,
                resultado.getStatus()
        );

        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId).orElseThrow();

        assertEquals("Missão concluída atualizada", missaoAtualizada.getNome());
        assertEquals(Rank.S, missaoAtualizada.getRank());
        assertEquals(
                StatusMissao.CONCLUIDA,
                missaoAtualizada.getStatus()
        );
    }

    @Test
    void deveExcluirMissaoSemNinjasVinculados() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão para exclusão",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        Long missaoId = missao.getId();

        assertTrue(missoesRepository.existsById(missaoId));
        assertFalse(ninjaRepository.existsByMissoes_Id(missaoId));

        long quantidadeAntes = missoesRepository.count();

        // ACT
        assertDoesNotThrow(
                () -> missoesService.deletarMissao(missaoId)
        );

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        assertFalse(missoesRepository.existsById(missaoId));

        assertEquals(
                quantidadeAntes - 1,
                missoesRepository.count()
        );
    }

    @Test
    void naoDeveExcluirMissaoComNinjasVinculados() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão com ninja vinculado",
                Rank.B,
                StatusMissao.DISPONIVEL
        );

        NinjaModel ninja = new NinjaModel();
        ninja.setNome("Ninja vinculado");
        ninja.setEmail("ninja.exclusao.bloqueada@example.com");
        ninja.setIdade(25);
        ninja.setRank(Rank.D);
        ninja.setMissoes(missao);

        ninja = ninjaRepository.saveAndFlush(ninja);

        Long missaoId = missao.getId();
        Long ninjaId = ninja.getId();

        long quantidadeAntes = missoesRepository.count();

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> missoesService.deletarMissao(missaoId)
        );

        assertEquals(
                "Não é possível excluir uma missão que possui ninjas vinculados",
                excecao.getMessage()
        );

        // ASSERT - Consultar novamente o banco
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );

        assertNotNull(ninjaAtualizado.getMissoes());

        assertEquals(
                missaoId,
                ninjaAtualizado.getMissoes().getId()
        );

        assertEquals(
                quantidadeAntes,
                missoesRepository.count()
        );
    }

    @Test
    void naoDeveExcluirMissaoInexistente() {

        // ARRANGE
        Long missaoIdInexistente = missoesRepository.findAll().stream()
                .map(MissoesModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(missoesRepository.existsById(missaoIdInexistente));

        long quantidadeAntes = missoesRepository.count();

        // ACT + ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> missoesService.deletarMissao(missaoIdInexistente)
        );

        assertEquals(
                "Missão não encontrada",
                excecao.getMessage()
        );

        entityManager.flush();
        entityManager.clear();

        assertFalse(missoesRepository.existsById(missaoIdInexistente));
        assertEquals(quantidadeAntes, missoesRepository.count());
    }

    @Test
    void deveAtivarMissaoInativa() {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão para ativação",
                Rank.A,
                StatusMissao.INATIVA
        );

        Long missaoId = missao.getId();

        // ACT
        MissoesResponseDTO resultado =
                missoesService.ativarMissao(missaoId);

        // ASSERT - Verificar retorno
        assertEquals(missaoId, resultado.getId());
        assertEquals("Missão para ativação", resultado.getNome());
        assertEquals(Rank.A, resultado.getRank());

        assertEquals(
                StatusMissao.DISPONIVEL,
                resultado.getStatus()
        );

        // Confirmar persistência
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );

        assertEquals("Missão para ativação", missaoAtualizada.getNome());
        assertEquals(Rank.A, missaoAtualizada.getRank());
    }

    @ParameterizedTest
    @EnumSource(
            value = StatusMissao.class,
            names = {"DISPONIVEL", "EM_ANDAMENTO", "CONCLUIDA"}
    )
    void naoDeveAtivarMissaoQueNaoEstaInativa(StatusMissao statusInicial) {

        // ARRANGE
        MissoesModel missao = criarMissao(
                "Missão com ativação proibida",
                Rank.B,
                statusInicial
        );

        Long missaoId = missao.getId();

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> missoesService.ativarMissao(missaoId)
        );

        assertEquals(
                "Só é possível ativar uma missão inativa",
                excecao.getMessage()
        );

        // ASSERT - Verificar que o status foi preservado
        entityManager.flush();
        entityManager.clear();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(
                statusInicial,
                missaoAtualizada.getStatus()
        );
    }

    @Test
    void naoDeveAtivarMissaoInexistente() {

        // ARRANGE
        Long missaoIdInexistente = missoesRepository.findAll().stream()
                .map(MissoesModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(missoesRepository.existsById(missaoIdInexistente));

        // ACT + ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> missoesService.ativarMissao(missaoIdInexistente)
        );

        assertEquals(
                "Missão não encontrada",
                excecao.getMessage()
        );

        assertFalse(missoesRepository.existsById(missaoIdInexistente));
    }









}