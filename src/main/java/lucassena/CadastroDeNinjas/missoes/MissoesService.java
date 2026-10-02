package lucassena.CadastroDeNinjas.missoes;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MissoesService {

    private final MissoesRepository missoesRepository;

    public MissoesService(MissoesRepository missoesRepository) {
        this.missoesRepository = missoesRepository;
    }
    public List<MissoesModel> listarMissoes() {
        return missoesRepository.findAll();
    }

    public MissoesModel listarMissoesPorId(Long id) {

        Optional<MissoesModel> missao = missoesRepository.findById(id);

        return missao.orElse(null);
    }

    public MissoesModel criarMissao(MissoesModel missao) {
        return missoesRepository.save(missao);
    }

    public void deletarMissao(Long id) {

        if (missoesRepository.existsById(id)) {
            missoesRepository.deleteById(id);
        }
    }

    public MissoesModel atualizarMissao(Long id, MissoesModel missao) {

        Optional<MissoesModel> missaoPorId = missoesRepository.findById(id);

        if (missaoPorId.isPresent()) {

            MissoesModel missaoAtualizada = missao;
            missaoAtualizada.setId(id);

            return missoesRepository.save(missaoAtualizada);
        }

        return null;
    }
}
