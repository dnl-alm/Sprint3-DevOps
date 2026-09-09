package br.com.yourpethealth.service;

import br.com.yourpethealth.entity.enums.StatusConsulta;
import br.com.yourpethealth.exception.RegraNegocioException;
import br.com.yourpethealth.exception.ValidacaoException;
import br.com.yourpethealth.repository.ConsultaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Component
public class RegrasAgendamento {

    private final ConsultaRepository consultaRepository;
    private final int antecedenciaMinimaHoras;
    private final int horaAbertura;
    private final int horaFechamento;
    private final int janelaConflitoMinutos;

    public RegrasAgendamento(
            ConsultaRepository consultaRepository,
            @Value("${app.agendamento.antecedencia-minima-horas}") int antecedenciaMinimaHoras,
            @Value("${app.agendamento.hora-abertura}") int horaAbertura,
            @Value("${app.agendamento.hora-fechamento}") int horaFechamento,
            @Value("${app.agendamento.janela-conflito-minutos}") int janelaConflitoMinutos) {

        this.consultaRepository = consultaRepository;
        this.antecedenciaMinimaHoras = antecedenciaMinimaHoras;
        this.horaAbertura = horaAbertura;
        this.horaFechamento = horaFechamento;
        this.janelaConflitoMinutos = janelaConflitoMinutos;
    }

    public void validar(Long petId, Long veterinarioId, LocalDateTime data, Long consultaId) {
        validarAntecedencia(data);
        validarHorarioComercial(data);
        validarConflitoVeterinario(veterinarioId, data, consultaId);
        validarConflitoPet(petId, data, consultaId);
    }

    private void validarAntecedencia(LocalDateTime data) {
        LocalDateTime minimo = LocalDateTime.now().plusHours(antecedenciaMinimaHoras);

        if (data.isBefore(minimo)) {
            throw new ValidacaoException(
                    "A consulta deve ser agendada com no mínimo %d horas de antecedência"
                            .formatted(antecedenciaMinimaHoras));
        }
    }

    private void validarHorarioComercial(LocalDateTime data) {
        if (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ValidacaoException("A clínica não atende aos domingos");
        }

        int hora = data.getHour();
        boolean forjaDoExpediente = hora < horaAbertura
                || hora > horaFechamento
                || (hora == horaFechamento && data.getMinute() > 0);

        if (forjaDoExpediente) {
            throw new ValidacaoException(
                    "O horário de atendimento é de %02d:00 às %02d:00, de segunda a sábado"
                            .formatted(horaAbertura, horaFechamento));
        }
    }

    private void validarConflitoVeterinario(Long veterinarioId, LocalDateTime data, Long consultaId) {
        LocalDateTime inicio = data.minusMinutes(janelaConflitoMinutos);
        LocalDateTime fim = data.plusMinutes(janelaConflitoMinutos);

        boolean ocupado = consultaRepository
                .findByVeterinarioIdAndStatusAndDataBetween(
                        veterinarioId, StatusConsulta.AGENDADA, inicio, fim)
                .stream()
                .anyMatch(c -> !c.getId().equals(consultaId));

        if (ocupado) {
            throw new RegraNegocioException(
                    "O veterinário já possui uma consulta agendada próxima a este horário");
        }
    }

    private void validarConflitoPet(Long petId, LocalDateTime data, Long consultaId) {
        boolean ocupado = consultaRepository
                .findByPetIdAndStatusAndData(petId, StatusConsulta.AGENDADA, data)
                .stream()
                .anyMatch(c -> !c.getId().equals(consultaId));

        if (ocupado) {
            throw new RegraNegocioException(
                    "Este pet já possui uma consulta agendada neste horário");
        }
    }
}