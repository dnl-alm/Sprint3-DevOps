package br.com.yourpethealth.entity;

import br.com.yourpethealth.entity.enums.SexoPet;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "yp_t_pets")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Responsavel responsavel;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 100)
    private String raca;

    private Integer idade;

    @Column(precision = 5, scale = 2)
    private BigDecimal peso;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo_pet", nullable = false, length = 20)
    private SexoPet sexoPet;
}