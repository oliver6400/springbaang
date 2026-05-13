package com.codesfree.prueba.model.saas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "permisos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_permisos_codigo", columnNames = "codigo")
        },
        indexes = {
                @Index(name = "idx_permisos_codigo", columnList = "codigo", unique = true)
        }
)
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String modulo;

    @Column(nullable = false, length = 50)
    private String accion;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoGeneral estado = EstadoGeneral.activo;
}
