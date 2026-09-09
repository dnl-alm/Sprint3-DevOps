package br.com.yourpethealth.service;

import br.com.yourpethealth.dto.response.HistoricoResponse;
import br.com.yourpethealth.exception.IdNaoEncontradoException;
import br.com.yourpethealth.repository.HistoricoClinicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricoClinicoService {

    private final HistoricoClinicoRepository repository;
    private final PetService petService;

    @Transactional(readOnly = true)
    public List<HistoricoResponse> listarPorPet(Long petId) {
        petService.carregarComLeitura(petId);   // valida acesso ao pet
        return repository.findByPetId(petId)
                .stream().map(HistoricoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public HistoricoResponse buscarPorId(Long id) {
        var historico = repository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException("Histórico clínico não encontrado"));

        petService.carregarComLeitura(historico.getPet().getId());
        return HistoricoResponse.from(historico);
    }
}