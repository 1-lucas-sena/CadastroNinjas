package lucassena.CadastroDeNinjas.ninjas;

import jakarta.validation.Valid;
import lucassena.CadastroDeNinjas.ninjaMissao.NinjaMissaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ninjas")
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

    @GetMapping
    public ResponseEntity<List<NinjaResponseDTO>> listarNinjas() {

        List<NinjaResponseDTO> ninjas =
                ninjaService.listarNinjas();

        return ResponseEntity.ok(ninjas);
    }

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

    @PostMapping
    public ResponseEntity<NinjaResponseDTO> criarNinja(
            @Valid @RequestBody NinjaRequestDTO ninjaDTO) {

        NinjaResponseDTO novoNinja =
                ninjaService.criarNinja(ninjaDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(novoNinja);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NinjaResponseDTO> atualizarNinja(
            @PathVariable Long id,
            @Valid @RequestBody NinjaRequestDTO ninjaDTO) {

        NinjaResponseDTO ninjaAtualizado =
                ninjaService.atualizarNinja(id, ninjaDTO);

        return ResponseEntity.ok(ninjaAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarNinja(
            @PathVariable Long id) {

        ninjaMissaoService.deletarNinja(id);

        return ResponseEntity.noContent().build();
    }
}