
package lucassena.CadastroDeNinjas.ninjaMissao;

import lucassena.CadastroDeNinjas.missoes.MissoesResponseDTO;
import lucassena.CadastroDeNinjas.ninjas.NinjaResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class NinjaMissaoController {

    private final NinjaMissaoService ninjaMissaoService;

    public NinjaMissaoController(
            NinjaMissaoService ninjaMissaoService) {

        this.ninjaMissaoService = ninjaMissaoService;
    }

    // =========================
    // ATRIBUIÇÃO
    // =========================

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

    @PatchMapping("/missoes/{id}/concluir")
    public ResponseEntity<MissoesResponseDTO> concluirMissao(
            @PathVariable Long id) {

        MissoesResponseDTO missaoConcluida =
                ninjaMissaoService.concluirMissao(id);

        return ResponseEntity.ok(missaoConcluida);
    }

}
