
package lucassena.CadastroDeNinjas.missaoStatus;

import lucassena.CadastroDeNinjas.missoes.MissoesModel;
import lucassena.CadastroDeNinjas.missoes.MissoesRepository;
import lucassena.CadastroDeNinjas.missoes.StatusMissao;
import lucassena.CadastroDeNinjas.ninjas.NinjaModel;
import lucassena.CadastroDeNinjas.ninjas.NinjaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MissaoStatusService {

    private final NinjaRepository ninjaRepository;
    private final MissoesRepository missoesRepository;

    public MissaoStatusService(
            NinjaRepository ninjaRepository,
            MissoesRepository missoesRepository) {

        this.ninjaRepository = ninjaRepository;
        this.missoesRepository = missoesRepository;
    }

    // =========================
    // QUALIFICAÇÃO
    // =========================

    public boolean ninjaQualificadoParaMissao(
            NinjaModel ninja,
            MissoesModel missao) {

        return ninja.getRank()
                .eMaiorOuIgual(missao.getRank());
    }

    // =========================
    // REAVALIAÇÃO DE STATUS
    // =========================

    @Transactional(propagation = Propagation.MANDATORY)
    public void reavaliarStatus(MissoesModel missao) {

        if (missao.getStatus() == StatusMissao.INATIVA
                || missao.getStatus() == StatusMissao.CONCLUIDA) {
            return;
        }

        List<NinjaModel> ninjas =
                ninjaRepository.findByMissoes_Id(missao.getId());

        boolean existeNinjaQualificado = ninjas.stream()
                .anyMatch(ninja ->
                        ninjaQualificadoParaMissao(ninja, missao));

        if (existeNinjaQualificado) {
            missao.setStatus(StatusMissao.EM_ANDAMENTO);
        } else {
            missao.setStatus(StatusMissao.DISPONIVEL);
        }

        missoesRepository.save(missao);
    }
}
