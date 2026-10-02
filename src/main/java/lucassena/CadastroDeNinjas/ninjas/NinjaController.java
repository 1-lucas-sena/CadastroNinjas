package lucassena.CadastroDeNinjas.ninjas;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ninjas")
public class NinjaController {
    private final NinjaService ninjaService;

    public NinjaController(NinjaService ninjaService) {

        this.ninjaService = ninjaService;
    }

    @GetMapping("/listar")
    public ResponseEntity<List<NinjaDTO>> listarNinjas() {
        List<NinjaDTO> ninjas = ninjaService.listarNinjas();
        return ResponseEntity.status(HttpStatus.OK).body(ninjas);
    }

    @GetMapping("/listar/{id}")
    public ResponseEntity<NinjaDTO> listarNinjasPorId(@PathVariable Long id) {
        NinjaDTO ninja = ninjaService.listarNinjasPorId(id);
        if (ninja != null) {
            return ResponseEntity.status(HttpStatus.OK).body(ninja);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/criar")
    public ResponseEntity<NinjaDTO> criarNinja(@RequestBody NinjaDTO ninjaDTO) {
        NinjaDTO novoNinja = ninjaService.criarNinja(ninjaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoNinja);
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {

        NinjaDTO ninja = ninjaService.listarNinjasPorId(id);
        if (ninja != null) {
            ninjaService.deletarNinja(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    @PutMapping("/alterar/{id}")
    public ResponseEntity<NinjaDTO> alterar(@PathVariable Long id, @RequestBody NinjaDTO ninjaAtualizado) {
        NinjaDTO ninjaModificado = ninjaService.atualizarNinja(id, ninjaAtualizado);
        if (ninjaModificado != null) {
            return ResponseEntity.status(HttpStatus.OK).body(ninjaModificado);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


}
