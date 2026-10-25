
package lucassena.CadastroDeNinjas.ninjaMissao;

import lucassena.CadastroDeNinjas.exceptions.RecursoNaoEncontradoException;
import lucassena.CadastroDeNinjas.exceptions.RegraDeNegocioException;
import lucassena.CadastroDeNinjas.missaoStatus.MissaoStatusService;
import lucassena.CadastroDeNinjas.missoes.*;
import lucassena.CadastroDeNinjas.ninjas.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NinjaMissaoService {

    private final NinjaRepository ninjaRepository;
    private final MissoesRepository missoesRepository;
    private final MissaoStatusService missaoStatusService;
    private final NinjaMapper ninjaMapper;
    private final MissoesMapper missoesMapper;

    public NinjaMissaoService(
            NinjaRepository ninjaRepository,
            MissoesRepository missoesRepository,
            MissaoStatusService missaoStatusService,
            NinjaMapper ninjaMapper,
            MissoesMapper missoesMapper) {

        this.ninjaRepository = ninjaRepository;
        this.missoesRepository = missoesRepository;
        this.missaoStatusService = missaoStatusService;
        this.ninjaMapper = ninjaMapper;
        this.missoesMapper = missoesMapper;
    }

    // =========================
    // ATRIBUIÇÃO
    // =========================

    @Transactional
    public NinjaResponseDTO atribuirMissao(
            Long ninjaId, Long missaoId) {

        NinjaModel ninja = buscarNinja(ninjaId);

        if (ninja.getMissoes() != null) {
            throw new RegraDeNegocioException(
                    "Ninja já possui uma missão");
        }

        MissoesModel missao = buscarMissao(missaoId);

        validarMissaoParaReceberNinja(missao);

        ninja.setMissoes(missao);

        NinjaModel ninjaSalvo =
                ninjaRepository.saveAndFlush(ninja);

        missaoStatusService.reavaliarStatus(missao);

        return ninjaMapper.map(ninjaSalvo);
    }

    // =========================
    // REMOÇÃO DE VÍNCULO
    // =========================

    @Transactional
    public NinjaResponseDTO removerMissao(Long ninjaId) {

        NinjaModel ninja = buscarNinja(ninjaId);

        if (ninja.getMissoes() == null) {
            throw new RegraDeNegocioException(
                    "Ninja não possui uma missão");
        }

        MissoesModel missao = ninja.getMissoes();

        ninja.setMissoes(null);

        ninjaRepository.saveAndFlush(ninja);

        missaoStatusService.reavaliarStatus(missao);

        return ninjaMapper.map(ninja);
    }

    // =========================
    // TRANSFERÊNCIA
    // =========================

    @Transactional
    public NinjaResponseDTO transferirMissao(
            Long ninjaId, Long novaMissaoId) {

        NinjaModel ninja = buscarNinja(ninjaId);

        if (ninja.getMissoes() == null) {
            throw new RegraDeNegocioException(
                    "Ninja não possui uma missão");
        }

        MissoesModel missaoAtual = ninja.getMissoes();
        MissoesModel novaMissao = buscarMissao(novaMissaoId);

        if (missaoAtual.getId().equals(novaMissao.getId())) {
            throw new RegraDeNegocioException(
                    "Ninja já está atribuído a esta missão");
        }

        validarMissaoParaReceberNinja(novaMissao);

        if (missaoAtual.getStatus() == StatusMissao.EM_ANDAMENTO
                && missaoStatusService.ninjaQualificadoParaMissao(
                ninja, missaoAtual)
                && !existeOutroNinjaQualificado(ninja, missaoAtual)) {

            throw new RegraDeNegocioException(
                    "Transferência não permitida: o ninja é o último qualificado da missão em andamento");
        }

        ninja.setMissoes(novaMissao);

        ninjaRepository.saveAndFlush(ninja);

        missaoStatusService.reavaliarStatus(missaoAtual);
        missaoStatusService.reavaliarStatus(novaMissao);

        return ninjaMapper.map(ninja);
    }

    // =========================
    // EXCLUSÃO DE NINJA
    // =========================

    @Transactional
    public void deletarNinja(Long id) {

        NinjaModel ninja = buscarNinja(id);

        MissoesModel missao = ninja.getMissoes();

        ninjaRepository.delete(ninja);
        ninjaRepository.flush();

        if (missao != null) {
            missaoStatusService.reavaliarStatus(missao);
        }
    }

    // =========================
    // INATIVAÇÃO DE MISSÃO
    // =========================

    @Transactional
    public MissoesResponseDTO inativarMissao(Long id) {

        MissoesModel missao = buscarMissao(id);

        if (missao.getStatus() == StatusMissao.CONCLUIDA) {
            throw new RegraDeNegocioException(
                    "Não é possível inativar uma missão concluída");
        }

        missao.setStatus(StatusMissao.INATIVA);

        removerTodosOsVinculos(missao.getId());

        MissoesModel missaoSalva =
                missoesRepository.save(missao);

        return missoesMapper.map(missaoSalva);
    }

    // =========================
    // CONCLUSÃO DE MISSÃO
    // =========================

    @Transactional
    public MissoesResponseDTO concluirMissao(Long id) {

        MissoesModel missao = buscarMissao(id);

        if (missao.getStatus() != StatusMissao.EM_ANDAMENTO) {
            throw new RegraDeNegocioException(
                    "Só é possível concluir uma missão em andamento");
        }

        missao.setStatus(StatusMissao.CONCLUIDA);

        removerTodosOsVinculos(missao.getId());

        MissoesModel missaoSalva =
                missoesRepository.save(missao);

        return missoesMapper.map(missaoSalva);
    }

    // =========================
    // MÉTODOS AUXILIARES
    // =========================

    private NinjaModel buscarNinja(Long id) {

        return ninjaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Ninja não encontrado"));
    }

    private MissoesModel buscarMissao(Long id) {

        return missoesRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Missão não encontrada"));
    }

    private void validarMissaoParaReceberNinja(
            MissoesModel missao) {

        if (missao.getStatus() == StatusMissao.INATIVA) {
            throw new RegraDeNegocioException(
                    "Não é possível atribuir ninja a uma missão inativa");
        }

        if (missao.getStatus() == StatusMissao.CONCLUIDA) {
            throw new RegraDeNegocioException(
                    "Não é possível atribuir ninja a uma missão concluída");
        }
    }

    private boolean existeOutroNinjaQualificado(
            NinjaModel ninja, MissoesModel missao) {

        List<NinjaModel> ninjas =
                ninjaRepository.findByMissoes_Id(missao.getId());

        return ninjas.stream()
                .filter(n -> !n.getId().equals(ninja.getId()))
                .anyMatch(n ->
                        missaoStatusService.ninjaQualificadoParaMissao(
                                n, missao));
    }

    private void removerTodosOsVinculos(Long missaoId) {

        List<NinjaModel> ninjas =
                ninjaRepository.findByMissoes_Id(missaoId);

        for (NinjaModel ninja : ninjas) {
            ninja.setMissoes(null);
        }

        ninjaRepository.saveAllAndFlush(ninjas);
    }
}
