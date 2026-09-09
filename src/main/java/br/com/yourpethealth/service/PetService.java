package br.com.yourpethealth.service;

import br.com.yourpethealth.dto.request.PetRequest;
import br.com.yourpethealth.dto.response.PetResponse;
import br.com.yourpethealth.entity.Consulta;
import br.com.yourpethealth.entity.Pet;
import br.com.yourpethealth.entity.enums.StatusConsulta;
import br.com.yourpethealth.exception.AcessoNegadoException;
import br.com.yourpethealth.exception.IdNaoEncontradoException;
import br.com.yourpethealth.exception.RegraNegocioException;
import br.com.yourpethealth.repository.ConsultaRepository;
import br.com.yourpethealth.repository.PetRepository;
import br.com.yourpethealth.repository.ResponsavelRepository;
import br.com.yourpethealth.security.UsuarioLogado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final ResponsavelRepository responsavelRepository;
    private final ConsultaRepository consultaRepository;
    private final UsuarioLogado usuarioLogado;

    @Transactional
    public PetResponse criar(PetRequest request) {
        // O dono vem do token, nunca do corpo da requisição.
        var responsavel = responsavelRepository.findById(usuarioLogado.responsavelId())
                .orElseThrow(() -> new IdNaoEncontradoException("Responsável não encontrado"));

        var pet = Pet.builder()
                .responsavel(responsavel)
                .nome(request.nome())
                .raca(request.raca())
                .idade(request.idade())
                .peso(request.peso())
                .sexoPet(request.sexo())
                .build();

        return toResponse(petRepository.save(pet));
    }

    /** Pets do responsável logado. */
    @Transactional(readOnly = true)
    public List<PetResponse> listar() {
        return petRepository.findByResponsavelId(usuarioLogado.responsavelId())
                .stream().map(this::toResponse).toList();
    }

    /** Busca por nome — exclusiva de veterinários. */
    @Transactional(readOnly = true)
    public List<PetResponse> buscarPorNome(String nome) {
        usuarioLogado.veterinarioId();   // valida o perfil
        return petRepository.findByNomeContainingIgnoreCase(nome)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PetResponse buscarPorId(Long id) {
        return toResponse(carregarComLeitura(id));
    }

    @Transactional
    public PetResponse atualizar(Long id, PetRequest request) {
        var pet = carregarComPosse(id);

        pet.setNome(request.nome());
        pet.setRaca(request.raca());
        pet.setIdade(request.idade());
        pet.setPeso(request.peso());
        pet.setSexoPet(request.sexo());

        return toResponse(petRepository.save(pet));
    }

    @Transactional
    public void remover(Long id) {
        var pet = carregarComPosse(id);

        if (consultaRepository.existsByPetIdAndStatus(pet.getId(), StatusConsulta.AGENDADA)) {
            throw new RegraNegocioException(
                    "Não é possível remover um pet com consultas agendadas");
        }

        petRepository.delete(pet);
    }

    // ---------- Acesso ----------

    /** Leitura: o dono ou qualquer veterinário. */
    Pet carregarComLeitura(Long id) {
        var pet = carregar(id);
        if (usuarioLogado.ehVeterinario()) return pet;

        if (!pet.getResponsavel().getId().equals(usuarioLogado.responsavelId())) {
            throw new AcessoNegadoException("Recurso não encontrado ou inacessível");
        }
        return pet;
    }

    /** Escrita: só o dono. */
    private Pet carregarComPosse(Long id) {
        var pet = carregar(id);
        if (!pet.getResponsavel().getId().equals(usuarioLogado.responsavelId())) {
            throw new AcessoNegadoException("Recurso não encontrado ou inacessível");
        }
        return pet;
    }

    private Pet carregar(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new IdNaoEncontradoException("Pet não encontrado"));
    }

    private PetResponse toResponse(Pet pet) {
        long total = consultaRepository.countByPetId(pet.getId());

        LocalDateTime proxima = consultaRepository
                .findFirstByPetIdAndStatusAndDataAfterOrderByDataAsc(
                        pet.getId(), StatusConsulta.AGENDADA, LocalDateTime.now())
                .map(Consulta::getData)
                .orElse(null);

        return PetResponse.from(pet, total, proxima);
    }
}