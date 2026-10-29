package lucassena.CadastroDeNinjas.ninjas;

import jakarta.persistence.EntityManager;
import tools.jackson.databind.json.JsonMapper;
import lucassena.CadastroDeNinjas.util.Rank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NinjaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NinjaRepository ninjaRepository;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveListarNinjasComStatus200() throws Exception {

        // ARRANGE
        NinjaModel ninja1 = criarNinja(
                "Naruto",
                "naruto.controller@example.com",
                17,
                Rank.A
        );

        NinjaModel ninja2 = criarNinja(
                "Sasuke",
                "sasuke.controller@example.com",
                18,
                Rank.S
        );

        // ACT + ASSERT
        mockMvc.perform(get("/ninjas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(ninja1.getId()))
                .andExpect(jsonPath("$[0].nome").value("Naruto"))
                .andExpect(jsonPath("$[1].id").value(ninja2.getId()))
                .andExpect(jsonPath("$[1].nome").value("Sasuke"));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremNinjas() throws Exception {

        // ARRANGE
        assertEquals(0, ninjaRepository.count());

        // ACT + ASSERT
        mockMvc.perform(get("/ninjas"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveBuscarNinjaPorIdComStatus200() throws Exception {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Kakashi",
                "kakashi.controller@example.com",
                30,
                Rank.A
        );

        Long ninjaId = ninja.getId();

        // ACT + ASSERT
        mockMvc.perform(get("/ninjas/{id}", ninjaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ninjaId))
                .andExpect(jsonPath("$.nome").value("Kakashi"))
                .andExpect(jsonPath("$.email")
                        .value("kakashi.controller@example.com"))
                .andExpect(jsonPath("$.idade").value(30))
                .andExpect(jsonPath("$.rank").value("A"))
                .andExpect(jsonPath("$.missaoId").isEmpty());
    }

    @Test
    void naoDeveBuscarNinjaPorIdInexistente() throws Exception {

        // ARRANGE
        Long ninjaIdInexistente = ninjaRepository.findAll().stream()
                .map(NinjaModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(ninjaRepository.existsById(ninjaIdInexistente));

        // ACT + ASSERT
        mockMvc.perform(get("/ninjas/{id}", ninjaIdInexistente))
                .andExpect(status().isNotFound());
    }


    @Test
    void deveCriarNinjaComStatus201() throws Exception {

        // ARRANGE
        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Naruto Uzumaki",
                "NARUTO.CADASTRO@example.com",
                17,
                Rank.A
        );

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        String resposta = mockMvc.perform(
                        post("/ninjas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Naruto Uzumaki"))
                .andExpect(jsonPath("$.email")
                        .value("naruto.cadastro@example.com"))
                .andExpect(jsonPath("$.idade").value(17))
                .andExpect(jsonPath("$.rank").value("A"))
                .andExpect(jsonPath("$.missaoId").isEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // ASSERT - Confirmar persistência
        entityManager.flush();
        entityManager.clear();

        assertEquals(quantidadeAntes + 1, ninjaRepository.count());

        NinjaModel ninjaSalvo = ninjaRepository
                .findAll()
                .stream()
                .filter(ninja ->
                        ninja.getEmail().equals("naruto.cadastro@example.com"))
                .findFirst()
                .orElseThrow();

        assertEquals("Naruto Uzumaki", ninjaSalvo.getNome());
        assertEquals(17, ninjaSalvo.getIdade());
        assertEquals(Rank.A, ninjaSalvo.getRank());
    }

    @Test
    void naoDeveCriarNinjaSemCamposObrigatorios() throws Exception {

        // ARRANGE
        String json = "{}";

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        post("/ninjas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());

        // Confirmar que nenhum ninja foi criado
        assertEquals(quantidadeAntes, ninjaRepository.count());
    }

    @Test
    void naoDeveCriarNinjaComNomeInvalido() throws Exception {

        // ARRANGE
        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Na",
                "nome.invalido@example.com",
                17,
                Rank.A
        );

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        post("/ninjas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest());

        // ASSERT
        assertEquals(quantidadeAntes, ninjaRepository.count());
    }

    @Test
    void naoDeveCriarNinjaComEmailInvalido() throws Exception {

        // ARRANGE
        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Sakura Haruno",
                "email-invalido",
                18,
                Rank.B
        );

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        post("/ninjas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest());

        // ASSERT
        assertEquals(quantidadeAntes, ninjaRepository.count());
    }

    @Test
    void naoDeveCriarNinjaComEmailDuplicado() throws Exception {

        // ARRANGE
        NinjaModel ninjaExistente = criarNinja(
                "Sasuke Uchiha",
                "sasuke.duplicado@example.com",
                18,
                Rank.A
        );

        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Outro Sasuke",
                "SASUKE.DUPLICADO@example.com",
                20,
                Rank.B
        );

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        post("/ninjas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isConflict());

        // ASSERT - Confirmar que não houve duplicação
        assertEquals(quantidadeAntes, ninjaRepository.count());

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(ninjaExistente.getId())
                .orElseThrow();

        assertEquals("Sasuke Uchiha", ninjaSalvo.getNome());
        assertEquals(
                "sasuke.duplicado@example.com",
                ninjaSalvo.getEmail()
        );
    }


    @Test
    void deveAtualizarNinjaComStatus200() throws Exception {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Naruto Uzumaki",
                "naruto.original@example.com",
                17,
                Rank.D
        );

        Long ninjaId = ninja.getId();

        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Naruto Hokage",
                "NARUTO.ATUALIZADO@example.com",
                30,
                Rank.S
        );

        // ACT + ASSERT
        mockMvc.perform(
                        put("/ninjas/{id}", ninjaId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ninjaId))
                .andExpect(jsonPath("$.nome").value("Naruto Hokage"))
                .andExpect(jsonPath("$.email")
                        .value("naruto.atualizado@example.com"))
                .andExpect(jsonPath("$.idade").value(30))
                .andExpect(jsonPath("$.rank").value("S"))
                .andExpect(jsonPath("$.missaoId").isEmpty());

        // ASSERT - Confirmar persistência
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaAtualizado = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertEquals("Naruto Hokage", ninjaAtualizado.getNome());
        assertEquals(
                "naruto.atualizado@example.com",
                ninjaAtualizado.getEmail()
        );
        assertEquals(30, ninjaAtualizado.getIdade());
        assertEquals(Rank.S, ninjaAtualizado.getRank());
    }

    @Test
    void naoDeveAtualizarNinjaInexistente() throws Exception {

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
                Rank.A
        );

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        put("/ninjas/{id}", ninjaIdInexistente)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isNotFound());

        // ASSERT - Confirmar que nada foi criado
        entityManager.flush();
        entityManager.clear();

        assertEquals(quantidadeAntes, ninjaRepository.count());
        assertFalse(ninjaRepository.existsById(ninjaIdInexistente));
    }

    @Test
    void naoDeveAtualizarNinjaComDadosInvalidos() throws Exception {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Sakura Haruno",
                "sakura.original@example.com",
                18,
                Rank.B
        );

        Long ninjaId = ninja.getId();

        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Sakura Atualizada",
                "sakura.atualizada@example.com",
                -5,
                Rank.A
        );

        // ACT + ASSERT
        mockMvc.perform(
                        put("/ninjas/{id}", ninjaId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest());

        // ASSERT - Confirmar que os dados não mudaram
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaSalvo = ninjaRepository
                .findById(ninjaId)
                .orElseThrow();

        assertEquals("Sakura Haruno", ninjaSalvo.getNome());
        assertEquals(
                "sakura.original@example.com",
                ninjaSalvo.getEmail()
        );
        assertEquals(18, ninjaSalvo.getIdade());
        assertEquals(Rank.B, ninjaSalvo.getRank());
    }

    @Test
    void naoDeveAtualizarNinjaComEmailDeOutroNinja() throws Exception {

        // ARRANGE
        NinjaModel ninjaExistente = criarNinja(
                "Sasuke Uchiha",
                "sasuke.conflito@example.com",
                20,
                Rank.A
        );

        NinjaModel ninjaParaAtualizar = criarNinja(
                "Naruto Uzumaki",
                "naruto.sem.conflito@example.com",
                19,
                Rank.B
        );

        Long ninjaExistenteId = ninjaExistente.getId();
        Long ninjaParaAtualizarId = ninjaParaAtualizar.getId();

        NinjaRequestDTO dto = new NinjaRequestDTO(
                "Naruto Atualizado",
                "SASUKE.CONFLITO@example.com",
                25,
                Rank.S
        );

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        put("/ninjas/{id}", ninjaParaAtualizarId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isConflict());

        // ASSERT - Confirmar que nenhum registro foi alterado
        entityManager.flush();
        entityManager.clear();

        NinjaModel ninjaOriginal = ninjaRepository
                .findById(ninjaExistenteId)
                .orElseThrow();

        NinjaModel ninjaNaoAtualizado = ninjaRepository
                .findById(ninjaParaAtualizarId)
                .orElseThrow();

        assertEquals(
                "sasuke.conflito@example.com",
                ninjaOriginal.getEmail()
        );

        assertEquals(
                "Naruto Uzumaki",
                ninjaNaoAtualizado.getNome()
        );

        assertEquals(
                "naruto.sem.conflito@example.com",
                ninjaNaoAtualizado.getEmail()
        );

        assertEquals(19, ninjaNaoAtualizado.getIdade());
        assertEquals(Rank.B, ninjaNaoAtualizado.getRank());

        assertEquals(quantidadeAntes, ninjaRepository.count());
    }


    @Test
    void deveExcluirNinjaComStatus204() throws Exception {

        // ARRANGE - Criar um ninja
        NinjaModel ninja = criarNinja(
                "Neji Hyuga",
                "neji.exclusao@example.com",
                18,
                Rank.B
        );

        Long ninjaId = ninja.getId();

        long quantidadeAntes = ninjaRepository.count();

        assertTrue(ninjaRepository.existsById(ninjaId));

        // ACT + ASSERT - Executar a exclusão
        mockMvc.perform(
                        delete("/ninjas/{id}", ninjaId)
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // ASSERT - Confirmar exclusão no banco
        entityManager.flush();
        entityManager.clear();

        assertFalse(ninjaRepository.existsById(ninjaId));

        assertEquals(
                quantidadeAntes - 1,
                ninjaRepository.count()
        );
    }

    @Test
    void naoDeveExcluirNinjaInexistente() throws Exception {

        // ARRANGE - Encontrar um ID inexistente
        Long ninjaIdInexistente = ninjaRepository.findAll()
                .stream()
                .map(NinjaModel::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1L;

        assertFalse(ninjaRepository.existsById(ninjaIdInexistente));

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        delete("/ninjas/{id}", ninjaIdInexistente)
                )
                .andExpect(status().isNotFound());

        // ASSERT - Verificar que nenhum ninja foi excluído
        entityManager.flush();
        entityManager.clear();

        assertEquals(
                quantidadeAntes,
                ninjaRepository.count()
        );

        assertFalse(
                ninjaRepository.existsById(ninjaIdInexistente)
        );
    }


    @Test
    void deveRetornarContratoJsonCorretoAoConsultarNinja() throws Exception {

        // ARRANGE
        NinjaModel ninja = criarNinja(
                "Minato Namikaze",
                "minato.contrato@example.com",
                24,
                Rank.S
        );

        Long ninjaId = ninja.getId();

        // ACT + ASSERT
        mockMvc.perform(get("/ninjas/{id}", ninjaId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))

                // Identificação
                .andExpect(jsonPath("$.id").value(ninjaId))
                .andExpect(jsonPath("$.id").isNumber())

                // Nome
                .andExpect(jsonPath("$.nome").value("Minato Namikaze"))
                .andExpect(jsonPath("$.nome").isString())

                // E-mail
                .andExpect(jsonPath("$.email")
                        .value("minato.contrato@example.com"))
                .andExpect(jsonPath("$.email").isString())

                // Idade
                .andExpect(jsonPath("$.idade").value(24))
                .andExpect(jsonPath("$.idade").isNumber())

                // Rank
                .andExpect(jsonPath("$.rank").value("S"))
                .andExpect(jsonPath("$.rank").isString())

                // Missão não vinculada
                .andExpect(jsonPath("$.missaoId").value(
                        org.hamcrest.Matchers.nullValue()
                ));
    }

    @Test
    void naoDeveConsultarNinjaComIdEmFormatoInvalido() throws Exception {

        // ARRANGE
        String idInvalido = "abc";

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        mockMvc.perform(get("/ninjas/{id}", idInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));

        // ASSERT - Confirmar que nenhum registro foi modificado
        assertEquals(
                quantidadeAntes,
                ninjaRepository.count()
        );
    }


    // METODO AUXILIAR
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


