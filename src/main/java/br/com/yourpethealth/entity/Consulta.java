package br.com.yourpethealth.entity;

import br.com.yourpethealth.entity.enums.StatusConsulta;
import br.com.yourpethealth.entity.enums.TipoConsulta;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "yp_t_consultas")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Veterinario veterinario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoConsulta tipo;

    @Column(length = 1000)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime data;

    @Column(length = 1000)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConsulta status;
}