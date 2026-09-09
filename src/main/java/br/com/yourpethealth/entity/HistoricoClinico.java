package br.com.yourpethealth.entity;

import br.com.yourpethealth.entity.enums.TipoHistorico;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "yp_t_historico_clinico")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HistoricoClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoHistorico tipo;

    @Column(length = 1000)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime data;
}