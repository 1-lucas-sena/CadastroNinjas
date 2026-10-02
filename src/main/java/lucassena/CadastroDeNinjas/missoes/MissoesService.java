package lucassena.CadastroDeNinjas.missoes;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MissoesService {

    private final MissoesRepository missoesRepository;
    private final MissoesMapper missoesMapper;

    public MissoesService(MissoesRepository missoesRepository,
                          MissoesMapper missoesMapper) {
        this.missoesRepository = missoesRepository;
        this.missoesMapper = missoesMapper;
    }

    public List<MissoesDTO> listarMissoes() {

        List<MissoesModel> missoes = missoesRepository.findAll();

        return missoes.stream()
                .map(missoesMapper::map)
                .toList();
    }

    public MissoesDTO listarMissoesPorId(Long id) {

        Optional<MissoesModel> missao = missoesRepository.findById(id);

        return missao
                .map(missoesMapper::map)
                .orElse(null);
    }

    public MissoesDTO criarMissao(MissoesDTO missaoDTO) {

        MissoesModel missao = missoesMapper.map(missaoDTO);

        MissoesModel missaoSalva = missoesRepository.save(missao);

        return missoesMapper.map(missaoSalva);
    }

    public void deletarMissao(Long id) {

        if (missoesRepository.existsById(id)) {
            missoesRepository.deleteById(id);
        }
    }

    public MissoesDTO atualizarMissao(
            Long id,
            MissoesDTO missaoDTO) {

        Optional<MissoesModel> missaoPorId = missoesRepository.findById(id);

        if (missaoPorId.isPresent()) {

            MissoesModel missaoAtualizada = missoesMapper.map(missaoDTO);

            missaoAtualizada.setId(id);

            MissoesModel missaoSalva = missoesRepository.save(missaoAtualizada);

            return missoesMapper.map(missaoSalva);
        }

        return null;
    }
}
