package com.codesfree.prueba.model.saas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "productos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_productos_tienda_slug", columnNames = {"tienda_id", "slug"}),
                @UniqueConstraint(name = "uk_productos_empresa_sku", columnNames = {"empresa_id", "sku"})
        },
        indexes = {
                @Index(name = "idx_productos_tienda_slug", columnList = "tienda_id, slug", unique = true),
                @Index(name = "idx_productos_empresa_sku", columnList = "empresa_id, sku", unique = true)
        }
)
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tienda_id", nullable = false)
    private Tienda tienda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_principal_id")
    private Proveedor proveedorPrincipal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_registro_id")
    private Usuario usuarioRegistro;

    @Column(nullable = false, length = 180)
    private String nombre;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(length = 100)
    private String marca;

    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "costo_referencial", precision = 12, scale = 2)
    private BigDecimal costoReferencial;

    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida = "unidad";

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_producto", nullable = false, length = 30)
    private EstadoProducto estadoProducto = EstadoProducto.borrador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoGeneral estado = EstadoGeneral.activo;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
