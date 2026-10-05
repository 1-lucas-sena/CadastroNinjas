package lucassena.CadastroDeNinjas.missoes;

import lucassena.CadastroDeNinjas.exceptions.RecursoNaoEncontradoException;
import lucassena.CadastroDeNinjas.ninjas.NinjaModel;
import lucassena.CadastroDeNinjas.ninjas.NinjaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MissoesService {

    private final MissoesRepository missoesRepository;
    private final MissoesMapper missoesMapper;
    private final NinjaRepository ninjaRepository;

    public MissoesService(MissoesRepository missoesRepository,
                          MissoesMapper missoesMapper,
                          NinjaRepository ninjaRepository) {
        this.missoesRepository = missoesRepository;
        this.missoesMapper = missoesMapper;
        this.ninjaRepository = ninjaRepository;
    }

    public List<MissoesDTO> listarMissoes() {

        List<MissoesModel> missoes = missoesRepository.findAll();

        return missoes.stream()
                .map(missoesMapper::map)
                .toList();
    }

    public MissoesDTO listarMissoesPorId(Long id) {
        Optional<MissoesModel> missaoPorId = missoesRepository.findById(id);

       return missaoPorId
               .map(missoesMapper::map)
               .orElseThrow(()->
                new RecursoNaoEncontradoException(
                        "Missão não encontrada"));
    }

    public MissoesDTO criarMissao(MissoesDTO missaoDTO) {
        MissoesModel missao = missoesMapper.map(missaoDTO);
        missao.setStatus(StatusMissao.EM_ESPERA);
        MissoesModel missaoSalva = missoesRepository.save(missao);

        return missoesMapper.map(missaoSalva);
    }

    public void deletarMissao(Long id) {
        if (missoesRepository.existsById(id)) {
            missoesRepository.deleteById(id);
            return;
        }
        throw new RecursoNaoEncontradoException("Missão não encontrada");
    }

    public MissoesDTO atualizarMissao(Long id, MissoesDTO missaoDTO) {
        Optional<MissoesModel> missaoPorId = missoesRepository.findById(id);

        if (missaoPorId.isPresent()) {
            MissoesModel missaoAtualizada = missoesMapper.map(missaoDTO);
            missaoAtualizada.setId(id);

            MissoesModel missaoSalva = missoesRepository.save(missaoAtualizada);

            return missoesMapper.map(missaoSalva);
        }
        throw new RecursoNaoEncontradoException("Missão não encontrada");
    }

    public MissoesDTO inativarMissao(Long id) {

        MissoesModel missao = missoesRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Missão não encontrada"));

        List<NinjaModel> ninjas = ninjaRepository.findByMissoes_Id(id);

        for (NinjaModel ninja : ninjas) {
            ninja.setMissoes(null);
            ninjaRepository.save(ninja);
        }

        missao.setStatus(StatusMissao.INATIVA);

        MissoesModel missaoSalva = missoesRepository.save(missao);

        return missoesMapper.map(missaoSalva);
    }

}
