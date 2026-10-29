package lucassena.CadastroDeNinjas.ninjas;

import jakarta.persistence.EntityManager;
import lucassena.CadastroDeNinjas.exceptions.RecursoNaoEncontradoException;
import lucassena.CadastroDeNinjas.exceptions.RegraDeNegocioException;
import lucassena.CadastroDeNinjas.missoes.MissoesModel;
import lucassena.CadastroDeNinjas.missoes.MissoesRepository;
import lucassena.CadastroDeNinjas.missoes.StatusMissao;
import lucassena.CadastroDeNinjas.util.Rank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NinjaServiceTest {

    @Autowired
    private NinjaService ninjaService;

    @Autowired
    private NinjaRepository ninjaRepository;

    @Autowired
    private MissoesRepository missoesRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveListarNinjasCadastrados() {

        // ARRANGE
        NinjaModel ninja1 = criarNinja(
                "Naruto",
                "naruto.listagem@example.com",
                17,
                Rank.A
        );

        NinjaModel ninja2 = criarNinja(
                "Sasuke",
                "sasuke.listagem@example.com",
                18,
                Rank.S
        );

        // ACT
        List<NinjaResponseDTO> resultado = ninjaService.listarNinjas();

        // ASSERT
        assertEquals(2, resultado.size());

        assertTrue(resultado.stream()
                .anyMatch(ninja ->
                        ninja.getId().equals(ninja1.getId())
                                && ninja.getNome().equals("Naruto")
                                && ninja.getRank() == Rank.A
                ));

        assertTrue(resultado.stream()
                .anyMatch(ninja ->
                        ninja.getId().equals(ninja2.getId())
                                && ninja.getNome().equals("Sasuke")
                                && ninja.getRank() == Rank.S
                ));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremNinjas() {

        // ARRANGE
        assertEquals(0, ninjaRepository.count());

        // ACT
        List<NinjaResponseDTO> resultado = ninjaService.listarNinjas();

        // ASSERT
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarNinjaPorIdExistente() {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Kakashi",
                "kakashi.busca@example.com",
                30,
                Rank.A
        );

        Long ninjaId = ninja.getId();

        // ACT
        NinjaResponseDTO resultado = ninjaService.listarNinjasPorId(ninjaId);

        // ASSERT
        assertNotNull(resultado);

        assertEquals(ninjaId, resultado.getId());
        assertEquals("Kakashi", resultado.getNome());
        assertEquals("kakashi.busca@example.com", resultado.getEmail());
        assertEquals(30, resultado.getIdade());
        assertEquals(Rank.A, resultado.getRank());
        assertNull(resultado.getMissaoId());
    }

    @Test
    void naoDeveBuscarNinjaPorIdInexistente() {

        // ARRANGE - Encontrar um ID que não existe
        Long ninjaIdInexistente = ninjaRepository.findAll().stream()
                .map(NinjaModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(ninjaRepository.existsById(ninjaIdInexistente));

        // ACT + ASSERT - Buscar ninja inexistente
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ninjaService.listarNinjasPorId(ninjaIdInexistente)
        );

        assertEquals(
                "Ninja não encontrado",
                excecao.getMessage()
        );

        // Confirmar que nenhum ninja foi criado
        assertFalse(ninjaRepository.existsById(ninjaIdInexistente));
    }

    @Test
    void deveCriarNinjaComDadosValidos() {

        // ARRANGE - Preparar os dados de cadastro
        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Naruto",
                "naruto.cadastro@example.com",
                17,
                Rank.A
        );

        long quantidadeAntes = ninjaRepository.count();

        // ACT - Cadastrar o ninja
        NinjaResponseDTO resultado = ninjaService.criarNinja(dto);

        // ASSERT - Verificar o retorno
        assertNotNull(resultado);
        assertNotNull(resultado.getId());

        assertEquals("Naruto", resultado.getNome());
        assertEquals("naruto.cadastro@example.com", resultado.getEmail());
        assertEquals(17, resultado.getIdade());
        assertEquals(Rank.A, resultado.getRank());
        assertNull(resultado.getMissaoId());

        // Confirmar que um registro foi adicionado
        entityManager.flush();
        entityManager.clear();

        assertEquals(quantidadeAntes + 1, ninjaRepository.count());

        // Confirmar os dados persistidos
        NinjaModel ninjaSalvo = ninjaRepository
                .findById(resultado.getId())
                .orElseThrow();

        assertEquals("Naruto", ninjaSalvo.getNome());
        assertEquals("naruto.cadastro@example.com", ninjaSalvo.getEmail());
        assertEquals(17, ninjaSalvo.getIdade());
        assertEquals(Rank.A, ninjaSalvo.getRank());
    }

    @Test
    void deveNormalizarEmailMaiusculoAoCriarNinja() {

        // ARRANGE
        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Sasuke",
                "SASUKE.CADASTRO@EXAMPLE.COM",
                18,
                Rank.S
        );

        // ACT
        NinjaResponseDTO resultado = ninjaService.criarNinja(dto);

        // ASSERT - Verificar o e-mail retornado
        assertEquals(
                "sasuke.cadastro@example.com",
                resultado.getEmail()
        );

        // Verificar o e-mail persistido
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(resultado.getId())
                .orElseThrow();

        assertEquals(
                "sasuke.cadastro@example.com",
                ninjaSalvo.getEmail()
        );
    }

    @Test
    void deveRemoverEspacosDoEmailAoCriarNinja() {

        // ARRANGE
        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Sakura",
                "   sakura.cadastro@example.com   ",
                17,
                Rank.B
        );

        // ACT
        NinjaResponseDTO resultado = ninjaService.criarNinja(dto);

        // ASSERT - Verificar o e-mail normalizado
        assertEquals(
                "sakura.cadastro@example.com",
                resultado.getEmail()
        );

        // Consultar novamente o banco
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(resultado.getId())
                .orElseThrow();

        assertEquals(
                "sakura.cadastro@example.com",
                ninjaSalvo.getEmail()
        );
    }

    @Test
    void naoDeveCriarNinjaComEmailJaCadastrado() {

        // ARRANGE - Criar primeiro ninja
        NinjaRequestDTO primeiroDTO = new NinjaRequestDTO(
                "Naruto",
                "email.duplicado@example.com",
                17,
                Rank.A
        );

        ninjaService.criarNinja(primeiroDTO);

        long quantidadeAntes = ninjaRepository.count();

        // Preparar outro ninja com o mesmo e-mail
        NinjaRequestDTO segundoDTO = new NinjaRequestDTO(
                "Sasuke",
                "email.duplicado@example.com",
                18,
                Rank.S
        );

        // ACT + ASSERT - Tentar cadastrar
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaService.criarNinja(segundoDTO)
        );

        assertEquals(
                "Email já cadastrado",
                excecao.getMessage()
        );

        // Verificar que nenhum segundo registro foi criado
        entityManager.flush();
        entityManager.clear();

        assertEquals(
                quantidadeAntes,
                ninjaRepository.count()
        );

        assertTrue(
                ninjaRepository.existsByEmail("email.duplicado@example.com")
        );
    }

    @Test
    void naoDeveCriarNinjaComEmailDuplicadoAposNormalizacao() {

        // ARRANGE - Cadastrar primeiro ninja
        NinjaRequestDTO primeiroDTO = new NinjaRequestDTO(
                "Kakashi",
                "kakashi.duplicado@example.com",
                30,
                Rank.A
        );

        NinjaResponseDTO primeiroNinja =
                ninjaService.criarNinja(primeiroDTO);

        Long primeiroNinjaId = primeiroNinja.getId();

        long quantidadeAntes = ninjaRepository.count();

        // Mesmo e-mail, mas com maiúsculas e espaços
        NinjaRequestDTO segundoDTO = new NinjaRequestDTO(
                "Obito",
                "   KAKASHI.DUPLICADO@EXAMPLE.COM   ",
                31,
                Rank.S
        );

        // ACT + ASSERT - Tentar cadastrar segundo ninja
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaService.criarNinja(segundoDTO)
        );

        assertEquals(
                "Email já cadastrado",
                excecao.getMessage()
        );

        // ASSERT - Verificar integridade dos dados
        entityManager.flush();
        entityManager.clear();

        assertEquals(
                quantidadeAntes,
                ninjaRepository.count()
        );

        NinjaModel ninjaOriginal = ninjaRepository
                .findById(primeiroNinjaId)
                .orElseThrow();

        // O primeiro cadastro permanece inalterado
        assertEquals("Kakashi", ninjaOriginal.getNome());

        assertEquals(
                "kakashi.duplicado@example.com",
                ninjaOriginal.getEmail()
        );

        assertEquals(30, ninjaOriginal.getIdade());
        assertEquals(Rank.A, ninjaOriginal.getRank());
    }

    @Test
    void deveAtualizarDadosDeNinjaExistente() {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Naruto",
                "naruto.antigo@example.com",
                17,
                Rank.D
        );

        Long ninjaId = ninja.getId();

        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Naruto Uzumaki",
                "naruto.novo@example.com",
                18,
                Rank.A
        );

        // ACT
        NinjaResponseDTO resultado =
                ninjaService.atualizarNinja(ninjaId, dto);

        // ASSERT - Verificar retorno
        assertEquals(ninjaId, resultado.getId());
        assertEquals("Naruto Uzumaki", resultado.getNome());
        assertEquals("naruto.novo@example.com", resultado.getEmail());
        assertEquals(18, resultado.getIdade());
        assertEquals(Rank.A, resultado.getRank());

        // Confirmar persistência
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertEquals("Naruto Uzumaki", ninjaAtualizado.getNome());
        assertEquals("naruto.novo@example.com", ninjaAtualizado.getEmail());
        assertEquals(18, ninjaAtualizado.getIdade());
        assertEquals(Rank.A, ninjaAtualizado.getRank());
    }

    @Test
    void naoDeveAtualizarNinjaInexistente() {

        // ARRANGE
        Long ninjaIdInexistente = ninjaRepository.findAll().stream()
                .map(NinjaModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(ninjaRepository.existsById(ninjaIdInexistente));

        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Ninja Inexistente",
                "inexistente@example.com",
                25,
                Rank.B
        );

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        RecursoNaoEncontradoException excecao = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ninjaService.atualizarNinja(ninjaIdInexistente, dto)
        );

        assertEquals("Ninja não encontrado", excecao.getMessage());

        entityManager.flush();
        entityManager.clear();

        assertEquals(quantidadeAntes, ninjaRepository.count());
        assertFalse(ninjaRepository.existsById(ninjaIdInexistente));
    }

    @Test
    void devePermitirAtualizarNinjaMantendoProprioEmail() {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Sasuke",
                "sasuke.proprio@example.com",
                18,
                Rank.B
        );

        Long ninjaId = ninja.getId();

        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Sasuke Uchiha",
                "sasuke.proprio@example.com",
                19,
                Rank.A
        );

        // ACT
        NinjaResponseDTO resultado =
                ninjaService.atualizarNinja(ninjaId, dto);

        // ASSERT
        assertEquals(ninjaId, resultado.getId());
        assertEquals("Sasuke Uchiha", resultado.getNome());
        assertEquals("sasuke.proprio@example.com", resultado.getEmail());

        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertEquals("Sasuke Uchiha", ninjaAtualizado.getNome());
        assertEquals("sasuke.proprio@example.com", ninjaAtualizado.getEmail());
        assertEquals(19, ninjaAtualizado.getIdade());
        assertEquals(Rank.A, ninjaAtualizado.getRank());
    }

    @Test
    void naoDeveAtualizarNinjaComEmailDeOutroNinja() {

        // ARRANGE
        NinjaModel ninja1 = criarNinja(
                "Naruto",
                "naruto.unico@example.com",
                17,
                Rank.A
        );

        NinjaModel ninja2 = criarNinja(
                "Sasuke",
                "sasuke.unico@example.com",
                18,
                Rank.B
        );

        Long ninja2Id = ninja2.getId();

        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Sasuke Atualizado",
                ninja1.getEmail(),
                20,
                Rank.S
        );

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaService.atualizarNinja(ninja2Id, dto)
        );

        assertEquals("Email já cadastrado", excecao.getMessage());

        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninja2Id)
                .orElseThrow();

        assertEquals("Sasuke", ninjaAtualizado.getNome());
        assertEquals("sasuke.unico@example.com", ninjaAtualizado.getEmail());
        assertEquals(18, ninjaAtualizado.getIdade());
        assertEquals(Rank.B, ninjaAtualizado.getRank());
    }

    @Test
    void deveNormalizarEmailAoAtualizarNinja() {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Sakura",
                "sakura.antigo@example.com",
                17,
                Rank.B
        );

        Long ninjaId = ninja.getId();

        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Sakura Haruno",
                "   SAKURA.NOVO@EXAMPLE.COM   ",
                18,
                Rank.A
        );

        // ACT
        NinjaResponseDTO resultado =
                ninjaService.atualizarNinja(ninjaId, dto);

        // ASSERT
        assertEquals(
                "sakura.novo@example.com",
                resultado.getEmail()
        );

        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertEquals(
                "sakura.novo@example.com",
                ninjaAtualizado.getEmail()
        );

        assertEquals("Sakura Haruno", ninjaAtualizado.getNome());
    }

    @Test
    void devePreservarDadosQuandoAtualizacaoTemEmailDuplicado() {

        // ARRANGE
        NinjaModel ninja1 = criarNinja(
                "Kakashi",
                "kakashi.original@example.com",
                30,
                Rank.A
        );

        NinjaModel ninja2 = criarNinja(
                "Obito",
                "obito.original@example.com",
                31,
                Rank.B
        );

        Long ninja1Id = ninja1.getId();
        Long ninja2Id = ninja2.getId();

        long quantidadeAntes = ninjaRepository.count();

        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Obito Modificado",
                "   KAKASHI.ORIGINAL@EXAMPLE.COM   ",
                35,
                Rank.S
        );

        // ACT + ASSERT
        RegraDeNegocioException excecao = assertThrows(
                RegraDeNegocioException.class,
                () -> ninjaService.atualizarNinja(ninja2Id, dto)
        );

        assertEquals("Email já cadastrado", excecao.getMessage());

        entityManager.flush();
        entityManager.clear();

        NinjaModel primeiroNinja = ninjaRepository
                .findById(ninja1Id)
                .orElseThrow();

        NinjaModel segundoNinja = ninjaRepository
                .findById(ninja2Id)
                .orElseThrow();

        // Primeiro ninja permanece intacto
        assertEquals("Kakashi", primeiroNinja.getNome());
        assertEquals("kakashi.original@example.com", primeiroNinja.getEmail());
        assertEquals(30, primeiroNinja.getIdade());
        assertEquals(Rank.A, primeiroNinja.getRank());

        // Segundo ninja também permanece intacto
        assertEquals("Obito", segundoNinja.getNome());
        assertEquals("obito.original@example.com", segundoNinja.getEmail());
        assertEquals(31, segundoNinja.getIdade());
        assertEquals(Rank.B, segundoNinja.getRank());

        assertEquals(quantidadeAntes, ninjaRepository.count());
    }

    @Test
    void deveIniciarMissaoAoPromoverNinjaVinculado() {

        // ARRANGE - Criar missão disponível
        MissoesModel missao = new MissoesModel();
        missao.setNome("Missão para promoção");
        missao.setRank(Rank.B);
        missao.setStatus(StatusMissao.DISPONIVEL);
        missao = missoesRepository.saveAndFlush(missao);

        // Criar ninja inicialmente não qualificado
        NinjaModel ninja = criarNinja(
                "Naruto",
                "naruto.promocao@example.com",
                17,
                Rank.D
        );

        Long ninjaId = ninja.getId();
        Long missaoId = missao.getId();

        ninja.setMissoes(missao);
        ninjaRepository.saveAndFlush(ninja);

        assertEquals(
                StatusMissao.DISPONIVEL,
                missao.getStatus()
        );

        // Preparar atualização para rank A
        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Naruto",
                "naruto.promocao@example.com",
                18,
                Rank.A
        );

        // ACT
        ninjaService.atualizarNinja(ninjaId, dto);

        // ASSERT - Reconsultar o banco
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(Rank.A, ninjaAtualizado.getRank());

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missaoAtualizada.getStatus()
        );

        assertNotNull(ninjaAtualizado.getMissoes());
        assertEquals(missaoId, ninjaAtualizado.getMissoes().getId());
    }

    @Test
    void deveDisponibilizarMissaoAoRebaixarUltimoNinjaQualificado() {

        // ARRANGE
        MissoesModel missao = new MissoesModel();
        missao.setNome("Missão para rebaixamento");
        missao.setRank(Rank.B);
        missao.setStatus(StatusMissao.EM_ANDAMENTO);
        missao = missoesRepository.saveAndFlush(missao);

        NinjaModel ninja = criarNinja(
                "Sasuke",
                "sasuke.rebaixamento@example.com",
                18,
                Rank.A
        );

        Long ninjaId = ninja.getId();
        Long missaoId = missao.getId();

        ninja.setMissoes(missao);
        ninjaRepository.saveAndFlush(ninja);

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missao.getStatus()
        );

        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Sasuke",
                "sasuke.rebaixamento@example.com",
                19,
                Rank.D
        );

        // ACT
        ninjaService.atualizarNinja(ninjaId, dto);

        // ASSERT
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals(Rank.D, ninjaAtualizado.getRank());

        assertEquals(
                StatusMissao.DISPONIVEL,
                missaoAtualizada.getStatus()
        );

        // O ninja permanece vinculado, mesmo não qualificado
        assertNotNull(ninjaAtualizado.getMissoes());
        assertEquals(missaoId, ninjaAtualizado.getMissoes().getId());
    }

    @Test
    void devePreservarMissaoAoAtualizarNinjaSemAlterarQualificacao() {

        // ARRANGE
        MissoesModel missao = new MissoesModel();
        missao.setNome("Missão para atualização");
        missao.setRank(Rank.B);
        missao.setStatus(StatusMissao.EM_ANDAMENTO);
        missao = missoesRepository.saveAndFlush(missao);

        NinjaModel ninja = criarNinja(
                "Kakashi",
                "kakashi.antigo.missao@example.com",
                30,
                Rank.A
        );

        Long ninjaId = ninja.getId();
        Long missaoId = missao.getId();

        ninja.setMissoes(missao);
        ninjaRepository.saveAndFlush(ninja);

        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Kakashi Hatake",
                "kakashi.novo.missao@example.com",
                31,
                Rank.A
        );

        // ACT
        NinjaResponseDTO resultado =
                ninjaService.atualizarNinja(ninjaId, dto);

        // ASSERT - Verificar retorno
        assertEquals(ninjaId, resultado.getId());
        assertEquals("Kakashi Hatake", resultado.getNome());
        assertEquals("kakashi.novo.missao@example.com", resultado.getEmail());
        assertEquals(Rank.A, resultado.getRank());
        assertEquals(missaoId, resultado.getMissaoId());

        // Verificar dados persistidos
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        MissoesModel missaoAtualizada = missoesRepository
                .findById(missaoId)
                .orElseThrow();

        assertEquals("Kakashi Hatake", ninjaAtualizado.getNome());
        assertEquals("kakashi.novo.missao@example.com", ninjaAtualizado.getEmail());
        assertEquals(31, ninjaAtualizado.getIdade());
        assertEquals(Rank.A, ninjaAtualizado.getRank());

        assertNotNull(ninjaAtualizado.getMissoes());
        assertEquals(missaoId, ninjaAtualizado.getMissoes().getId());

        assertEquals(
                StatusMissao.EM_ANDAMENTO,
                missaoAtualizada.getStatus()
        );
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
}