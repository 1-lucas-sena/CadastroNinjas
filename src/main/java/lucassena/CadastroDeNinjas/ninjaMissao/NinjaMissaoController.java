package lucassena.CadastroDeNinjas.ninjaMissao;

import lucassena.CadastroDeNinjas.missoes.MissoesDTO;
import lucassena.CadastroDeNinjas.ninjas.NinjaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ninja-missao")
public class NinjaMissaoController {

    private final NinjaMissaoService ninjaMissaoService;

    public NinjaMissaoController(NinjaMissaoService ninjaMissaoService) {
        this.ninjaMissaoService = ninjaMissaoService;
    }

    @PatchMapping("/ninjas/{ninjaId}/missoes/{missaoId}")
    public ResponseEntity<NinjaDTO> atribuirMissao(
            @PathVariable Long ninjaId,
            @PathVariable Long missaoId) {

        NinjaDTO ninjaAtualizado =
                ninjaMissaoService.atribuirMissao(ninjaId, missaoId);

        return ResponseEntity.status(HttpStatus.OK).body(ninjaAtualizado);
    }

    @PatchMapping("/ninjas/{ninjaId}/missao")
    public ResponseEntity<NinjaDTO> removerMissao(
            @PathVariable Long ninjaId) {

        NinjaDTO ninjaAtualizado =
                ninjaMissaoService.removerMissao(ninjaId);

        return ResponseEntity.status(HttpStatus.OK).body(ninjaAtualizado);
    }

    @PatchMapping("/missoes/{id}/inativar")
    public ResponseEntity<MissoesDTO> inativarMissao(
            @PathVariable Long id) {

        MissoesDTO missaoInativada =
                ninjaMissaoService.inativarMissao(id);

        return ResponseEntity.status(HttpStatus.OK).body(missaoInativada);
    }

    @PatchMapping("/missoes/{id}/concluir")
    public ResponseEntity<MissoesDTO> concluirMissao(
            @PathVariable Long id) {

        MissoesDTO missaoConcluida =
                ninjaMissaoService.concluirMissao(id);

        return ResponseEntity.status(HttpStatus.OK).body(missaoConcluida);
    }

    @PatchMapping("/missoes/{id}/ativar")
    public ResponseEntity<MissoesDTO> ativarMissao(
            @PathVariable Long id) {

        MissoesDTO missaoAtivada =
                ninjaMissaoService.ativarMissao(id);

        return ResponseEntity.status(HttpStatus.OK).body(missaoAtivada);
    }

    @PatchMapping("/ninjas/{ninjaId}/transferir/{novaMissaoId}")
    public ResponseEntity<NinjaDTO> transferirMissao(
            @PathVariable Long ninjaId,
            @PathVariable Long novaMissaoId) {

        NinjaDTO ninjaAtualizado =
                ninjaMissaoService.transferirMissao(
                        ninjaId,
                        novaMissaoId);

        return ResponseEntity.status(HttpStatus.OK).body(ninjaAtualizado);
    }

}