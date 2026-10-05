package lucassena.CadastroDeNinjas.ninjas;

import lucassena.CadastroDeNinjas.exceptions.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public NinjaDTO listarNinjasPorId(Long ninjaId) {

        NinjaModel ninja = buscarPorId(ninjaId);

        return ninjaMapper.map(ninja);
    }

    // =========================
    // CRUD
    // =========================

    public NinjaDTO criarNinja(NinjaDTO ninjaDTO) {

        NinjaModel ninja = ninjaMapper.map(ninjaDTO);

        NinjaModel ninjaSalvo = ninjaRepository.save(ninja);

        return ninjaMapper.map(ninjaSalvo);
    }

    public NinjaDTO atualizarNinja(Long id, NinjaDTO ninjaDTO) {

        buscarPorId(id);

        NinjaModel ninjaAtualizado = ninjaMapper.map(ninjaDTO);
        ninjaAtualizado.setId(id);

        NinjaModel ninjaSalvo = ninjaRepository.save(ninjaAtualizado);

        return ninjaMapper.map(ninjaSalvo);
    }

    public void deletarNinja(Long id) {

        if (!ninjaRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException(
                    "Ninja não encontrado");
        }

        ninjaRepository.deleteById(id);
    }

    // =========================
    // RELACIONAMENTO COM MISSÃO
    // =========================

    public NinjaDTO removerMissao(Long ninjaId) {

        NinjaModel ninja = buscarPorId(ninjaId);

        ninja.setMissoes(null);

        NinjaModel ninjaSalvo = ninjaRepository.save(ninja);

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