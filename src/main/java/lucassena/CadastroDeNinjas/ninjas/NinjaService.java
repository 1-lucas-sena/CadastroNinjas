package lucassena.CadastroDeNinjas.ninjas;

import lucassena.CadastroDeNinjas.exceptions.RecursoNaoEncontradoException;
import lucassena.CadastroDeNinjas.missoes.MissoesModel;
import lucassena.CadastroDeNinjas.missoes.MissoesRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class NinjaService {


    private final NinjaRepository ninjaRepository;
    private final NinjaMapper ninjaMapper;
    private final MissoesRepository missoesRepository;

    public NinjaService(
            NinjaRepository ninjaRepository,
            NinjaMapper ninjaMapper,
            MissoesRepository missoesRepository) {

        this.ninjaRepository = ninjaRepository;
        this.ninjaMapper = ninjaMapper;
        this.missoesRepository = missoesRepository;
    }

    public List<NinjaDTO> listarNinjas() {
        List<NinjaModel> ninjas = ninjaRepository.findAll();
        return ninjas.stream()
                .map(ninjaMapper::map)
                .toList();
    }

    public NinjaDTO listarNinjasPorId(Long ninjaID) {
        Optional<NinjaModel> ninjaPorId = ninjaRepository.findById(ninjaID);

        return ninjaPorId
                .map(ninjaMapper::map)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Ninja não encontrado"));
    }

    public NinjaDTO criarNinja(NinjaDTO ninjaDto) {
        NinjaModel ninja = ninjaMapper.map(ninjaDto);
        ninja = ninjaRepository.save(ninja);
        return ninjaMapper.map(ninja);
    }

    public void deletarNinja(Long id) {
        if (ninjaRepository.existsById(id)) {
            ninjaRepository.deleteById(id);
            return;
        }
        throw new RecursoNaoEncontradoException("Ninja não encontrado");
    }

    public NinjaDTO atualizarNinja(Long id, NinjaDTO ninjaDTO) {
        Optional<NinjaModel> ninjaPorId = ninjaRepository.findById(id);

        if (ninjaPorId.isPresent()) {
            NinjaModel ninjaAtualizado = ninjaMapper.map(ninjaDTO);
            ninjaAtualizado.setId(id);

            NinjaModel ninjaSalvo = ninjaRepository.save(ninjaAtualizado);

            return ninjaMapper.map(ninjaSalvo);
        }

        throw new RecursoNaoEncontradoException("Ninja não encontrado");
    }

    public NinjaDTO atribuirMissao(Long ninjaId, Long missaoId) {

        NinjaModel ninja = ninjaRepository.findById(ninjaId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Ninja não encontrado"));

        MissoesModel missao = missoesRepository.findById(missaoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Missão não encontrada"));

        ninja.setMissoes(missao);

        NinjaModel ninjaSalvo = ninjaRepository.save(ninja);

        return ninjaMapper.map(ninjaSalvo);
    }

    public NinjaDTO removerMissao(Long ninjaId) {

        NinjaModel ninja = ninjaRepository.findById(ninjaId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Ninja não encontrado"));

        ninja.setMissoes(null);

        NinjaModel ninjaSalvo = ninjaRepository.save(ninja);

        return ninjaMapper.map(ninjaSalvo);
    }
}
