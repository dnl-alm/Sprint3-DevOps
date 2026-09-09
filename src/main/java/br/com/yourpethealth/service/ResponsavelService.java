package br.com.yourpethealth.service;

import br.com.yourpethealth.dto.request.ResponsavelRequest;
import br.com.yourpethealth.dto.response.ResponsavelResponse;
import br.com.yourpethealth.entity.Responsavel;
import br.com.yourpethealth.exception.IdNaoEncontradoException;
import br.com.yourpethealth.repository.ResponsavelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResponsavelService {

    private final ResponsavelRepository repository;

    @Transactional
    public ResponsavelResponse criar(ResponsavelRequest request) {
        var responsavel = Responsavel.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .build();

        return ResponsavelResponse.from(repository.save(responsavel));
    }

    @Transactional(readOnly = true)
    public List<ResponsavelResponse> listar() {
        return repository.findAll().stream().map(ResponsavelResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ResponsavelResponse buscarPorId(Long id) {
        return ResponsavelResponse.from(carregar(id));
    }

    @Transactional
    public ResponsavelResponse atualizar(Long id, ResponsavelRequest request) {
        var responsavel = carregar(id);

        responsavel.setNome(request.nome());
        responsavel.setEmail(request.email());
        responsavel.setTelefone(request.telefone());

        return ResponsavelResponse.from(repository.save(responsavel));
    }

    @Transactional
    public void remover(Long id) {
        repository.delete(carregar(id));
    }

    private Responsavel carregar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException("Responsável não encontrado"));
    }
}