
package lucassena.CadastroDeNinjas.ninjas;

import lucassena.CadastroDeNinjas.exceptions.RecursoNaoEncontradoException;
import lucassena.CadastroDeNinjas.exceptions.RegraDeNegocioException;
import lucassena.CadastroDeNinjas.missaoStatus.MissaoStatusService;
import lucassena.CadastroDeNinjas.missoes.MissoesModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class NinjaService {

    private final NinjaRepository ninjaRepository;
    private final NinjaMapper ninjaMapper;
    private final MissaoStatusService missaoStatusService;

    public NinjaService(
            NinjaRepository ninjaRepository,
            NinjaMapper ninjaMapper,
            MissaoStatusService missaoStatusService) {

        this.ninjaRepository = ninjaRepository;
        this.ninjaMapper = ninjaMapper;
        this.missaoStatusService = missaoStatusService;
    }

    // =========================
    // CONSULTAS
    // =========================

    @Transactional(readOnly = true)
    public List<NinjaResponseDTO> listarNinjas() {

        List<NinjaModel> ninjas = ninjaRepository.findAll();

        return ninjas.stream()
                .map(ninjaMapper::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public NinjaResponseDTO listarNinjasPorId(Long ninjaID) {

        NinjaModel ninja = buscarPorId(ninjaID);

        return ninjaMapper.map(ninja);
    }

    // =========================
    // CADASTRO
    // =========================

    public NinjaResponseDTO criarNinja(NinjaRequestDTO ninjaDTO) {

        String emailNormalizado = normalizarEmail(ninjaDTO.getEmail());

        if (ninjaRepository.existsByEmail(emailNormalizado)) {
            throw new RegraDeNegocioException("Email já cadastrado");
        }

        NinjaModel ninja = ninjaMapper.map(ninjaDTO);
        ninja.setEmail(emailNormalizado);

        NinjaModel ninjaSalvo = ninjaRepository.save(ninja);

        return ninjaMapper.map(ninjaSalvo);
    }

    // =========================
    // ATUALIZAÇÃO
    // =========================

    @Transactional
    public NinjaResponseDTO atualizarNinja(
            Long id,
            NinjaRequestDTO ninjaDTO) {

        NinjaModel ninja = buscarPorId(id);

        String emailNormalizado = normalizarEmail(ninjaDTO.getEmail());

        if (ninjaRepository.existsByEmailAndIdNot(emailNormalizado, id)) {
            throw new RegraDeNegocioException("Email já cadastrado");
        }

        MissoesModel missao = ninja.getMissoes();

        ninja.setNome(ninjaDTO.getNome());
        ninja.setEmail(emailNormalizado);
        ninja.setIdade(ninjaDTO.getIdade());
        ninja.setRank(ninjaDTO.getRank());

        NinjaModel ninjaSalvo = ninjaRepository.save(ninja);

        if (missao != null) {
            ninjaRepository.flush();
            missaoStatusService.reavaliarStatus(missao);
        }

        return ninjaMapper.map(ninjaSalvo);
    }

    // =========================
    // MÉTODOS INTERNOS
    // =========================

    private NinjaModel buscarPorId(Long id) {

        return ninjaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Ninja não encontrado"));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
