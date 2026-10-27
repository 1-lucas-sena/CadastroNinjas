
package lucassena.CadastroDeNinjas.missoes;

import lucassena.CadastroDeNinjas.exceptions.RecursoNaoEncontradoException;
import lucassena.CadastroDeNinjas.exceptions.RegraDeNegocioException;
import lucassena.CadastroDeNinjas.missaoStatus.MissaoStatusService;
import lucassena.CadastroDeNinjas.ninjas.NinjaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MissoesService {

    private final MissoesRepository missoesRepository;
    private final MissoesMapper missoesMapper;
    private final NinjaRepository ninjaRepository;
    private final MissaoStatusService missaoStatusService;

    public MissoesService(
            MissoesRepository missoesRepository,
            MissoesMapper missoesMapper,
            NinjaRepository ninjaRepository,
            MissaoStatusService missaoStatusService) {

        this.missoesRepository = missoesRepository;
        this.missoesMapper = missoesMapper;
        this.ninjaRepository = ninjaRepository;
        this.missaoStatusService = missaoStatusService;
    }

    // =========================
    // CONSULTAS
    // =========================

    @Transactional(readOnly = true)
    public List<MissoesResponseDTO> listarMissoes() {

        List<MissoesModel> missoes = missoesRepository.findAll();

        return missoes.stream()
                .map(missoesMapper::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public MissoesResponseDTO listarMissoesPorId(Long id) {

        MissoesModel missao = buscarPorId(id);

        return missoesMapper.map(missao);
    }

    // =========================
    // CRUD
    // =========================

    public MissoesResponseDTO criarMissao(
            MissoesRequestDTO missaoDTO) {

        MissoesModel missao = missoesMapper.map(missaoDTO);

        missao.setStatus(StatusMissao.DISPONIVEL);

        MissoesModel missaoSalva =
                missoesRepository.save(missao);

        return missoesMapper.map(missaoSalva);
    }

    @Transactional
    public MissoesResponseDTO atualizarMissao(
            Long id,
            MissoesRequestDTO missaoDTO) {

        MissoesModel missao = buscarPorId(id);

        missao.setNome(missaoDTO.getNome());
        missao.setRank(missaoDTO.getRank());

        MissoesModel missaoSalva =
                missoesRepository.save(missao);

        missaoStatusService.reavaliarStatus(missaoSalva);

        return missoesMapper.map(missaoSalva);
    }

    @Transactional
    public void deletarMissao(Long id) {

        MissoesModel missao = buscarPorId(id);

        if (ninjaRepository.existsByMissoes_Id(id)) {
            throw new RegraDeNegocioException(
                    "Não é possível excluir uma missão que possui ninjas vinculados");
        }

        missoesRepository.delete(missao);
    }

    // =========================
    // ATIVAÇÃO
    // =========================

    @Transactional
    public MissoesResponseDTO ativarMissao(Long id) {

        MissoesModel missao = buscarPorId(id);

        if (missao.getStatus() != StatusMissao.INATIVA) {
            throw new RegraDeNegocioException(
                    "Só é possível ativar uma missão inativa");
        }

        missao.setStatus(StatusMissao.DISPONIVEL);

        MissoesModel missaoAtivada =
                missoesRepository.save(missao);

        return missoesMapper.map(missaoAtivada);
    }

    // =========================
    // MÉTODOS INTERNOS
    // =========================

    private MissoesModel buscarPorId(Long id) {

        return missoesRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Missão não encontrada"));
    }
}
