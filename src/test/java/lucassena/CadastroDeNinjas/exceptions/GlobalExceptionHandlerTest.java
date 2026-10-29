
package lucassena.CadastroDeNinjas.exceptions;

import lucassena.CadastroDeNinjas.ninjas.NinjaModel;
import lucassena.CadastroDeNinjas.ninjas.NinjaRepository;
import lucassena.CadastroDeNinjas.util.Rank;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NinjaRepository ninjaRepository;

    // MÉTODO AUXILIAR
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


    @Test
    void deveRetornar404QuandoRecursoNaoExiste() throws Exception {

        // ARRANGE
        Long ninjaIdInexistente = 999999999L;

        assertFalse(
                ninjaRepository.existsById(ninjaIdInexistente)
        );

        // ACT + ASSERT
        mockMvc.perform(
                        get("/ninjas/{id}", ninjaIdInexistente)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Ninja não encontrado"));
    }

    @Test
    void deveRetornar409QuandoRegraDeNegocioEViolada() throws Exception {

        // ARRANGE
        criarNinja(
                "Naruto Uzumaki",
                "naruto.handler@example.com",
                25,
                Rank.A
        );

        String json = """
            {
                "nome": "Outro Naruto",
                "email": "NARUTO.HANDLER@example.com",
                "idade": 20,
                "rank": "B"
            }
            """;

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        post("/ninjas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Email já cadastrado"));

        // ASSERT - Confirmar que não houve novo cadastro
        assertEquals(
                quantidadeAntes,
                ninjaRepository.count()
        );
    }

    @Test
    void deveRetornar400ComDetalhesDosCamposInvalidos() throws Exception {

        // ARRANGE
        String json = """
            {
                "nome": "",
                "email": "email-invalido",
                "idade": -5,
                "rank": null
            }
            """;

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        post("/ninjas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Erro de validação"))

                // Campos inválidos
                .andExpect(jsonPath("$.errors.nome").exists())
                .andExpect(jsonPath("$.errors.email")
                        .value("Email deve ser válido"))
                .andExpect(jsonPath("$.errors.idade").exists())
                .andExpect(jsonPath("$.errors.rank").exists());

        // ASSERT - Nenhum ninja deve ser criado
        assertEquals(
                quantidadeAntes,
                ninjaRepository.count()
        );
    }

    @Test
    void deveRetornar409QuandoOcorreViolacaoDeIntegridade() {

        // ARRANGE
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        DataIntegrityViolationException excecao =
                new DataIntegrityViolationException(
                        "Violação de constraint no banco de dados"
                );

        // ACT
        ResponseEntity<ErrorResponse> resposta =
                handler.tratarViolacaoDeIntegridade(excecao);

        // ASSERT
        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());

        ErrorResponse erro = resposta.getBody();

        assertNotNull(erro);

        assertEquals(409, erro.getStatus());
        assertEquals("Conflict", erro.getError());

        assertEquals(
                "Operação não permitida devido a uma restrição de integridade dos dados",
                erro.getMessage()
        );

        assertNotNull(erro.getTimestamp());
    }

    @Test
    void deveRetornar400QuandoJsonEstaMalformado() throws Exception {

        // ARRANGE
        String jsonInvalido = """
            {
                "nome": "Naruto Uzumaki",
                "email": "naruto@example.com",
                "idade": 25,
                "rank": "A"
            """;

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        post("/ninjas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonInvalido)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("JSON inválido ou malformado."));

        // ASSERT
        assertEquals(
                quantidadeAntes,
                ninjaRepository.count()
        );
    }

    @Test
    void deveRetornar400ComValoresPermitidosQuandoRankEInvalido() throws Exception {

        // ARRANGE
        String json = """
            {
                "nome": "Ninja com rank inválido",
                "email": "rank.invalido@example.com",
                "idade": 25,
                "rank": "X"
            }
            """;

        long quantidadeAntes = ninjaRepository.count();

        // ACT + ASSERT
        mockMvc.perform(
                        post("/ninjas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString(
                                "Valor inválido para Rank."
                        )))
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString(
                                "Valores permitidos:"
                        )));

        // ASSERT - Nenhum ninja deve ser criado
        assertEquals(
                quantidadeAntes,
                ninjaRepository.count()
        );
    }






}
