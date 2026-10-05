package lucassena.CadastroDeNinjas.missoes;

import lucassena.CadastroDeNinjas.exceptions.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MissoesService {

    private final MissoesRepository missoesRepository;
    private final MissoesMapper missoesMapper;

    public MissoesService(
            MissoesRepository missoesRepository,
            MissoesMapper missoesMapper) {

        this.missoesRepository = missoesRepository;
        this.missoesMapper = missoesMapper;
    }

    // =========================
    // CONSULTAS
    // =========================

    public List<MissoesDTO> listarMissoes() {

        List<MissoesModel> missoes = missoesRepository.findAll();

        return missoes.stream()
                .map(missoesMapper::map)
                .toList();
    }

    public MissoesDTO listarMissoesPorId(Long id) {

        MissoesModel missao = buscarPorId(id);

        return missoesMapper.map(missao);
    }

    // =========================
    // CRUD
    // =========================

    public MissoesDTO criarMissao(MissoesDTO missaoDTO) {

        MissoesModel missao = missoesMapper.map(missaoDTO);

        missao.setStatus(StatusMissao.EM_ESPERA);

        MissoesModel missaoSalva = missoesRepository.save(missao);

        return missoesMapper.map(missaoSalva);
    }

    public MissoesDTO atualizarMissao(Long id, MissoesDTO missaoDTO) {

        buscarPorId(id);

        MissoesModel missaoAtualizada = missoesMapper.map(missaoDTO);
        missaoAtualizada.setId(id);

        MissoesModel missaoSalva =
                missoesRepository.save(missaoAtualizada);

        return missoesMapper.map(missaoSalva);
    }

    public void deletarMissao(Long id) {

        if (!missoesRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException(
                    "Missão não encontrada");
        }

        missoesRepository.deleteById(id);
    }

    // =========================
    // MÉTODOS INTERNOS
    // =========================

    public MissoesModel buscarPorId(Long id) {

        return missoesRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Missão não encontrada"));
    }

    public MissoesModel salvar(MissoesModel missao) {

        return missoesRepository.save(missao);
    }
}