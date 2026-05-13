package com.codesfree.prueba.controller;

import com.codesfree.prueba.dto.SaasRequests;
import com.codesfree.prueba.service.SaasManagementService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saas")
public class SaasManagementController {

    private final SaasManagementService saasManagementService;

    public SaasManagementController(SaasManagementService saasManagementService) {
        this.saasManagementService = saasManagementService;
    }

    @GetMapping("/planes")
    public List<Map<String, Object>> listPlanes() {
        return saasManagementService.listPlanes();
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard(Authentication authentication) {
        return saasManagementService.buildDashboard(authentication.getName());
    }

    @PostMapping("/planes")
    public ResponseEntity<Map<String, Object>> createPlan(@Valid @RequestBody SaasRequests.PlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createPlan(request));
    }

    @PutMapping("/planes/{planId}")
    public Map<String, Object> updatePlan(@PathVariable Long planId, @Valid @RequestBody SaasRequests.PlanRequest request) {
        return saasManagementService.updatePlan(planId, request);
    }

    @GetMapping("/usuarios-sistema")
    public List<Map<String, Object>> listSystemUsers(@RequestParam(required = false) String role) {
        return saasManagementService.listSystemUsers(role);
    }

    @PostMapping("/usuarios-sistema")
    public ResponseEntity<Map<String, Object>> createSystemUser(@Valid @RequestBody SaasRequests.SystemUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createSystemUser(request));
    }

    @PutMapping("/usuarios-sistema/{userId}")
    public Map<String, Object> updateSystemUser(@PathVariable Long userId, @Valid @RequestBody SaasRequests.SystemUserRequest request) {
        return saasManagementService.updateSystemUser(userId, request);
    }

    @GetMapping("/empresas")
    public List<Map<String, Object>> listEmpresas() {
        return saasManagementService.listEmpresas();
    }

    @PostMapping("/empresas")
    public ResponseEntity<Map<String, Object>> createEmpresa(@Valid @RequestBody SaasRequests.EmpresaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createEmpresa(request));
    }

    @GetMapping("/suscripciones")
    public List<Map<String, Object>> listSuscripciones() {
        return saasManagementService.listSuscripciones();
    }

    @PostMapping("/suscripciones")
    public ResponseEntity<Map<String, Object>> createSuscripcion(@Valid @RequestBody SaasRequests.SuscripcionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createSuscripcion(request));
    }

    @GetMapping("/tiendas")
    public List<Map<String, Object>> listTiendas(@RequestParam(required = false) Long empresaId) {
        return saasManagementService.listTiendas(empresaId);
    }

    @PostMapping("/tiendas")
    public ResponseEntity<Map<String, Object>> createTienda(@Valid @RequestBody SaasRequests.TiendaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createTienda(request));
    }

    @GetMapping("/almacenes")
    public List<Map<String, Object>> listAlmacenes(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Long tiendaId) {
        return saasManagementService.listAlmacenes(empresaId, tiendaId);
    }

    @PostMapping("/almacenes")
    public ResponseEntity<Map<String, Object>> createAlmacen(@Valid @RequestBody SaasRequests.AlmacenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createAlmacen(request));
    }

    @GetMapping("/categorias")
    public List<Map<String, Object>> listCategorias(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Long tiendaId) {
        return saasManagementService.listCategorias(empresaId, tiendaId);
    }

    @PostMapping("/categorias")
    public ResponseEntity<Map<String, Object>> createCategoria(@Valid @RequestBody SaasRequests.CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createCategoria(request));
    }

    @GetMapping("/usuarios")
    public List<Map<String, Object>> listUsuarios(@RequestParam(required = false) Long empresaId) {
        return saasManagementService.listUsuarios(empresaId);
    }

    @PostMapping("/usuarios")
    public ResponseEntity<Map<String, Object>> createUsuario(@Valid @RequestBody SaasRequests.UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createUsuario(request));
    }

    @PostMapping("/usuarios/{usuarioId}/roles")
    public ResponseEntity<Map<String, Object>> assignRoleToUser(
            @PathVariable Long usuarioId,
            @Valid @RequestBody SaasRequests.UsuarioRolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.assignRoleToUser(usuarioId, request));
    }

    @GetMapping("/roles")
    public List<Map<String, Object>> listRoles(@RequestParam(required = false) Long empresaId) {
        return saasManagementService.listRoles(empresaId);
    }

    @PostMapping("/roles")
    public ResponseEntity<Map<String, Object>> createRol(@Valid @RequestBody SaasRequests.RolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createRol(request));
    }

    @GetMapping("/permisos")
    public List<Map<String, Object>> listPermisos() {
        return saasManagementService.listPermisos();
    }

    @PostMapping("/permisos")
    public ResponseEntity<Map<String, Object>> createPermiso(@Valid @RequestBody SaasRequests.PermisoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createPermiso(request));
    }

    @PostMapping("/roles/{rolId}/permisos/{permisoId}")
    public ResponseEntity<Map<String, Object>> assignPermissionToRole(@PathVariable Long rolId, @PathVariable Long permisoId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.assignPermissionToRole(rolId, permisoId));
    }

    @GetMapping("/archivos")
    public List<Map<String, Object>> listArchivos(@RequestParam(required = false) Long empresaId) {
        return saasManagementService.listArchivos(empresaId);
    }

    @PostMapping("/archivos")
    public ResponseEntity<Map<String, Object>> createArchivo(@Valid @RequestBody SaasRequests.ArchivoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createArchivo(request));
    }

    @GetMapping("/proveedores")
    public List<Map<String, Object>> listProveedores(@RequestParam(required = false) Long empresaId) {
        return saasManagementService.listProveedores(empresaId);
    }

    @PostMapping("/proveedores")
    public ResponseEntity<Map<String, Object>> createProveedor(@Valid @RequestBody SaasRequests.ProveedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createProveedor(request));
    }

    @GetMapping("/productos")
    public List<Map<String, Object>> listProductos(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Long tiendaId) {
        return saasManagementService.listProductos(empresaId, tiendaId);
    }

    @GetMapping("/catalogo")
    public List<Map<String, Object>> listCatalogo(
            @RequestParam Long tiendaId,
            @RequestParam(required = false) Long categoriaId) {
        return saasManagementService.listCatalogo(tiendaId, categoriaId);
    }

    @PostMapping("/productos")
    public ResponseEntity<Map<String, Object>> createProducto(@Valid @RequestBody SaasRequests.ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createProducto(request));
    }

    @GetMapping("/clientes")
    public List<Map<String, Object>> listClientes(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Long tiendaId) {
        return saasManagementService.listClientes(empresaId, tiendaId);
    }

    @PostMapping("/clientes")
    public ResponseEntity<Map<String, Object>> createCliente(@Valid @RequestBody SaasRequests.ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createCliente(request));
    }

    @GetMapping("/inventarios")
    public List<Map<String, Object>> listInventario(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Long almacenId) {
        return saasManagementService.listInventario(empresaId, almacenId);
    }

    @PostMapping("/inventarios")
    public ResponseEntity<Map<String, Object>> saveInventario(@Valid @RequestBody SaasRequests.InventarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.saveInventario(request));
    }

    @GetMapping("/ordenes-venta")
    public List<Map<String, Object>> listOrdenesVenta(@RequestParam(required = false) Long empresaId) {
        return saasManagementService.listOrdenesVenta(empresaId);
    }

    @PostMapping("/ordenes-venta")
    public ResponseEntity<Map<String, Object>> createOrdenVenta(@Valid @RequestBody SaasRequests.OrdenVentaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createOrdenVenta(request));
    }

    @PutMapping("/ordenes-venta/{ordenVentaId}/estado")
    public Map<String, Object> changeOrdenVentaEstado(
            @PathVariable Long ordenVentaId,
            @Valid @RequestBody SaasRequests.CambioEstadoOrdenVentaRequest request) {
        return saasManagementService.changeOrdenVentaEstado(ordenVentaId, request);
    }

    @GetMapping("/ordenes-compra")
    public List<Map<String, Object>> listOrdenesCompra(@RequestParam(required = false) Long empresaId) {
        return saasManagementService.listOrdenesCompra(empresaId);
    }

    @PostMapping("/ordenes-compra")
    public ResponseEntity<Map<String, Object>> createOrdenCompra(@Valid @RequestBody SaasRequests.OrdenCompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.createOrdenCompra(request));
    }

    @PostMapping("/recepciones-compra")
    public ResponseEntity<Map<String, Object>> registerRecepcionCompra(@Valid @RequestBody SaasRequests.RecepcionCompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saasManagementService.registerRecepcionCompra(request));
    }

    @GetMapping("/lotes")
    public List<Map<String, Object>> listLotes(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Long almacenId) {
        return saasManagementService.listLotes(empresaId, almacenId);
    }

    @GetMapping("/movimientos-inventario")
    public List<Map<String, Object>> listMovimientos(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Long almacenId) {
        return saasManagementService.listMovimientos(empresaId, almacenId);
    }

    @GetMapping("/bitacora-eventos")
    public List<Map<String, Object>> listBitacora(@RequestParam(required = false) Long empresaId) {
        return saasManagementService.listBitacora(empresaId);
    }
}
