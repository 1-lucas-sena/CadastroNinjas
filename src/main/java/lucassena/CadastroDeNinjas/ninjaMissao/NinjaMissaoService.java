package lucassena.CadastroDeNinjas.ninjaMissao;

import org.springframework.transaction.annotation.Transactional;
import lucassena.CadastroDeNinjas.missoes.MissoesDTO;
import lucassena.CadastroDeNinjas.missoes.MissoesMapper;
import lucassena.CadastroDeNinjas.missoes.MissoesModel;
import lucassena.CadastroDeNinjas.missoes.MissoesService;
import lucassena.CadastroDeNinjas.missoes.StatusMissao;
import lucassena.CadastroDeNinjas.ninjas.NinjaDTO;
import lucassena.CadastroDeNinjas.ninjas.NinjaMapper;
import lucassena.CadastroDeNinjas.ninjas.NinjaModel;
import lucassena.CadastroDeNinjas.ninjas.NinjaService;
import org.springframework.stereotype.Service;

@Service
public class NinjaMissaoService {

    private final NinjaService ninjaService;
    private final MissoesService missoesService;
    private final NinjaMapper ninjaMapper;
    private final MissoesMapper missoesMapper;

    public NinjaMissaoService(
            NinjaService ninjaService,
            MissoesService missoesService,
            NinjaMapper ninjaMapper,
            MissoesMapper missoesMapper) {

        this.ninjaService = ninjaService;
        this.missoesService = missoesService;
        this.ninjaMapper = ninjaMapper;
        this.missoesMapper = missoesMapper;
    }

    @Transactional
    public NinjaDTO atribuirMissao(Long ninjaId, Long missaoId) {

        NinjaModel ninja = ninjaService.buscarPorId(ninjaId);

        MissoesModel missao = missoesService.buscarPorId(missaoId);

        ninja.setMissoes(missao);

        NinjaModel ninjaSalvo = ninjaService.salvar(ninja);

        return ninjaMapper.map(ninjaSalvo);
    }

    @Transactional
    public MissoesDTO inativarMissao(Long id) {

        MissoesModel missao = missoesService.buscarPorId(id);

        missao.setStatus(StatusMissao.INATIVA);

        ninjaService.removerMissaoDosNinjas(missao.getId());

        MissoesModel missaoInativada = missoesService.salvar(missao);

        return missoesMapper.map(missaoInativada);
    }
}