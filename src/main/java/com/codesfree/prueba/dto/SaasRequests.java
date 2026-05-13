package com.codesfree.prueba.dto;

import com.codesfree.prueba.model.saas.EstadoOrdenCompra;
import com.codesfree.prueba.model.saas.EstadoOrdenVenta;
import com.codesfree.prueba.model.saas.EstadoGeneral;
import com.codesfree.prueba.model.saas.TipoArchivo;
import com.codesfree.prueba.model.saas.TipoRol;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public final class SaasRequests {

    private SaasRequests() {
    }

    @Getter
    @Setter
    public static class PlanRequest {
        @NotBlank
        private String nombre;
        private String descripcion;
        @NotNull
        private BigDecimal precioMensual;
        private Integer limiteTiendas;
        private Integer limiteAlmacenes;
        private Integer limiteUsuarios;
        private Integer limiteProductos;
        private Boolean incluyeCrm;
        private Boolean incluyeIa;
        private String soporte;
        private EstadoGeneral estado;
    }

    @Getter
    @Setter
    public static class SystemUserRequest {
        @NotBlank
        private String username;
        private String password;
        @NotNull
        private com.codesfree.prueba.model.AppRole role;
        private Boolean activo;
    }

    @Getter
    @Setter
    public static class EmpresaRequest {
        @NotBlank
        private String nombreComercial;
        private String razonSocial;
        private String nit;
        private String correo;
        private String telefono;
        private String direccion;
        private Long planActualId;
        private Long usuarioRegistroId;
    }

    @Getter
    @Setter
    public static class SuscripcionRequest {
        @NotNull
        private Long empresaId;
        @NotNull
        private Long planSuscripcionId;
        private Long usuarioRegistroId;
        @NotNull
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private BigDecimal montoPagado;
        private Boolean renovacionAutomatica;
    }

    @Getter
    @Setter
    public static class TiendaRequest {
        @NotNull
        private Long empresaId;
        private Long usuarioRegistroId;
        @NotBlank
        private String nombre;
        @NotBlank
        private String slug;
        private String rubro;
        private String descripcion;
        private String correoContacto;
        private String telefono;
    }

    @Getter
    @Setter
    public static class AlmacenRequest {
        @NotNull
        private Long empresaId;
        @NotNull
        private Long tiendaId;
        private Long usuarioRegistroId;
        @NotBlank
        private String nombre;
        private String direccion;
        private String referencia;
    }

    @Getter
    @Setter
    public static class CategoriaRequest {
        @NotNull
        private Long empresaId;
        @NotNull
        private Long tiendaId;
        private Long categoriaPadreId;
        private Long usuarioRegistroId;
        @NotBlank
        private String nombre;
        @NotBlank
        private String slug;
        private String descripcion;
    }

    @Getter
    @Setter
    public static class UsuarioRequest {
        private Long empresaId;
        private Long tiendaId;
        private Long fotoArchivoId;
        @NotBlank
        private String nombres;
        private String apellidos;
        @NotBlank
        private String email;
        @NotBlank
        private String password;
        private String telefono;
    }

    @Getter
    @Setter
    public static class RolRequest {
        private Long empresaId;
        @NotBlank
        private String codigo;
        @NotBlank
        private String nombre;
        private String descripcion;
        private TipoRol tipo;
        private Boolean esSistema;
    }

    @Getter
    @Setter
    public static class PermisoRequest {
        @NotBlank
        private String codigo;
        @NotBlank
        private String modulo;
        @NotBlank
        private String accion;
        private String descripcion;
    }

    @Getter
    @Setter
    public static class UsuarioRolRequest {
        @NotNull
        private Long rolId;
        private Long empresaId;
        private Long tiendaId;
    }

    @Getter
    @Setter
    public static class ArchivoRequest {
        private Long empresaId;
        private Long usuarioSubidaId;
        @NotBlank
        private String nombreOriginal;
        @NotBlank
        private String rutaArchivo;
        private String urlPublica;
        @NotNull
        private TipoArchivo tipoArchivo;
        private String extension;
        private Long tamanioBytes;
    }

    @Getter
    @Setter
    public static class ProveedorRequest {
        @NotNull
        private Long empresaId;
        private Long usuarioRegistroId;
        @NotBlank
        private String nombre;
        private String nit;
        private String correo;
        private String telefono;
        private String direccion;
        private String contactoPrincipal;
    }

    @Getter
    @Setter
    public static class ProductoRequest {
        @NotNull
        private Long empresaId;
        @NotNull
        private Long tiendaId;
        private Long categoriaId;
        private Long proveedorPrincipalId;
        private Long usuarioRegistroId;
        @NotBlank
        private String nombre;
        @NotBlank
        private String slug;
        @NotBlank
        private String sku;
        private String descripcion;
        private String marca;
        @NotNull
        private BigDecimal precioVenta;
        private BigDecimal costoReferencial;
        private String unidadMedida;
    }

    @Getter
    @Setter
    public static class ClienteRequest {
        @NotNull
        private Long empresaId;
        @NotNull
        private Long tiendaId;
        @NotBlank
        private String nombres;
        private String apellidos;
        private String email;
        private String telefono;
        private String documentoIdentidad;
        private String direccion;
    }

    @Getter
    @Setter
    public static class InventarioRequest {
        @NotNull
        private Long empresaId;
        @NotNull
        private Long almacenId;
        @NotNull
        private Long productoId;
        @NotNull
        @Min(0)
        private Integer cantidadDisponible;
        @Min(0)
        private Integer cantidadReservada;
        @Min(0)
        private Integer stockMinimo;
        @Min(0)
        private Integer stockMaximo;
    }

    @Getter
    @Setter
    public static class OrdenVentaRequest {
        @NotNull
        private Long empresaId;
        @NotNull
        private Long tiendaId;
        @NotNull
        private Long almacenId;
        @NotNull
        private Long clienteId;
        private Long usuarioId;
        @NotBlank
        private String codigo;
        private String observaciones;
        @Valid
        @NotEmpty
        private List<OrdenVentaItemRequest> items = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class OrdenVentaItemRequest {
        @NotNull
        private Long productoId;
        @NotNull
        @Min(1)
        private Integer cantidad;
        private BigDecimal descuento;
    }

    @Getter
    @Setter
    public static class CambioEstadoOrdenVentaRequest {
        @NotNull
        private EstadoOrdenVenta estado;
        private Long usuarioId;
        private String observacion;
    }

    @Getter
    @Setter
    public static class OrdenCompraRequest {
        @NotNull
        private Long empresaId;
        @NotNull
        private Long proveedorId;
        @NotNull
        private Long almacenId;
        private Long usuarioCreacionId;
        @NotBlank
        private String codigo;
        @NotNull
        private LocalDate fechaEmision;
        private LocalDate fechaEstimadaRecepcion;
        private EstadoOrdenCompra estado;
        private String observaciones;
        @Valid
        @NotEmpty
        private List<OrdenCompraItemRequest> items = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class OrdenCompraItemRequest {
        @NotNull
        private Long productoId;
        @NotNull
        @Min(1)
        private Integer cantidadSolicitada;
        @NotNull
        private BigDecimal costoUnitario;
    }

    @Getter
    @Setter
    public static class RecepcionCompraRequest {
        @NotNull
        private Long empresaId;
        @NotNull
        private Long ordenCompraId;
        @NotNull
        private Long almacenId;
        private Long usuarioRecepcionId;
        @NotBlank
        private String codigo;
        private Instant fechaRecepcion;
        private String observaciones;
        @Valid
        @NotEmpty
        private List<RecepcionCompraDetalleRequest> detalles = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class RecepcionCompraDetalleRequest {
        private Long ordenCompraDetalleId;
        @NotNull
        private Long productoId;
        @NotNull
        @Min(1)
        private Integer cantidadRecibida;
        private BigDecimal costoUnitario;
        @NotBlank
        private String codigoLote;
        private LocalDate fechaVencimiento;
    }
}
