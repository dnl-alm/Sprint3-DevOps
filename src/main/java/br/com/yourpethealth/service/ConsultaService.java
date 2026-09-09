package br.com.yourpethealth.service;

import br.com.yourpethealth.dto.request.ConsultaAtualizacaoRequest;
import br.com.yourpethealth.dto.request.ConsultaConclusaoRequest;
import br.com.yourpethealth.dto.request.ConsultaRequest;
import br.com.yourpethealth.dto.response.ConsultaResponse;
import br.com.yourpethealth.entity.Consulta;
import br.com.yourpethealth.entity.HistoricoClinico;
import br.com.yourpethealth.entity.enums.StatusConsulta;
import br.com.yourpethealth.entity.enums.TipoHistorico;
import br.com.yourpethealth.exception.AcessoNegadoException;
import br.com.yourpethealth.exception.IdNaoEncontradoException;
import br.com.yourpethealth.exception.RegraNegocioException;
import br.com.yourpethealth.repository.ConsultaRepository;
import br.com.yourpethealth.repository.HistoricoClinicoRepository;
import br.com.yourpethealth.repository.PetRepository;
import br.com.yourpethealth.repository.VeterinarioRepository;
import br.com.yourpethealth.security.UsuarioLogado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PetRepository petRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final HistoricoClinicoRepository historicoRepository;
    private final UsuarioLogado usuarioLogado;
    private final RegrasAgendamento regrasAgendamento;

    @Transactional
    public ConsultaResponse criar(ConsultaRequest request) {
        Long responsavelId = usuarioLogado.responsavelId();

        var pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new IdNaoEncontradoException("Pet não encontrado"));

        if (!pet.getResponsavel().getId().equals(responsavelId)) {
            throw new AcessoNegadoException("Recurso não encontrado ou inacessível");
        }

        var veterinario = veterinarioRepository.findById(request.veterinarioId())
                .orElseThrow(() -> new IdNaoEncontradoException("Veterinário não encontrado"));

        regrasAgendamento.validar(
                pet.getId(), veterinario.getId(), request.data(), null);

        var consulta = Consulta.builder()
                .pet(pet)
                .veterinario(veterinario)
                .tipo(request.tipo())
                .descricao(request.descricao())
                .data(request.data())
                .status(StatusConsulta.AGENDADA)
                .build();

        return ConsultaResponse.from(consultaRepository.save(consulta));
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> listar() {
        return consultaRepository
                .findByPetResponsavelIdOrderByDataDesc(usuarioLogado.responsavelId())
                .stream().map(ConsultaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponse> agenda(LocalDate data) {
        LocalDate dia = data != null ? data : LocalDate.now();

        return consultaRepository.findByVeterinarioIdAndDataBetweenOrderByData(
                        usuarioLogado.veterinarioId(),
                        dia.atStartOfDay(),
                        dia.atTime(23, 59, 59))
                .stream().map(ConsultaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ConsultaResponse buscarPorId(Long id) {
        return ConsultaResponse.from(carregarComLeitura(id));
    }

    @Transactional
    public ConsultaResponse atualizar(Long id, ConsultaAtualizacaoRequest request) {
        var consulta = carregarComPosseDoResponsavel(id);

        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new RegraNegocioException("Só é possível alterar consultas agendadas");
        }

        regrasAgendamento.validar(
                consulta.getPet().getId(),
                consulta.getVeterinario().getId(),
                request.data(),
                consulta.getId());

        consulta.setTipo(request.tipo());
        consulta.setDescricao(request.descricao());
        consulta.setData(request.data());

        return ConsultaResponse.from(consultaRepository.save(consulta));
    }

    @Transactional
    public ConsultaResponse cancelar(Long id) {
        var consulta = carregarComPosseDoResponsavel(id);

        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new RegraNegocioException("Só é possível cancelar consultas agendadas");
        }

        consulta.setStatus(StatusConsulta.CANCELADA);
        return ConsultaResponse.from(consultaRepository.save(consulta));
    }

    @Transactional
    public ConsultaResponse concluir(Long id, ConsultaConclusaoRequest request) {
        var consulta = carregar(id);

        if (!consulta.getVeterinario().getId().equals(usuarioLogado.veterinarioId())) {
            throw new AcessoNegadoException("Recurso não encontrado ou inacessível");
        }
        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new RegraNegocioException("Esta consulta já foi concluída ou cancelada");
        }

        consulta.setStatus(StatusConsulta.REALIZADA);
        consulta.setObservacoes(request.observacoes());

        historicoRepository.save(HistoricoClinico.builder()
                .pet(consulta.getPet())
                .tipo(TipoHistorico.CONSULTA)
                .descricao(request.observacoes())
                .data(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build());

        return ConsultaResponse.from(consultaRepository.save(consulta));
    }

    @Transactional
    public void remover(Long id) {
        var consulta = carregarComPosseDoResponsavel(id);

        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new RegraNegocioException("Só é possível excluir consultas agendadas");
        }

        consultaRepository.delete(consulta);
    }

    private Consulta carregarComLeitura(Long id) {
        var consulta = carregar(id);

        if (usuarioLogado.ehVeterinario()) {
            if (consulta.getVeterinario().getId().equals(usuarioLogado.veterinarioId())) {
                return consulta;
            }
        } else if (consulta.getPet().getResponsavel().getId()
                .equals(usuarioLogado.responsavelId())) {
            return consulta;
        }

        throw new AcessoNegadoException("Recurso não encontrado ou inacessível");
    }

    private Consulta carregarComPosseDoResponsavel(Long id) {
        var consulta = carregar(id);

        if (!consulta.getPet().getResponsavel().getId()
                .equals(usuarioLogado.responsavelId())) {
            throw new AcessoNegadoException("Recurso não encontrado ou inacessível");
        }
        return consulta;
    }

    private Consulta carregar(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException("Consulta não encontrada"));
    }
}