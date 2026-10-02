package lucassena.CadastroDeNinjas.missoes;

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

    @GetMapping("/listar")
    public ResponseEntity<List<MissoesDTO>> listarMissoes() {
        List<MissoesDTO> missoes = missoesService.listarMissoes();
        return ResponseEntity.status(HttpStatus.OK).body(missoes);
    }

    @GetMapping("/listar/{id}")
    public ResponseEntity<MissoesDTO> listarMissaoPorId(@PathVariable Long id) {
        MissoesDTO missao = missoesService.listarMissoesPorId(id);
        if (missao != null) {
            return ResponseEntity.status(HttpStatus.OK).body(missao);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/criar")
    public ResponseEntity<MissoesDTO> criarMissao(@RequestBody MissoesDTO missaoDTO) {
        MissoesDTO novaMissao = missoesService.criarMissao(missaoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaMissao);
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        MissoesDTO missao = missoesService.listarMissoesPorId(id);
        if (missao != null) {
            missoesService.deletarMissao(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/alterar/{id}")
    public ResponseEntity<MissoesDTO> alterar(@PathVariable Long id, @RequestBody MissoesDTO missaoAtualizada) {
        MissoesDTO missaoModificada = missoesService.atualizarMissao(id, missaoAtualizada);
        if (missaoModificada != null) {
            return ResponseEntity.status(HttpStatus.OK).body(missaoModificada);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}