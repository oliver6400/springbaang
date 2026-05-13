package com.codesfree.prueba.model.saas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "recepciones_compra",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_recepciones_compra_empresa_codigo", columnNames = {"empresa_id", "codigo"})
        },
        indexes = {
                @Index(name = "idx_recepciones_compra_empresa_codigo", columnList = "empresa_id, codigo", unique = true)
        }
)
public class RecepcionCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id", nullable = false)
    private OrdenCompra ordenCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_id", nullable = false)
    private Almacen almacen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_recepcion_id")
    private Usuario usuarioRecepcion;

    @Column(nullable = false, length = 80)
    private String codigo;

    @Column(name = "fecha_recepcion", nullable = false)
    private Instant fechaRecepcion;

    @Column(columnDefinition = "text")
    private String observaciones;

    @Column(name = "created_at")
    private Instant createdAt;
}
