
package lucassena.CadastroDeNinjas.missoes;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;




@RestController
@RequestMapping("/missoes")

@Tag(
        name = "Missões",
        description = "Operações de cadastro, consulta, atualização, exclusão e ativação de missões"
)

public class MissoesController {

    private final MissoesService missoesService;

    public MissoesController(MissoesService missoesService) {
        this.missoesService = missoesService;
    }

    // =========================
    // CONSULTAS
    // =========================

    @Operation(
            summary = "Listar missões",
            description = "Retorna uma lista paginada de missões, permitindo controlar a página, a quantidade de registros e a ordenação."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Missões listadas com sucesso"
            )
    })

    @GetMapping
    public ResponseEntity<Page<MissoesResponseDTO>> listarMissoes(
            @ParameterObject
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        Page<MissoesResponseDTO> missoes =
                missoesService.listarMissoes(pageable);

        return ResponseEntity.ok(missoes);
    }




    @Operation(
            summary = "Consultar missão por ID",
            description = "Retorna os dados de uma missão cadastrada a partir do seu identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Missão encontrada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Missão não encontrada",
                    content = @Content
            )
    })

    @GetMapping("/{id}")
    public ResponseEntity<MissoesResponseDTO> listarMissaoPorId(
            @PathVariable Long id) {

        MissoesResponseDTO missao =
                missoesService.listarMissoesPorId(id);

        return ResponseEntity.ok(missao);
    }

    // =========================
    // CRUD
    // =========================

    @Operation(
            summary = "Cadastrar missão",
            description = "Cadastra uma nova missão, validando nome e rank. Toda missão é criada com status DISPONIVEL."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Missão cadastrada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou erro de validação",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<MissoesResponseDTO> criarMissao(
            @Valid @RequestBody MissoesRequestDTO missaoDTO) {

        MissoesResponseDTO novaMissao =
                missoesService.criarMissao(missaoDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(novaMissao);
    }

    @Operation(
            summary = "Atualizar missão",
            description = "Atualiza o nome e o rank de uma missão existente. Alterações no rank podem provocar a reavaliação automática do status da missão, conforme a qualificação dos ninjas vinculados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Missão atualizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou erro de validação",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Missão não encontrada",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<MissoesResponseDTO> atualizarMissao(
            @PathVariable Long id,
            @Valid @RequestBody MissoesRequestDTO missaoDTO) {

        MissoesResponseDTO missaoAtualizada =
                missoesService.atualizarMissao(id, missaoDTO);

        return ResponseEntity.ok(missaoAtualizada);
    }

    @Operation(
            summary = "Excluir missão",
            description = "Exclui uma missão cadastrada, desde que não possua ninjas vinculados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Missão excluída com sucesso",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Missão não encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Não é possível excluir uma missão que possui ninjas vinculados",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarMissao(
            @PathVariable Long id) {

        missoesService.deletarMissao(id);

        return ResponseEntity.noContent().build();
    }

    // =========================
    // ATIVAÇÃO
    // =========================

    @Operation(
            summary = "Ativar missão",
            description = "Ativa uma missão com status INATIVA, alterando seu status para DISPONIVEL. Missões com outros status não podem ser ativadas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Missão ativada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Missão não encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A missão não está inativa",
                    content = @Content
            )
    })
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<MissoesResponseDTO> ativarMissao(
            @PathVariable Long id) {

        MissoesResponseDTO missaoAtivada =
                missoesService.ativarMissao(id);

        return ResponseEntity.ok(missaoAtivada);
    }
}
