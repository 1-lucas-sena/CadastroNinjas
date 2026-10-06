package lucassena.CadastroDeNinjas.ninjas;

import lucassena.CadastroDeNinjas.exceptions.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NinjaService {

    private final NinjaRepository ninjaRepository;
    private final NinjaMapper ninjaMapper;

    public NinjaService(
            NinjaRepository ninjaRepository,
            NinjaMapper ninjaMapper) {

        this.ninjaRepository = ninjaRepository;
        this.ninjaMapper = ninjaMapper;
    }


    // =========================
    // CONSULTAS
    // =========================

    public List<NinjaDTO> listarNinjas() {

        List<NinjaModel> ninjas = ninjaRepository.findAll();

        return ninjas.stream()
                .map(ninjaMapper::map)
                .toList();
    }

    public NinjaDTO listarNinjasPorId(Long ninjaID) {

        Optional<NinjaModel> ninjaPorId =
                ninjaRepository.findById(ninjaID);

        return ninjaPorId
                .map(ninjaMapper::map)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Ninja não encontrado"));
    }

    public List<NinjaModel> buscarPorMissao(Long missaoId) {

        return ninjaRepository.findByMissoes_Id(missaoId);
    }


    // =========================
    // CRUD
    // =========================

    public NinjaDTO criarNinja(NinjaDTO ninjaDto) {

        NinjaModel ninja = ninjaMapper.map(ninjaDto);

        ninja = ninjaRepository.save(ninja);

        return ninjaMapper.map(ninja);
    }

    public NinjaDTO atualizarNinja(Long id, NinjaDTO ninjaDTO) {

        NinjaModel ninja = buscarPorId(id);

        ninja.setNome(ninjaDTO.getNome());
        ninja.setEmail(ninjaDTO.getEmail());
        ninja.setIdade(ninjaDTO.getIdade());
        ninja.setRank(ninjaDTO.getRank());

        NinjaModel ninjaSalvo =
                ninjaRepository.save(ninja);

        return ninjaMapper.map(ninjaSalvo);
    }

    public void deletarNinja(Long id) {

        if (ninjaRepository.existsById(id)) {
            ninjaRepository.deleteById(id);
            return;
        }

        throw new RecursoNaoEncontradoException(
                "Ninja não encontrado");
    }


    // =========================
    // RELACIONAMENTO
    // =========================

    public NinjaDTO removerMissao(Long ninjaId) {

        NinjaModel ninja = ninjaRepository.findById(ninjaId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Ninja não encontrado"));

        ninja.setMissoes(null);

        NinjaModel ninjaSalvo =
                ninjaRepository.save(ninja);

        return ninjaMapper.map(ninjaSalvo);
    }

    public void removerMissaoDosNinjas(Long missaoId) {

        List<NinjaModel> ninjas =
                ninjaRepository.findByMissoes_Id(missaoId);

        for (NinjaModel ninja : ninjas) {
            ninja.setMissoes(null);
        }

        ninjaRepository.saveAll(ninjas);
    }


    // =========================
    // MÉTODOS INTERNOS
    // =========================

    public NinjaModel buscarPorId(Long id) {

        return ninjaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Ninja não encontrado"));
    }

    public NinjaModel salvar(NinjaModel ninja) {

        return ninjaRepository.save(ninja);
    }
}