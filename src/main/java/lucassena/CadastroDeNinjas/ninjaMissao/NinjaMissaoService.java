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

import java.util.List;

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

        if (ninja.getMissoes() != null) {
            throw new RegraDeNegocioException(
                    "Ninja já possui uma missão");
        }

        MissoesModel missao = missoesService.buscarPorId(missaoId);

        if (!missaoPodeReceberNinja(missao)) {

            if (missao.getStatus() == StatusMissao.INATIVA) {
                throw new RegraDeNegocioException(
                        "Não é possível atribuir Ninja a uma missão inativa");
            }

            throw new RegraDeNegocioException(
                    "Não é possível atribuir Ninja a uma missão concluída");
        }

        ninja.setMissoes(missao);

        if (missao.getStatus() == StatusMissao.ATIVA
                && ninjaQualificadoParaMissao(ninja, missao)) {

            missao.setStatus(StatusMissao.EM_CURSO);
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

        if (missao.getStatus() != StatusMissao.EM_CURSO) {
            throw new RegraDeNegocioException(
                    "Só é possível concluir uma missão em curso");
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

        missao.setStatus(StatusMissao.ATIVA);

        MissoesModel missaoAtivada = missoesService.salvar(missao);

        return missoesMapper.map(missaoAtivada);
    }

    @Transactional
    public NinjaDTO removerMissao(Long ninjaId) {

        NinjaModel ninja = ninjaService.buscarPorId(ninjaId);

        if (ninja.getMissoes() == null) {
            throw new RegraDeNegocioException(
                    "Ninja não possui uma missão");
        }

        MissoesModel missao = ninja.getMissoes();

        ninja.setMissoes(null);

        ninjaService.salvar(ninja);

        List<NinjaModel> ninjas =
                ninjaService.buscarPorMissao(missao.getId());

        boolean existeNinjaQualificado = ninjas.stream()
                .anyMatch(n ->
                        ninjaQualificadoParaMissao(n, missao)
                );

        if (missao.getStatus() == StatusMissao.EM_CURSO
                && !existeNinjaQualificado) {

            missao.setStatus(StatusMissao.ATIVA);

            missoesService.salvar(missao);
        }

        return ninjaMapper.map(ninja);
    }

    @Transactional
    public NinjaDTO transferirMissao(Long ninjaId, Long novaMissaoId) {

        NinjaModel ninja = ninjaService.buscarPorId(ninjaId);
        MissoesModel novaMissao = missoesService.buscarPorId(novaMissaoId);

        if (ninja.getMissoes() == null) {
            throw new RegraDeNegocioException(
                    "Ninja não possui uma missão");
        }

        MissoesModel missaoAtual = ninja.getMissoes();

        if (missaoAtual.getId().equals(novaMissao.getId())) {
            throw new RegraDeNegocioException(
                    "Ninja já está atribuído a esta missão");
        }

        if (!missaoPodeReceberNinja(novaMissao)) {

            if (novaMissao.getStatus() == StatusMissao.INATIVA) {
                throw new RegraDeNegocioException(
                        "Não é possível transferir Ninja para uma missão inativa");
            }

            throw new RegraDeNegocioException(
                    "Não é possível transferir Ninja para uma missão concluída");
        }

        if (missaoAtual.getStatus() == StatusMissao.EM_CURSO) {

            if (!existeOutroNinjaQualificado(ninja, missaoAtual)) {
                throw new RegraDeNegocioException(
                        "Ninja não pode ser transferido, pois é o último Ninja qualificado da missão");
            }
        }

        ninja.setMissoes(novaMissao);

        if (novaMissao.getStatus() == StatusMissao.ATIVA
                && ninjaQualificadoParaMissao(ninja, novaMissao)) {

            novaMissao.setStatus(StatusMissao.EM_CURSO);
        }

        ninjaService.salvar(ninja);
        missoesService.salvar(novaMissao);

        return ninjaMapper.map(ninja);
    }

    private boolean missaoPodeReceberNinja(MissoesModel missao) {

        return missao.getStatus() == StatusMissao.ATIVA
                || missao.getStatus() == StatusMissao.EM_CURSO;
    }

    private boolean ninjaQualificadoParaMissao(
            NinjaModel ninja,
            MissoesModel missao) {

        return ninja.getRank().eMaiorOuIgual(missao.getRank());
    }

    private boolean existeOutroNinjaQualificado(
            NinjaModel ninja,
            MissoesModel missao) {

        List<NinjaModel> ninjas =
                ninjaService.buscarPorMissao(missao.getId());

        return ninjas.stream()
                .filter(n -> !n.getId().equals(ninja.getId()))
                .anyMatch(n -> ninjaQualificadoParaMissao(n, missao));
    }
}