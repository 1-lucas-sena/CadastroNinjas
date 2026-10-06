package lucassena.CadastroDeNinjas.ninjaMissao;

import lucassena.CadastroDeNinjas.exceptions.RegraDeNegocioException;
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

        if (missao.getStatus() == StatusMissao.INATIVA) {
            throw new RegraDeNegocioException(
                    "Não é possível atribuir Ninja a uma missão inativa");
        }

        if (missao.getStatus() == StatusMissao.CONCLUIDA) {
            throw new RegraDeNegocioException(
                    "Não é possível atribuir Ninja a uma missão concluída");
        }

        ninja.setMissoes(missao);

        if (missao.getStatus() == StatusMissao.EM_ESPERA
                && ninja.getRank().eMaiorOuIgual(missao.getRank())) {

            missao.setStatus(StatusMissao.ATIVA);
            missoesService.salvar(missao);
        }

        NinjaModel ninjaSalvo = ninjaService.salvar(ninja);

        return ninjaMapper.map(ninjaSalvo);
    }

    @Transactional
    public MissoesDTO inativarMissao(Long id) {

        MissoesModel missao = missoesService.buscarPorId(id);

        if (missao.getStatus() == StatusMissao.CONCLUIDA) {
            throw new RegraDeNegocioException(
                    "Não é possível inativar uma missão concluída");
        }

        missao.setStatus(StatusMissao.INATIVA);

        ninjaService.removerMissaoDosNinjas(missao.getId());

        MissoesModel missaoInativada = missoesService.salvar(missao);

        return missoesMapper.map(missaoInativada);
    }

    @Transactional
    public MissoesDTO concluirMissao(Long id) {

        MissoesModel missao = missoesService.buscarPorId(id);

        if (missao.getStatus() != StatusMissao.ATIVA) {
            throw new RegraDeNegocioException(
                    "Só é possível concluir uma missão ativa");
        }

        ninjaService.removerMissaoDosNinjas(missao.getId());

        missao.setStatus(StatusMissao.CONCLUIDA);

        MissoesModel missaoConcluida = missoesService.salvar(missao);

        return missoesMapper.map(missaoConcluida);
    }
    @Transactional
    public MissoesDTO ativarMissao(Long id) {

        MissoesModel missao = missoesService.buscarPorId(id);

        if (missao.getStatus() != StatusMissao.INATIVA) {
            throw new RegraDeNegocioException(
                    "Só é possível ativar uma missão inativa");
        }

        missao.setStatus(StatusMissao.EM_ESPERA);

        MissoesModel missaoAtivada = missoesService.salvar(missao);

        return missoesMapper.map(missaoAtivada);
    }
}