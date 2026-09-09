package br.com.yourpethealth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "yp_t_veterinarios")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(nullable = false, unique = true, length = 20)
    private String crmv;

    @Column(length = 100)
    private String especialidade;
}