
package lucassena.CadastroDeNinjas.ninjaMissao;

import lucassena.CadastroDeNinjas.missoes.MissoesResponseDTO;
import lucassena.CadastroDeNinjas.ninjas.NinjaResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@Tag(
        name = "Associação de Ninjas e Missões",
        description = "Operações de atribuição, remoção e transferência de ninjas, além de inativação e conclusão de missões"
)
public class NinjaMissaoController {

    private final NinjaMissaoService ninjaMissaoService;

    public NinjaMissaoController(
            NinjaMissaoService ninjaMissaoService) {

        this.ninjaMissaoService = ninjaMissaoService;
    }

    // =========================
    // ATRIBUIÇÃO
    // =========================

    @Operation(
            summary = "Atribuir missão a um ninja",
            description = "Associa um ninja a uma missão disponível ou em andamento. O ninja não pode possuir outra missão. Após a atribuição, o status da missão é reavaliado conforme a qualificação dos ninjas vinculados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Missão atribuída ao ninja com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ninja ou missão não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ninja já possui missão ou a missão não aceita novas atribuições",
                    content = @Content
            )
    })
    @PatchMapping("/ninjas/{ninjaId}/missoes/{missaoId}")
    public ResponseEntity<NinjaResponseDTO> atribuirMissao(
            @PathVariable Long ninjaId,
            @PathVariable Long missaoId) {

        NinjaResponseDTO ninjaAtualizado =
                ninjaMissaoService.atribuirMissao(ninjaId, missaoId);

        return ResponseEntity.ok(ninjaAtualizado);
    }

    // =========================
    // REMOÇÃO
    // =========================

    @Operation(
            summary = "Remover missão de um ninja",
            description = "Remove o vínculo entre um ninja e sua missão atual. Após a remoção, o status da missão é reavaliado automaticamente."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Vínculo removido com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ninja não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ninja não possui missão vinculada",
                    content = @Content
            )
    })
    @DeleteMapping("/ninjas/{ninjaId}/missao")
    public ResponseEntity<NinjaResponseDTO> removerMissao(
            @PathVariable Long ninjaId) {

        NinjaResponseDTO ninjaAtualizado =
                ninjaMissaoService.removerMissao(ninjaId);

        return ResponseEntity.ok(ninjaAtualizado);
    }

    // =========================
    // TRANSFERÊNCIA
    // =========================

    @Operation(
            summary = "Transferir ninja para outra missão",
            description = "Transfere um ninja para uma missão diferente da atual, desde que a missão de destino aceite novos ninjas. Não é permitido retirar o último ninja qualificado de uma missão em andamento quando isso deixaria a missão sem outro ninja qualificado. Os status das missões envolvidas são reavaliados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ninja transferido com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ninja ou missão de destino não encontrado",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Transferência não permitida pelas regras de negócio",
                    content = @Content
            )
    })
    @PatchMapping("/ninjas/{ninjaId}/transferencias/{novaMissaoId}")
    public ResponseEntity<NinjaResponseDTO> transferirMissao(
            @PathVariable Long ninjaId,
            @PathVariable Long novaMissaoId) {

        NinjaResponseDTO ninjaAtualizado =
                ninjaMissaoService.transferirMissao(
                        ninjaId, novaMissaoId);

        return ResponseEntity.ok(ninjaAtualizado);
    }

    // =========================
    // INATIVAÇÃO
    // =========================

    @Operation(
            summary = "Inativar missão",
            description = "Altera o status de uma missão para INATIVA e remove todos os vínculos dos ninjas associados. Missões concluídas não podem ser inativadas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Missão inativada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Missão não encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Não é possível inativar uma missão concluída",
                    content = @Content
            )
    })
    @PatchMapping("/missoes/{id}/inativar")
    public ResponseEntity<MissoesResponseDTO> inativarMissao(
            @PathVariable Long id) {

        MissoesResponseDTO missaoInativada =
                ninjaMissaoService.inativarMissao(id);

        return ResponseEntity.ok(missaoInativada);
    }

    // =========================
    // CONCLUSÃO
    // =========================

    @Operation(
            summary = "Concluir missão",
            description = "Conclui uma missão com status EM_ANDAMENTO, alterando seu status para CONCLUIDA e removendo os vínculos dos ninjas associados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Missão concluída com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Missão não encontrada",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A missão não está em andamento",
                    content = @Content
            )
    })
    @PatchMapping("/missoes/{id}/concluir")
    public ResponseEntity<MissoesResponseDTO> concluirMissao(
            @PathVariable Long id) {

        MissoesResponseDTO missaoConcluida =
                ninjaMissaoService.concluirMissao(id);

        return ResponseEntity.ok(missaoConcluida);
    }

}
