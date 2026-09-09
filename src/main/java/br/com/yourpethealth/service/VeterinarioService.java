package br.com.yourpethealth.service;

import br.com.yourpethealth.dto.request.VeterinarioRequest;
import br.com.yourpethealth.dto.response.VeterinarioResponse;
import br.com.yourpethealth.entity.Veterinario;
import br.com.yourpethealth.exception.IdNaoEncontradoException;
import br.com.yourpethealth.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VeterinarioService {

    private final VeterinarioRepository repository;

    @Transactional
    public VeterinarioResponse criar(VeterinarioRequest request) {
        var veterinario = Veterinario.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .crmv(request.crmv())
                .especialidade(request.especialidade())
                .build();

        return VeterinarioResponse.from(repository.save(veterinario));
    }

    @Transactional(readOnly = true)
    public List<VeterinarioResponse> listar() {
        return repository.findAll().stream().map(VeterinarioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VeterinarioResponse buscarPorId(Long id) {
        return VeterinarioResponse.from(carregar(id));
    }

    @Transactional
    public VeterinarioResponse atualizar(Long id, VeterinarioRequest request) {
        var veterinario = carregar(id);

        veterinario.setNome(request.nome());
        veterinario.setEmail(request.email());
        veterinario.setTelefone(request.telefone());
        veterinario.setCrmv(request.crmv());
        veterinario.setEspecialidade(request.especialidade());

        return VeterinarioResponse.from(repository.save(veterinario));
    }

    @Transactional
    public void remover(Long id) {
        repository.delete(carregar(id));
    }

    private Veterinario carregar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException("Veterinário não encontrado"));
    }
}