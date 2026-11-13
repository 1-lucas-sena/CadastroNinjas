package lucassena.CadastroDeNinjas.ninjas;

import jakarta.validation.Valid;
import lucassena.CadastroDeNinjas.ninjaMissao.NinjaMissaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;


@RestController
@RequestMapping("/ninjas")
@Tag(
        name = "Ninjas",
        description = "Operações de cadastro, consulta, atualização e exclusão de ninjas"
)
public class NinjaController {

    private final NinjaService ninjaService;
    private final NinjaMissaoService ninjaMissaoService;

    public NinjaController(
            NinjaService ninjaService,
            NinjaMissaoService ninjaMissaoService) {

        this.ninjaService = ninjaService;
        this.ninjaMissaoService = ninjaMissaoService;
    }

    // =========================
    // CONSULTAS
    // =========================


    @Operation(
            summary = "Listar ninjas",
            description = "Retorna uma lista paginada de ninjas, permitindo controlar a página, a quantidade de registros e a ordenação."
    )
    @GetMapping
    public ResponseEntity<Page<NinjaResponseDTO>> listarNinjas(
            @ParameterObject
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        Page<NinjaResponseDTO> ninjas =
                ninjaService.listarNinjas(pageable);

        return ResponseEntity.ok(ninjas);
    }


    @Operation(
            summary = "Consultar ninja por ID",
            description = "Retorna os dados de um ninja cadastrado a partir do seu identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ninja encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ninja não encontrado",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<NinjaResponseDTO> listarNinjasPorId(
            @PathVariable Long id) {

        NinjaResponseDTO ninja =
                ninjaService.listarNinjasPorId(id);

        return ResponseEntity.ok(ninja);
    }

    // =========================
    // CRUD
    // =========================


    @Operation(
            summary = "Cadastrar ninja",
            description = "Cadastra um novo ninja, validando nome, e-mail, idade e rank. O e-mail é normalizado e não pode estar associado a outro ninja."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Ninja cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou erro de validação",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail já cadastrado",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<NinjaResponseDTO> criarNinja(
            @Valid @RequestBody NinjaRequestDTO ninjaDTO) {

        NinjaResponseDTO novoNinja =
                ninjaService.criarNinja(ninjaDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(novoNinja);
    }

    @Operation(
            summary = "Atualizar ninja",
            description = "Atualiza os dados de um ninja existente, validando nome, e-mail, idade e rank. O e-mail é normalizado e não pode pertencer a outro ninja. Alterações no rank podem provocar a reavaliação do status da missão associada."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ninja atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou erro de validação",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ninja não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "E-mail já cadastrado ou conflito com regra de negócio",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<NinjaResponseDTO> atualizarNinja(
            @PathVariable Long id,
            @Valid @RequestBody NinjaRequestDTO ninjaDTO) {

        NinjaResponseDTO ninjaAtualizado =
                ninjaService.atualizarNinja(id, ninjaDTO);

        return ResponseEntity.ok(ninjaAtualizado);
    }
    @Operation(
            summary = "Excluir ninja",
            description = "Exclui um ninja cadastrado. Caso esteja vinculado a uma missão, o status da missão será reavaliado após a exclusão."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Ninja excluído com sucesso",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ninja não encontrado",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarNinja(
            @PathVariable Long id) {

        ninjaMissaoService.deletarNinja(id);

        return ResponseEntity.noContent().build();
    }
}