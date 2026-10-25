
package lucassena.CadastroDeNinjas.missoes;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/missoes")
public class MissoesController {

    private final MissoesService missoesService;

    public MissoesController(MissoesService missoesService) {
        this.missoesService = missoesService;
    }

    // =========================
    // CONSULTAS
    // =========================

    @GetMapping
    public ResponseEntity<List<MissoesResponseDTO>> listarMissoes() {

        List<MissoesResponseDTO> missoes =
                missoesService.listarMissoes();

        return ResponseEntity.ok(missoes);
    }

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

    @PostMapping
    public ResponseEntity<MissoesResponseDTO> criarMissao(
            @Valid @RequestBody MissoesRequestDTO missaoDTO) {

        MissoesResponseDTO novaMissao =
                missoesService.criarMissao(missaoDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(novaMissao);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MissoesResponseDTO> atualizarMissao(
            @PathVariable Long id,
            @Valid @RequestBody MissoesRequestDTO missaoDTO) {

        MissoesResponseDTO missaoAtualizada =
                missoesService.atualizarMissao(id, missaoDTO);

        return ResponseEntity.ok(missaoAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarMissao(
            @PathVariable Long id) {

        missoesService.deletarMissao(id);

        return ResponseEntity.noContent().build();
    }

    // =========================
    // ATIVAÇÃO
    // =========================

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<MissoesResponseDTO> ativarMissao(
            @PathVariable Long id) {

        MissoesResponseDTO missaoAtivada =
                missoesService.ativarMissao(id);

        return ResponseEntity.ok(missaoAtivada);
    }
}
