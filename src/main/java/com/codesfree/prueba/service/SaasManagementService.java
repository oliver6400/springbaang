package com.codesfree.prueba.service;

import com.codesfree.prueba.dto.SaasRequests;
import com.codesfree.prueba.exception.ResourceNotFoundException;
import com.codesfree.prueba.model.AppRole;
import com.codesfree.prueba.model.AppUser;
import com.codesfree.prueba.repository.AppUserRepository;
import com.codesfree.prueba.model.saas.Almacen;
import com.codesfree.prueba.model.saas.Archivo;
import com.codesfree.prueba.model.saas.BitacoraEvento;
import com.codesfree.prueba.model.saas.Categoria;
import com.codesfree.prueba.model.saas.Cliente;
import com.codesfree.prueba.model.saas.Empresa;
import com.codesfree.prueba.model.saas.EstadoOrdenCompra;
import com.codesfree.prueba.model.saas.EstadoOrdenVenta;
import com.codesfree.prueba.model.saas.Inventario;
import com.codesfree.prueba.model.saas.Lote;
import com.codesfree.prueba.model.saas.LoteDetalle;
import com.codesfree.prueba.model.saas.MovimientoInventario;
import com.codesfree.prueba.model.saas.OrdenCompra;
import com.codesfree.prueba.model.saas.OrdenCompraDetalle;
import com.codesfree.prueba.model.saas.OrdenVenta;
import com.codesfree.prueba.model.saas.OrdenVentaDetalle;
import com.codesfree.prueba.model.saas.OrdenVentaEstado;
import com.codesfree.prueba.model.saas.Permiso;
import com.codesfree.prueba.model.saas.PlanSuscripcion;
import com.codesfree.prueba.model.saas.Producto;
import com.codesfree.prueba.model.saas.Proveedor;
import com.codesfree.prueba.model.saas.RecepcionCompra;
import com.codesfree.prueba.model.saas.RecepcionCompraDetalle;
import com.codesfree.prueba.model.saas.Rol;
import com.codesfree.prueba.model.saas.RolPermiso;
import com.codesfree.prueba.model.saas.Suscripcion;
import com.codesfree.prueba.model.saas.Tienda;
import com.codesfree.prueba.model.saas.TipoRol;
import com.codesfree.prueba.model.saas.TipoMovimientoInventario;
import com.codesfree.prueba.model.saas.Usuario;
import com.codesfree.prueba.model.saas.UsuarioEmpresa;
import com.codesfree.prueba.model.saas.UsuarioRol;
import com.codesfree.prueba.model.saas.UsuarioTienda;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SaasManagementService {

    private static final Logger log = LoggerFactory.getLogger(SaasManagementService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository appUserRepository;

    public SaasManagementService(PasswordEncoder passwordEncoder, AppUserRepository appUserRepository) {
        this.passwordEncoder = passwordEncoder;
        this.appUserRepository = appUserRepository;
    }

    public Map<String, Object> buildDashboard(String username) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + username));

        return switch (appUser.getRole()) {
            case ROLE_SUPERADMIN -> buildSuperadminDashboard(appUser);
            case ROLE_ADMIN_TIENDA, ROLE_ADMIN_TIENDA_LIMITADO -> buildEmpresaDashboard(appUser);
            case ROLE_ENCARGADO_TIENDA -> buildTiendaDashboard(appUser);
            default -> buildFallbackDashboard(appUser);
        };
    }

    public List<Map<String, Object>> listPlanes() {
        return entityManager.createQuery("select p from PlanSuscripcion p order by p.id", PlanSuscripcion.class)
                .getResultList()
                .stream()
                .map(this::toPlanMap)
                .toList();
    }

    private Map<String, Object> buildSuperadminDashboard(AppUser appUser) {
        long empresas = countQuery("select count(e) from Empresa e");
        long tiendas = countQuery("select count(t) from Tienda t");
        long usuarios = countQuery("select count(u) from Usuario u");
        long productos = countQuery("select count(p) from Producto p");
        long clientes = countQuery("select count(c) from Cliente c");
        long ordenes = countQuery("select count(o) from OrdenVenta o");

        List<Map<String, Object>> records = entityManager.createQuery("""
                        select e from Empresa e
                        order by e.id desc
                        """, Empresa.class)
                .setMaxResults(6)
                .getResultList()
                .stream()
                .map(empresa -> orderedMap(
                        "title", empresa.getNombreComercial(),
                        "subtitle", defaultText(empresa.getCorreo(), "Sin correo registrado"),
                        "status", String.valueOf(empresa.getEstado()),
                        "meta", defaultText(idName(empresa.getPlanActual()), "Sin plan asignado")))
                .toList();

        return orderedMap(
                "context", orderedMap(
                        "username", appUser.getUsername(),
                        "role", appUser.getRole().name(),
                        "roleLabel", "Superadmin",
                        "scopeLabel", "Vista global de la plataforma"),
                "metrics", List.of(
                        metric("Empresas", empresas, "Empresas registradas en la plataforma", "violet"),
                        metric("Tiendas", tiendas, "Canales y sucursales creados", "sky"),
                        metric("Usuarios", usuarios, "Usuarios operativos del ecosistema", "emerald"),
                        metric("Productos", productos, "Catálogo consolidado", "amber")),
                "highlights", List.of(
                        metric("Clientes", clientes, "Base total de clientes SaaS", "rose"),
                        metric("Órdenes", ordenes, "Órdenes de venta registradas", "slate")),
                "recordsTitle", "Empresas recientes",
                "records", records);
    }

    private Map<String, Object> buildEmpresaDashboard(AppUser appUser) {
        Usuario usuario = findUsuarioByEmail(appUser.getUsername());
        UserScope scope = resolveUserScope(usuario);
        Long empresaId = idOf(scope.empresa());

        long tiendas = countQuery("select count(t) from Tienda t where t.empresa.id = :empresaId", "empresaId", empresaId);
        long usuarios = countQuery("select count(ue) from UsuarioEmpresa ue where ue.empresa.id = :empresaId", "empresaId", empresaId);
        long productos = countQuery("select count(p) from Producto p where p.empresa.id = :empresaId", "empresaId", empresaId);
        long clientes = countQuery("select count(c) from Cliente c where c.empresa.id = :empresaId", "empresaId", empresaId);
        long ordenes = countQuery("select count(o) from OrdenVenta o where o.empresa.id = :empresaId", "empresaId", empresaId);
        long inventarios = countQuery("select count(i) from Inventario i where i.empresa.id = :empresaId", "empresaId", empresaId);

        List<Map<String, Object>> records = entityManager.createQuery("""
                        select t from Tienda t
                        where t.empresa.id = :empresaId
                        order by t.id desc
                        """, Tienda.class)
                .setParameter("empresaId", empresaId)
                .setMaxResults(6)
                .getResultList()
                .stream()
                .map(tienda -> orderedMap(
                        "title", tienda.getNombre(),
                        "subtitle", defaultText(tienda.getCorreoContacto(), "Sin correo de contacto"),
                        "status", String.valueOf(tienda.getEstado()),
                        "meta", defaultText(tienda.getRubro(), "Sin rubro definido")))
                .toList();

        return orderedMap(
                "context", orderedMap(
                        "username", appUser.getUsername(),
                        "role", appUser.getRole().name(),
                        "roleLabel", "Dueño de empresa",
                        "scopeLabel", defaultText(idName(scope.empresa()), "Empresa sin asignar"),
                        "empresaId", empresaId,
                        "empresaNombre", idName(scope.empresa()),
                        "tiendaId", idOf(scope.tienda()),
                        "tiendaNombre", idName(scope.tienda())),
                "metrics", List.of(
                        metric("Tiendas", tiendas, "Tiendas vinculadas a tu empresa", "emerald"),
                        metric("Usuarios", usuarios, "Equipo registrado", "sky"),
                        metric("Productos", productos, "Catálogo disponible", "amber"),
                        metric("Órdenes", ordenes, "Ventas registradas", "violet")),
                "highlights", List.of(
                        metric("Clientes", clientes, "Clientes asociados a la empresa", "rose"),
                        metric("Inventario", inventarios, "Registros de inventario activos", "slate")),
                "recordsTitle", "Tiendas de la empresa",
                "records", records);
    }

    private Map<String, Object> buildTiendaDashboard(AppUser appUser) {
        Usuario usuario = findUsuarioByEmail(appUser.getUsername());
        UserScope scope = resolveUserScope(usuario);
        Long empresaId = idOf(scope.empresa());
        Long tiendaId = idOf(scope.tienda());

        long productos = countQuery("select count(p) from Producto p where p.tienda.id = :tiendaId", "tiendaId", tiendaId);
        long categorias = countQuery("select count(c) from Categoria c where c.tienda.id = :tiendaId", "tiendaId", tiendaId);
        long clientes = countQuery("select count(c) from Cliente c where c.tienda.id = :tiendaId", "tiendaId", tiendaId);
        long ordenes = countQuery("select count(o) from OrdenVenta o where o.tienda.id = :tiendaId", "tiendaId", tiendaId);
        long almacenes = countQuery("select count(a) from Almacen a where a.tienda.id = :tiendaId", "tiendaId", tiendaId);
        long inventarios = countQuery("""
                select count(i)
                from Inventario i
                join i.almacen a
                where a.tienda.id = :tiendaId
                """, "tiendaId", tiendaId);

        List<Map<String, Object>> records = entityManager.createQuery("""
                        select o from OrdenVenta o
                        where o.tienda.id = :tiendaId
                        order by o.id desc
                        """, OrdenVenta.class)
                .setParameter("tiendaId", tiendaId)
                .setMaxResults(6)
                .getResultList()
                .stream()
                .map(orden -> orderedMap(
                        "title", defaultText(orden.getCodigo(), "Orden #" + orden.getId()),
                        "subtitle", "Cliente ID " + idOf(orden.getCliente()),
                        "status", String.valueOf(orden.getEstado()),
                        "meta", orden.getTotal() != null ? "Bs " + orden.getTotal() : "Monto pendiente"))
                .toList();

        return orderedMap(
                "context", orderedMap(
                        "username", appUser.getUsername(),
                        "role", appUser.getRole().name(),
                        "roleLabel", "Encargado de tienda",
                        "scopeLabel", defaultText(idName(scope.tienda()), "Tienda sin asignar"),
                        "empresaId", empresaId,
                        "empresaNombre", idName(scope.empresa()),
                        "tiendaId", tiendaId,
                        "tiendaNombre", idName(scope.tienda())),
                "metrics", List.of(
                        metric("Productos", productos, "Productos visibles en tienda", "emerald"),
                        metric("Categorías", categorias, "Categorías operativas", "sky"),
                        metric("Clientes", clientes, "Clientes atendidos por la tienda", "amber"),
                        metric("Órdenes", ordenes, "Órdenes registradas", "violet")),
                "highlights", List.of(
                        metric("Almacenes", almacenes, "Almacenes vinculados a la tienda", "rose"),
                        metric("Inventario", inventarios, "Registros de stock disponibles", "slate")),
                "recordsTitle", "Órdenes recientes",
                "records", records);
    }

    private Map<String, Object> buildFallbackDashboard(AppUser appUser) {
        return orderedMap(
                "context", orderedMap(
                        "username", appUser.getUsername(),
                        "role", appUser.getRole().name(),
                        "roleLabel", formatRoleLabel(appUser.getRole()),
                        "scopeLabel", "Sin tablero especializado"),
                "metrics", List.of(
                        metric("Acceso", 1, "La sesión está activa", "slate")),
                "highlights", List.of(),
                "recordsTitle", "Sin datos",
                "records", List.of());
    }

    private Usuario findUsuarioByEmail(String username) {
        List<Usuario> matches = entityManager.createQuery("""
                        select u from Usuario u
                        where lower(u.email) = :email
                        """, Usuario.class)
                .setParameter("email", username.toLowerCase(Locale.ROOT))
                .setMaxResults(1)
                .getResultList();

        if (matches.isEmpty()) {
            throw new ResourceNotFoundException("No se encontró un usuario SaaS asociado a " + username);
        }

        return matches.get(0);
    }

    private Map<String, Object> metric(String title, long value, String description, String tone) {
        return metric(title, String.valueOf(value), description, tone);
    }

    private Map<String, Object> metric(String title, String value, String description, String tone) {
        return orderedMap(
                "title", title,
                "value", value,
                "description", description,
                "tone", tone);
    }

    private long countQuery(String jpql) {
        Long result = entityManager.createQuery(jpql, Long.class).getSingleResult();
        return result != null ? result : 0L;
    }

    private long countQuery(String jpql, String parameter, Object value) {
        Long result = entityManager.createQuery(jpql, Long.class)
                .setParameter(parameter, value)
                .getSingleResult();
        return result != null ? result : 0L;
    }

    private String defaultText(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private String idName(Object entity) {
        if (entity == null) {
            return null;
        }

        return switch (entity) {
            case Empresa empresa -> empresa.getNombreComercial();
            case Tienda tienda -> tienda.getNombre();
            case PlanSuscripcion plan -> plan.getNombre();
            default -> null;
        };
    }

    private String formatRoleLabel(AppRole role) {
        return switch (role) {
            case ROLE_SUPERADMIN -> "Superadmin";
            case ROLE_ADMIN_TIENDA, ROLE_ADMIN_TIENDA_LIMITADO -> "Dueño de empresa";
            case ROLE_ENCARGADO_TIENDA -> "Encargado de tienda";
            default -> role.name().replace("ROLE_", "").replace('_', ' ');
        };
    }

    public Map<String, Object> createPlan(SaasRequests.PlanRequest request) {
        log.info("[SaaS] createPlan request nombre={}, precioMensual={}, limiteTiendas={}, limiteAlmacenes={}, limiteUsuarios={}, limiteProductos={}, incluyeCrm={}, incluyeIa={}, soporte={}, estado={}",
                request.getNombre(),
                request.getPrecioMensual(),
                request.getLimiteTiendas(),
                request.getLimiteAlmacenes(),
                request.getLimiteUsuarios(),
                request.getLimiteProductos(),
                request.getIncluyeCrm(),
                request.getIncluyeIa(),
                request.getSoporte(),
                request.getEstado());
        PlanSuscripcion plan = new PlanSuscripcion();
        applyPlanRequest(plan, request);
        touchCreate(plan);
        entityManager.persist(plan);
        entityManager.flush();
        logEvent(null, null, null, "planes", "crear", "PlanSuscripcion", plan.getId(), "Plan de suscripcion creado");
        entityManager.flush();
        log.info("[SaaS] createPlan success id={}", plan.getId());
        return toPlanMap(plan);
    }

    public Map<String, Object> updatePlan(Long planId, SaasRequests.PlanRequest request) {
        log.info("[SaaS] updatePlan request planId={}, nombre={}, precioMensual={}, estado={}",
                planId,
                request.getNombre(),
                request.getPrecioMensual(),
                request.getEstado());
        PlanSuscripcion plan = findRequired(planId, PlanSuscripcion.class);
        applyPlanRequest(plan, request);
        touchUpdate(plan);
        entityManager.flush();
        logEvent(null, null, null, "planes", "actualizar", "PlanSuscripcion", plan.getId(), "Plan de suscripcion actualizado");
        entityManager.flush();
        log.info("[SaaS] updatePlan success id={}", plan.getId());
        return toPlanMap(plan);
    }

    public List<Map<String, Object>> listSystemUsers(String role) {
        return appUserRepository.findAll()
                .stream()
                .filter(user -> role == null || user.getRole().name().equalsIgnoreCase(role))
                .sorted((left, right) -> Long.compare(
                        right.getId() != null ? right.getId() : 0L,
                        left.getId() != null ? left.getId() : 0L))
                .map(this::toSystemUserMap)
                .toList();
    }

    public Map<String, Object> createSystemUser(SaasRequests.SystemUserRequest request) {
        AppUser user = new AppUser();
        user.setUsername(request.getUsername().trim());
        user.setRole(request.getRole());
        user.setActivo(Boolean.TRUE);
        user.setPassword(passwordEncoder.encode(requirePassword(request.getPassword())));
        touchCreate(user);
        appUserRepository.save(user);
        return toSystemUserMap(user);
    }

    public Map<String, Object> updateSystemUser(Long userId, SaasRequests.SystemUserRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario del sistema no encontrado: " + userId));
        user.setUsername(request.getUsername().trim());
        user.setRole(request.getRole());
        user.setActivo(request.getActivo() == null ? Boolean.TRUE : request.getActivo());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        touchUpdate(user);
        appUserRepository.save(user);
        return toSystemUserMap(user);
    }

    public List<Map<String, Object>> listEmpresas() {
        return entityManager.createQuery("select e from Empresa e order by e.id", Empresa.class)
                .getResultList()
                .stream()
                .map(this::toEmpresaMap)
                .toList();
    }

    public Map<String, Object> createEmpresa(SaasRequests.EmpresaRequest request) {
        Empresa empresa = new Empresa();
        empresa.setNombreComercial(request.getNombreComercial());
        empresa.setRazonSocial(request.getRazonSocial());
        empresa.setNit(request.getNit());
        empresa.setCorreo(request.getCorreo());
        empresa.setTelefono(request.getTelefono());
        empresa.setDireccion(request.getDireccion());
        empresa.setPlanActual(findOptional(request.getPlanActualId(), PlanSuscripcion.class));
        empresa.setUsuarioRegistro(findOptional(request.getUsuarioRegistroId(), Usuario.class));
        touchCreate(empresa);
        entityManager.persist(empresa);
        logEvent(empresa, null, empresa.getUsuarioRegistro(), "empresas", "crear", "Empresa", empresa.getId(), "Empresa registrada");
        return toEmpresaMap(empresa);
    }

    public List<Map<String, Object>> listSuscripciones() {
        return entityManager.createQuery("select s from Suscripcion s order by s.id desc", Suscripcion.class)
                .getResultList()
                .stream()
                .map(this::toSuscripcionMap)
                .toList();
    }

    public Map<String, Object> createSuscripcion(SaasRequests.SuscripcionRequest request) {
        Empresa empresa = findRequired(request.getEmpresaId(), Empresa.class);
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setEmpresa(empresa);
        suscripcion.setPlanSuscripcion(findRequired(request.getPlanSuscripcionId(), PlanSuscripcion.class));
        suscripcion.setUsuarioRegistro(findOptional(request.getUsuarioRegistroId(), Usuario.class));
        suscripcion.setFechaInicio(request.getFechaInicio());
        suscripcion.setFechaFin(request.getFechaFin());
        suscripcion.setMontoPagado(zeroIfNull(request.getMontoPagado()));
        suscripcion.setRenovacionAutomatica(Boolean.TRUE.equals(request.getRenovacionAutomatica()));
        touchCreate(suscripcion);
        entityManager.persist(suscripcion);
        empresa.setPlanActual(suscripcion.getPlanSuscripcion());
        touchUpdate(empresa);
        logEvent(empresa, null, suscripcion.getUsuarioRegistro(), "suscripciones", "crear", "Suscripcion", suscripcion.getId(), "Suscripcion registrada");
        return toSuscripcionMap(suscripcion);
    }

    public List<Map<String, Object>> listTiendas(Long empresaId) {
        return queryByCompany("select t from Tienda t where (:empresaId is null or t.empresa.id = :empresaId) order by t.id", Tienda.class, empresaId)
                .stream()
                .map(this::toTiendaMap)
                .toList();
    }

    public Map<String, Object> createTienda(SaasRequests.TiendaRequest request) {
        Empresa empresa = findRequired(request.getEmpresaId(), Empresa.class);
        Tienda tienda = new Tienda();
        tienda.setEmpresa(empresa);
        tienda.setUsuarioRegistro(findOptional(request.getUsuarioRegistroId(), Usuario.class));
        tienda.setNombre(request.getNombre());
        tienda.setSlug(request.getSlug());
        tienda.setRubro(request.getRubro());
        tienda.setDescripcion(request.getDescripcion());
        tienda.setCorreoContacto(request.getCorreoContacto());
        tienda.setTelefono(request.getTelefono());
        touchCreate(tienda);
        entityManager.persist(tienda);
        logEvent(empresa, tienda, tienda.getUsuarioRegistro(), "tiendas", "crear", "Tienda", tienda.getId(), "Tienda registrada");
        return toTiendaMap(tienda);
    }

    public List<Map<String, Object>> listAlmacenes(Long empresaId, Long tiendaId) {
        return entityManager.createQuery("""
                        select a from Almacen a
                        where (:empresaId is null or a.empresa.id = :empresaId)
                          and (:tiendaId is null or a.tienda.id = :tiendaId)
                        order by a.id
                        """, Almacen.class)
                .setParameter("empresaId", empresaId)
                .setParameter("tiendaId", tiendaId)
                .getResultList()
                .stream()
                .map(this::toAlmacenMap)
                .toList();
    }

    public Map<String, Object> createAlmacen(SaasRequests.AlmacenRequest request) {
        Empresa empresa = findRequired(request.getEmpresaId(), Empresa.class);
        Almacen almacen = new Almacen();
        almacen.setEmpresa(empresa);
        almacen.setTienda(findRequired(request.getTiendaId(), Tienda.class));
        almacen.setUsuarioRegistro(findOptional(request.getUsuarioRegistroId(), Usuario.class));
        almacen.setNombre(request.getNombre());
        almacen.setDireccion(request.getDireccion());
        almacen.setReferencia(request.getReferencia());
        touchCreate(almacen);
        entityManager.persist(almacen);
        logEvent(empresa, almacen.getTienda(), almacen.getUsuarioRegistro(), "almacenes", "crear", "Almacen", almacen.getId(), "Almacen registrado");
        return toAlmacenMap(almacen);
    }

    public List<Map<String, Object>> listCategorias(Long empresaId, Long tiendaId) {
        return entityManager.createQuery("""
                        select c from Categoria c
                        where (:empresaId is null or c.empresa.id = :empresaId)
                          and (:tiendaId is null or c.tienda.id = :tiendaId)
                        order by c.id
                        """, Categoria.class)
                .setParameter("empresaId", empresaId)
                .setParameter("tiendaId", tiendaId)
                .getResultList()
                .stream()
                .map(this::toCategoriaMap)
                .toList();
    }

    public Map<String, Object> createCategoria(SaasRequests.CategoriaRequest request) {
        Empresa empresa = findRequired(request.getEmpresaId(), Empresa.class);
        Categoria categoria = new Categoria();
        categoria.setEmpresa(empresa);
        categoria.setTienda(findRequired(request.getTiendaId(), Tienda.class));
        categoria.setCategoriaPadre(findOptional(request.getCategoriaPadreId(), Categoria.class));
        categoria.setUsuarioRegistro(findOptional(request.getUsuarioRegistroId(), Usuario.class));
        categoria.setNombre(request.getNombre());
        categoria.setSlug(request.getSlug());
        categoria.setDescripcion(request.getDescripcion());
        touchCreate(categoria);
        entityManager.persist(categoria);
        logEvent(empresa, categoria.getTienda(), categoria.getUsuarioRegistro(), "categorias", "crear", "Categoria", categoria.getId(), "Categoria registrada");
        return toCategoriaMap(categoria);
    }

    public List<Map<String, Object>> listUsuarios(Long empresaId) {
        List<Usuario> usuarios;
        if (empresaId == null) {
            usuarios = entityManager.createQuery("select u from Usuario u order by u.id", Usuario.class)
                    .getResultList();
        } else {
            usuarios = entityManager.createQuery("""
                            select distinct ue.usuario from UsuarioEmpresa ue
                            join ue.usuario u
                            where ue.empresa.id = :empresaId
                            order by u.id
                            """, Usuario.class)
                    .setParameter("empresaId", empresaId)
                    .getResultList();
        }
        return usuarios.stream()
                .map(this::toUsuarioMap)
                .toList();
    }

    public Map<String, Object> createUsuario(SaasRequests.UsuarioRequest request) {
        Usuario usuario = new Usuario();
        Empresa empresa = findOptional(request.getEmpresaId(), Empresa.class);
        Tienda tienda = findOptional(request.getTiendaId(), Tienda.class);
        if (empresa == null && tienda != null) {
            empresa = tienda.getEmpresa();
        }
        usuario.setFotoArchivo(findOptional(request.getFotoArchivoId(), Archivo.class));
        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setEmail(request.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setTelefono(request.getTelefono());
        usuario.setEstado(com.codesfree.prueba.model.saas.EstadoGeneral.activo);
        touchCreate(usuario);
        entityManager.persist(usuario);
        createUsuarioEmpresaIfNeeded(usuario, empresa);
        createUsuarioTiendaIfNeeded(usuario, empresa, tienda);
        logEvent(empresa, tienda, usuario, "usuarios", "crear", "Usuario", usuario.getId(), "Usuario registrado");
        return toUsuarioMap(usuario);
    }

    public List<Map<String, Object>> listRoles(Long empresaId) {
        return queryByCompany("select r from Rol r where (:empresaId is null or r.empresa.id = :empresaId) order by r.id", Rol.class, empresaId)
                .stream()
                .map(this::toRolMap)
                .toList();
    }

    public Map<String, Object> createRol(SaasRequests.RolRequest request) {
        Rol rol = new Rol();
        rol.setEmpresa(findOptional(request.getEmpresaId(), Empresa.class));
        rol.setCodigo(request.getCodigo());
        rol.setNombre(request.getNombre());
        rol.setDescripcion(request.getDescripcion());
        rol.setTipo(resolveTipoRol(request));
        rol.setEsSistema(Boolean.TRUE.equals(request.getEsSistema()));
        touchCreate(rol);
        entityManager.persist(rol);
        logEvent(rol.getEmpresa(), null, null, "roles", "crear", "Rol", rol.getId(), "Rol registrado");
        return toRolMap(rol);
    }

    public List<Map<String, Object>> listPermisos() {
        return entityManager.createQuery("select p from Permiso p order by p.id", Permiso.class)
                .getResultList()
                .stream()
                .map(this::toPermisoMap)
                .toList();
    }

    public Map<String, Object> createPermiso(SaasRequests.PermisoRequest request) {
        Permiso permiso = new Permiso();
        permiso.setCodigo(request.getCodigo());
        permiso.setModulo(request.getModulo());
        permiso.setAccion(request.getAccion());
        permiso.setDescripcion(request.getDescripcion());
        entityManager.persist(permiso);
        logEvent(null, null, null, "permisos", "crear", "Permiso", permiso.getId(), "Permiso registrado");
        return toPermisoMap(permiso);
    }

    public Map<String, Object> assignPermissionToRole(Long rolId, Long permisoId) {
        RolPermiso relation = new RolPermiso();
        relation.setRol(findRequired(rolId, Rol.class));
        relation.setPermiso(findRequired(permisoId, Permiso.class));
        entityManager.persist(relation);
        logEvent(relation.getRol().getEmpresa(), null, null, "roles", "asignar_permiso", "RolPermiso", relation.getId(), "Permiso asignado a rol");
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", relation.getId());
        response.put("rolId", relation.getRol().getId());
        response.put("permisoId", relation.getPermiso().getId());
        return response;
    }

    public Map<String, Object> assignRoleToUser(Long usuarioId, SaasRequests.UsuarioRolRequest request) {
        UsuarioRol relation = new UsuarioRol();
        Usuario usuario = findRequired(usuarioId, Usuario.class);
        Rol rol = findRequired(request.getRolId(), Rol.class);
        Tienda tienda = findOptional(request.getTiendaId(), Tienda.class);
        Empresa empresa = findOptional(request.getEmpresaId(), Empresa.class);
        if (empresa == null && tienda != null) {
            empresa = tienda.getEmpresa();
        }
        if (empresa == null) {
            empresa = rol.getEmpresa();
        }
        relation.setUsuario(usuario);
        relation.setRol(rol);
        relation.setEmpresa(empresa);
        relation.setTienda(tienda);
        relation.setCreatedAt(Instant.now());
        entityManager.persist(relation);
        createUsuarioEmpresaIfNeeded(usuario, empresa);
        createUsuarioTiendaIfNeeded(usuario, empresa, tienda);
        logEvent(empresa, tienda, relation.getUsuario(), "usuarios", "asignar_rol", "UsuarioRol", relation.getId(), "Rol asignado a usuario");
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", relation.getId());
        response.put("usuarioId", relation.getUsuario().getId());
        response.put("rolId", relation.getRol().getId());
        response.put("empresaId", relation.getEmpresa() != null ? relation.getEmpresa().getId() : null);
        response.put("tiendaId", relation.getTienda() != null ? relation.getTienda().getId() : null);
        return response;
    }

    public List<Map<String, Object>> listArchivos(Long empresaId) {
        return queryByCompany("select a from Archivo a where (:empresaId is null or a.empresa.id = :empresaId) order by a.id desc", Archivo.class, empresaId)
                .stream()
                .map(this::toArchivoMap)
                .toList();
    }

    public Map<String, Object> createArchivo(SaasRequests.ArchivoRequest request) {
        Archivo archivo = new Archivo();
        archivo.setEmpresa(findOptional(request.getEmpresaId(), Empresa.class));
        archivo.setUsuarioSubida(findOptional(request.getUsuarioSubidaId(), Usuario.class));
        archivo.setNombreOriginal(request.getNombreOriginal());
        archivo.setRutaArchivo(request.getRutaArchivo());
        archivo.setUrlPublica(request.getUrlPublica());
        archivo.setTipoArchivo(request.getTipoArchivo());
        archivo.setExtension(request.getExtension());
        archivo.setTamanioBytes(request.getTamanioBytes());
        archivo.setCreatedAt(Instant.now());
        entityManager.persist(archivo);
        logEvent(archivo.getEmpresa(), null, archivo.getUsuarioSubida(), "archivos", "crear", "Archivo", archivo.getId(), "Archivo registrado");
        return toArchivoMap(archivo);
    }

    public List<Map<String, Object>> listProveedores(Long empresaId) {
        return queryByCompany("select p from Proveedor p where (:empresaId is null or p.empresa.id = :empresaId) order by p.id", Proveedor.class, empresaId)
                .stream()
                .map(this::toProveedorMap)
                .toList();
    }

    public Map<String, Object> createProveedor(SaasRequests.ProveedorRequest request) {
        Empresa empresa = findRequired(request.getEmpresaId(), Empresa.class);
        Proveedor proveedor = new Proveedor();
        proveedor.setEmpresa(empresa);
        proveedor.setUsuarioRegistro(findOptional(request.getUsuarioRegistroId(), Usuario.class));
        proveedor.setNombre(request.getNombre());
        proveedor.setNit(request.getNit());
        proveedor.setCorreo(request.getCorreo());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setContactoPrincipal(request.getContactoPrincipal());
        touchCreate(proveedor);
        entityManager.persist(proveedor);
        logEvent(empresa, null, proveedor.getUsuarioRegistro(), "proveedores", "crear", "Proveedor", proveedor.getId(), "Proveedor registrado");
        return toProveedorMap(proveedor);
    }

    public List<Map<String, Object>> listProductos(Long empresaId, Long tiendaId) {
        return entityManager.createQuery("""
                        select p from Producto p
                        where (:empresaId is null or p.empresa.id = :empresaId)
                          and (:tiendaId is null or p.tienda.id = :tiendaId)
                        order by p.id
                        """, Producto.class)
                .setParameter("empresaId", empresaId)
                .setParameter("tiendaId", tiendaId)
                .getResultList()
                .stream()
                .map(this::toProductoMap)
                .toList();
    }

    public Map<String, Object> createProducto(SaasRequests.ProductoRequest request) {
        Empresa empresa = findRequired(request.getEmpresaId(), Empresa.class);
        Producto producto = new Producto();
        producto.setEmpresa(empresa);
        producto.setTienda(findRequired(request.getTiendaId(), Tienda.class));
        producto.setCategoria(findOptional(request.getCategoriaId(), Categoria.class));
        producto.setProveedorPrincipal(findOptional(request.getProveedorPrincipalId(), Proveedor.class));
        producto.setUsuarioRegistro(findOptional(request.getUsuarioRegistroId(), Usuario.class));
        producto.setNombre(request.getNombre());
        producto.setSlug(request.getSlug());
        producto.setSku(request.getSku());
        producto.setDescripcion(request.getDescripcion());
        producto.setMarca(request.getMarca());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setCostoReferencial(request.getCostoReferencial());
        producto.setUnidadMedida(request.getUnidadMedida() == null || request.getUnidadMedida().isBlank() ? "unidad" : request.getUnidadMedida());
        touchCreate(producto);
        entityManager.persist(producto);
        logEvent(empresa, producto.getTienda(), producto.getUsuarioRegistro(), "productos", "crear", "Producto", producto.getId(), "Producto registrado");
        return toProductoMap(producto);
    }

    public List<Map<String, Object>> listClientes(Long empresaId, Long tiendaId) {
        return entityManager.createQuery("""
                        select c from Cliente c
                        where (:empresaId is null or c.empresa.id = :empresaId)
                          and (:tiendaId is null or c.tienda.id = :tiendaId)
                        order by c.id
                        """, Cliente.class)
                .setParameter("empresaId", empresaId)
                .setParameter("tiendaId", tiendaId)
                .getResultList()
                .stream()
                .map(this::toClienteMap)
                .toList();
    }

    public Map<String, Object> createCliente(SaasRequests.ClienteRequest request) {
        Empresa empresa = findRequired(request.getEmpresaId(), Empresa.class);
        Cliente cliente = new Cliente();
        cliente.setEmpresa(empresa);
        cliente.setTienda(findRequired(request.getTiendaId(), Tienda.class));
        cliente.setNombres(request.getNombres());
        cliente.setApellidos(request.getApellidos());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDocumentoIdentidad(request.getDocumentoIdentidad());
        cliente.setDireccion(request.getDireccion());
        touchCreate(cliente);
        entityManager.persist(cliente);
        logEvent(empresa, cliente.getTienda(), null, "clientes", "crear", "Cliente", cliente.getId(), "Cliente registrado");
        return toClienteMap(cliente);
    }

    public List<Map<String, Object>> listInventario(Long empresaId, Long almacenId) {
        return entityManager.createQuery("""
                        select i from Inventario i
                        where (:empresaId is null or i.empresa.id = :empresaId)
                          and (:almacenId is null or i.almacen.id = :almacenId)
                        order by i.id
                        """, Inventario.class)
                .setParameter("empresaId", empresaId)
                .setParameter("almacenId", almacenId)
                .getResultList()
                .stream()
                .map(this::toInventarioMap)
                .toList();
    }

    public Map<String, Object> saveInventario(SaasRequests.InventarioRequest request) {
        Empresa empresa = findRequired(request.getEmpresaId(), Empresa.class);
        Almacen almacen = findRequired(request.getAlmacenId(), Almacen.class);
        Producto producto = findRequired(request.getProductoId(), Producto.class);
        Inventario inventario = findInventario(almacen.getId(), producto.getId());
        if (inventario == null) {
            inventario = new Inventario();
            inventario.setEmpresa(empresa);
            inventario.setAlmacen(almacen);
            inventario.setProducto(producto);
            entityManager.persist(inventario);
        }
        inventario.setCantidadDisponible(request.getCantidadDisponible());
        inventario.setCantidadReservada(defaultInt(request.getCantidadReservada()));
        inventario.setStockMinimo(defaultInt(request.getStockMinimo()));
        inventario.setStockMaximo(request.getStockMaximo());
        inventario.setUpdatedAt(Instant.now());
        logEvent(empresa, almacen.getTienda(), null, "inventarios", "actualizar", "Inventario", inventario.getId(), "Inventario actualizado");
        return toInventarioMap(inventario);
    }

    public List<Map<String, Object>> listCatalogo(Long tiendaId, Long categoriaId) {
        return entityManager.createQuery("""
                        select p from Producto p
                        where p.tienda.id = :tiendaId
                          and (:categoriaId is null or p.categoria.id = :categoriaId)
                        order by p.nombre
                        """, Producto.class)
                .setParameter("tiendaId", tiendaId)
                .setParameter("categoriaId", categoriaId)
                .getResultList()
                .stream()
                .map(this::toProductoMap)
                .toList();
    }

    public List<Map<String, Object>> listOrdenesVenta(Long empresaId) {
        return queryByCompany("select o from OrdenVenta o where (:empresaId is null or o.empresa.id = :empresaId) order by o.id desc", OrdenVenta.class, empresaId)
                .stream()
                .map(this::toOrdenVentaMap)
                .toList();
    }

    public Map<String, Object> createOrdenVenta(SaasRequests.OrdenVentaRequest request) {
        Empresa empresa = findRequired(request.getEmpresaId(), Empresa.class);
        Tienda tienda = findRequired(request.getTiendaId(), Tienda.class);
        Almacen almacen = findRequired(request.getAlmacenId(), Almacen.class);
        Cliente cliente = findRequired(request.getClienteId(), Cliente.class);
        Usuario usuario = findOptional(request.getUsuarioId(), Usuario.class);

        OrdenVenta ordenVenta = new OrdenVenta();
        ordenVenta.setEmpresa(empresa);
        ordenVenta.setTienda(tienda);
        ordenVenta.setCliente(cliente);
        ordenVenta.setCodigo(request.getCodigo());
        ordenVenta.setObservaciones(request.getObservaciones());
        ordenVenta.setCreatedAt(Instant.now());
        entityManager.persist(ordenVenta);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (SaasRequests.OrdenVentaItemRequest itemRequest : request.getItems()) {
            Producto producto = findRequired(itemRequest.getProductoId(), Producto.class);
            Inventario inventario = requireInventario(almacen.getId(), producto.getId());
            if (inventario.getCantidadDisponible() < itemRequest.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para producto " + producto.getId());
            }

            OrdenVentaDetalle detalle = new OrdenVentaDetalle();
            detalle.setOrdenVenta(ordenVenta);
            detalle.setProducto(producto);
            detalle.setCantidad(itemRequest.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setDescuento(zeroIfNull(itemRequest.getDescuento()));
            detalle.setSubtotal(producto.getPrecioVenta()
                    .multiply(BigDecimal.valueOf(itemRequest.getCantidad()))
                    .subtract(detalle.getDescuento()));
            entityManager.persist(detalle);
            subtotal = subtotal.add(detalle.getSubtotal());

            inventario.setCantidadDisponible(inventario.getCantidadDisponible() - itemRequest.getCantidad());
            inventario.setUpdatedAt(Instant.now());

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setEmpresa(empresa);
            movimiento.setAlmacen(almacen);
            movimiento.setProducto(producto);
            movimiento.setOrdenVentaDetalle(detalle);
            movimiento.setTipo(TipoMovimientoInventario.salida_venta);
            movimiento.setCantidad(itemRequest.getCantidad());
            movimiento.setCantidadResultante(inventario.getCantidadDisponible());
            movimiento.setMotivo("Salida por orden de venta " + ordenVenta.getCodigo());
            movimiento.setUsuario(usuario);
            movimiento.setCreatedAt(Instant.now());
            entityManager.persist(movimiento);
        }

        ordenVenta.setSubtotal(subtotal);
        ordenVenta.setTotal(subtotal.subtract(ordenVenta.getDescuentoTotal()));

        OrdenVentaEstado estado = new OrdenVentaEstado();
        estado.setOrdenVenta(ordenVenta);
        estado.setUsuario(usuario);
        estado.setEstadoNuevo(ordenVenta.getEstado());
        estado.setObservacion("Orden creada");
        estado.setCreatedAt(Instant.now());
        entityManager.persist(estado);

        logEvent(empresa, tienda, usuario, "ordenes_venta", "crear", "OrdenVenta", ordenVenta.getId(), "Orden de venta registrada");
        return toOrdenVentaMap(ordenVenta);
    }

    public Map<String, Object> changeOrdenVentaEstado(Long ordenVentaId, SaasRequests.CambioEstadoOrdenVentaRequest request) {
        OrdenVenta ordenVenta = findRequired(ordenVentaId, OrdenVenta.class);
        EstadoOrdenVenta anterior = ordenVenta.getEstado();
        ordenVenta.setEstado(request.getEstado());
        if (request.getEstado() == EstadoOrdenVenta.confirmado) {
            ordenVenta.setConfirmadoAt(Instant.now());
        } else if (request.getEstado() == EstadoOrdenVenta.cancelado) {
            ordenVenta.setCanceladoAt(Instant.now());
        } else if (request.getEstado() == EstadoOrdenVenta.entregado) {
            ordenVenta.setEntregadoAt(Instant.now());
        }

        OrdenVentaEstado estado = new OrdenVentaEstado();
        estado.setOrdenVenta(ordenVenta);
        estado.setUsuario(findOptional(request.getUsuarioId(), Usuario.class));
        estado.setEstadoAnterior(anterior);
        estado.setEstadoNuevo(request.getEstado());
        estado.setObservacion(request.getObservacion());
        estado.setCreatedAt(Instant.now());
        entityManager.persist(estado);

        logEvent(ordenVenta.getEmpresa(), ordenVenta.getTienda(), estado.getUsuario(), "ordenes_venta", "cambiar_estado", "OrdenVenta", ordenVenta.getId(), "Estado actualizado");
        return toOrdenVentaMap(ordenVenta);
    }

    public List<Map<String, Object>> listOrdenesCompra(Long empresaId) {
        return queryByCompany("select o from OrdenCompra o where (:empresaId is null or o.empresa.id = :empresaId) order by o.id desc", OrdenCompra.class, empresaId)
                .stream()
                .map(this::toOrdenCompraMap)
                .toList();
    }

    public Map<String, Object> createOrdenCompra(SaasRequests.OrdenCompraRequest request) {
        Empresa empresa = findRequired(request.getEmpresaId(), Empresa.class);
        OrdenCompra ordenCompra = new OrdenCompra();
        ordenCompra.setEmpresa(empresa);
        ordenCompra.setProveedor(findRequired(request.getProveedorId(), Proveedor.class));
        ordenCompra.setAlmacen(findRequired(request.getAlmacenId(), Almacen.class));
        ordenCompra.setUsuarioCreacion(findOptional(request.getUsuarioCreacionId(), Usuario.class));
        ordenCompra.setCodigo(request.getCodigo());
        ordenCompra.setFechaEmision(request.getFechaEmision());
        ordenCompra.setFechaEstimadaRecepcion(request.getFechaEstimadaRecepcion());
        ordenCompra.setEstado(request.getEstado() == null ? EstadoOrdenCompra.emitida : request.getEstado());
        ordenCompra.setObservaciones(request.getObservaciones());
        touchCreate(ordenCompra);
        entityManager.persist(ordenCompra);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (SaasRequests.OrdenCompraItemRequest itemRequest : request.getItems()) {
            Producto producto = findRequired(itemRequest.getProductoId(), Producto.class);
            OrdenCompraDetalle detalle = new OrdenCompraDetalle();
            detalle.setOrdenCompra(ordenCompra);
            detalle.setProducto(producto);
            detalle.setCantidadSolicitada(itemRequest.getCantidadSolicitada());
            detalle.setCostoUnitario(itemRequest.getCostoUnitario());
            detalle.setSubtotal(itemRequest.getCostoUnitario().multiply(BigDecimal.valueOf(itemRequest.getCantidadSolicitada())));
            entityManager.persist(detalle);
            subtotal = subtotal.add(detalle.getSubtotal());
        }

        ordenCompra.setSubtotal(subtotal);
        ordenCompra.setTotal(subtotal);
        logEvent(empresa, ordenCompra.getAlmacen().getTienda(), ordenCompra.getUsuarioCreacion(), "ordenes_compra", "crear", "OrdenCompra", ordenCompra.getId(), "Orden de compra registrada");
        return toOrdenCompraMap(ordenCompra);
    }

    public Map<String, Object> registerRecepcionCompra(SaasRequests.RecepcionCompraRequest request) {
        Empresa empresa = findRequired(request.getEmpresaId(), Empresa.class);
        OrdenCompra ordenCompra = findRequired(request.getOrdenCompraId(), OrdenCompra.class);
        Almacen almacen = findRequired(request.getAlmacenId(), Almacen.class);
        Usuario usuario = findOptional(request.getUsuarioRecepcionId(), Usuario.class);

        RecepcionCompra recepcion = new RecepcionCompra();
        recepcion.setEmpresa(empresa);
        recepcion.setOrdenCompra(ordenCompra);
        recepcion.setAlmacen(almacen);
        recepcion.setUsuarioRecepcion(usuario);
        recepcion.setCodigo(request.getCodigo());
        recepcion.setFechaRecepcion(request.getFechaRecepcion() == null ? Instant.now() : request.getFechaRecepcion());
        recepcion.setObservaciones(request.getObservaciones());
        recepcion.setCreatedAt(Instant.now());
        entityManager.persist(recepcion);

        for (SaasRequests.RecepcionCompraDetalleRequest detalleRequest : request.getDetalles()) {
            Producto producto = findRequired(detalleRequest.getProductoId(), Producto.class);
            OrdenCompraDetalle ordenDetalle = detalleRequest.getOrdenCompraDetalleId() == null
                    ? null
                    : findRequired(detalleRequest.getOrdenCompraDetalleId(), OrdenCompraDetalle.class);

            RecepcionCompraDetalle detalle = new RecepcionCompraDetalle();
            detalle.setRecepcionCompra(recepcion);
            detalle.setOrdenCompraDetalle(ordenDetalle);
            detalle.setProducto(producto);
            detalle.setCantidadRecibida(detalleRequest.getCantidadRecibida());
            detalle.setCostoUnitario(detalleRequest.getCostoUnitario());
            entityManager.persist(detalle);

            if (ordenDetalle != null) {
                ordenDetalle.setCantidadRecibida(ordenDetalle.getCantidadRecibida() + detalleRequest.getCantidadRecibida());
            }

            Inventario inventario = findInventario(almacen.getId(), producto.getId());
            if (inventario == null) {
                inventario = new Inventario();
                inventario.setEmpresa(empresa);
                inventario.setAlmacen(almacen);
                inventario.setProducto(producto);
                inventario.setCantidadDisponible(0);
                inventario.setCantidadReservada(0);
                inventario.setStockMinimo(0);
                entityManager.persist(inventario);
            }
            inventario.setCantidadDisponible(inventario.getCantidadDisponible() + detalleRequest.getCantidadRecibida());
            inventario.setUpdatedAt(Instant.now());

            Lote lote = findLoteByCodigo(empresa.getId(), detalleRequest.getCodigoLote());
            if (lote == null) {
                lote = new Lote();
                lote.setEmpresa(empresa);
                lote.setProveedor(ordenCompra.getProveedor());
                lote.setAlmacen(almacen);
                lote.setOrdenCompra(ordenCompra);
                lote.setRecepcionCompra(recepcion);
                lote.setCodigoLote(detalleRequest.getCodigoLote());
                lote.setFechaIngreso(LocalDate.now());
                lote.setFechaVencimiento(detalleRequest.getFechaVencimiento());
                lote.setCreatedAt(Instant.now());
                entityManager.persist(lote);
            }

            LoteDetalle loteDetalle = new LoteDetalle();
            loteDetalle.setLote(lote);
            loteDetalle.setProducto(producto);
            loteDetalle.setRecepcionCompraDetalle(detalle);
            loteDetalle.setCantidadInicial(detalleRequest.getCantidadRecibida());
            loteDetalle.setCantidadActual(detalleRequest.getCantidadRecibida());
            loteDetalle.setCostoUnitario(detalleRequest.getCostoUnitario());
            loteDetalle.setCreatedAt(Instant.now());
            entityManager.persist(loteDetalle);

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setEmpresa(empresa);
            movimiento.setAlmacen(almacen);
            movimiento.setProducto(producto);
            movimiento.setLoteDetalle(loteDetalle);
            movimiento.setRecepcionCompraDetalle(detalle);
            movimiento.setTipo(TipoMovimientoInventario.entrada_compra);
            movimiento.setCantidad(detalleRequest.getCantidadRecibida());
            movimiento.setCantidadResultante(inventario.getCantidadDisponible());
            movimiento.setMotivo("Entrada por recepcion " + recepcion.getCodigo());
            movimiento.setUsuario(usuario);
            movimiento.setCreatedAt(Instant.now());
            entityManager.persist(movimiento);
        }

        updateEstadoOrdenCompra(ordenCompra);
        logEvent(empresa, almacen.getTienda(), usuario, "recepciones_compra", "crear", "RecepcionCompra", recepcion.getId(), "Recepcion de compra registrada");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("recepcion", toRecepcionCompraMap(recepcion));
        response.put("ordenCompra", toOrdenCompraMap(ordenCompra));
        return response;
    }

    public List<Map<String, Object>> listMovimientos(Long empresaId, Long almacenId) {
        return entityManager.createQuery("""
                        select m from MovimientoInventario m
                        where (:empresaId is null or m.empresa.id = :empresaId)
                          and (:almacenId is null or m.almacen.id = :almacenId)
                        order by m.id desc
                        """, MovimientoInventario.class)
                .setParameter("empresaId", empresaId)
                .setParameter("almacenId", almacenId)
                .getResultList()
                .stream()
                .map(this::toMovimientoMap)
                .toList();
    }

    public List<Map<String, Object>> listLotes(Long empresaId, Long almacenId) {
        return entityManager.createQuery("""
                        select l from Lote l
                        where (:empresaId is null or l.empresa.id = :empresaId)
                          and (:almacenId is null or l.almacen.id = :almacenId)
                        order by l.id desc
                        """, Lote.class)
                .setParameter("empresaId", empresaId)
                .setParameter("almacenId", almacenId)
                .getResultList()
                .stream()
                .map(this::toLoteMap)
                .toList();
    }

    public List<Map<String, Object>> listBitacora(Long empresaId) {
        return queryByCompany("select b from BitacoraEvento b where (:empresaId is null or b.empresa.id = :empresaId) order by b.id desc", BitacoraEvento.class, empresaId)
                .stream()
                .map(this::toBitacoraMap)
                .toList();
    }

    private void updateEstadoOrdenCompra(OrdenCompra ordenCompra) {
        Long pendientes = entityManager.createQuery("""
                        select count(d) from OrdenCompraDetalle d
                        where d.ordenCompra.id = :ordenCompraId
                          and d.cantidadRecibida < d.cantidadSolicitada
                        """, Long.class)
                .setParameter("ordenCompraId", ordenCompra.getId())
                .getSingleResult();
        if (pendientes == 0L) {
            ordenCompra.setEstado(EstadoOrdenCompra.recibida);
        } else {
            ordenCompra.setEstado(EstadoOrdenCompra.parcialmente_recibida);
        }
        touchUpdate(ordenCompra);
    }

    private Inventario requireInventario(Long almacenId, Long productoId) {
        Inventario inventario = findInventario(almacenId, productoId);
        if (inventario == null) {
            throw new ResourceNotFoundException("Inventario no encontrado para almacen " + almacenId + " y producto " + productoId);
        }
        return inventario;
    }

    private Inventario findInventario(Long almacenId, Long productoId) {
        List<Inventario> result = entityManager.createQuery("""
                        select i from Inventario i
                        where i.almacen.id = :almacenId and i.producto.id = :productoId
                        """, Inventario.class)
                .setParameter("almacenId", almacenId)
                .setParameter("productoId", productoId)
                .getResultList();
        return result.isEmpty() ? null : result.getFirst();
    }

    private Lote findLoteByCodigo(Long empresaId, String codigoLote) {
        List<Lote> result = entityManager.createQuery("""
                        select l from Lote l
                        where l.empresa.id = :empresaId and l.codigoLote = :codigoLote
                        """, Lote.class)
                .setParameter("empresaId", empresaId)
                .setParameter("codigoLote", codigoLote)
                .getResultList();
        return result.isEmpty() ? null : result.getFirst();
    }

    private <T> List<T> queryByCompany(String jpql, Class<T> type, Long empresaId) {
        return entityManager.createQuery(jpql, type)
                .setParameter("empresaId", empresaId)
                .getResultList();
    }

    private <T> T findRequired(Long id, Class<T> type) {
        T entity = entityManager.find(type, id);
        if (entity == null) {
            throw new ResourceNotFoundException(type.getSimpleName() + " not found: " + id);
        }
        return entity;
    }

    private <T> T findOptional(Long id, Class<T> type) {
        return id == null ? null : findRequired(id, type);
    }

    private void touchCreate(Empresa entity) {
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(entity.getCreatedAt());
    }

    private void touchCreate(PlanSuscripcion entity) {
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(entity.getCreatedAt());
    }

    private void touchCreate(AppUser entity) {
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(entity.getCreatedAt());
        if (entity.getActivo() == null) {
            entity.setActivo(Boolean.TRUE);
        }
    }

    private void touchCreate(Suscripcion entity) {
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(entity.getCreatedAt());
    }

    private void touchCreate(Tienda entity) {
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(entity.getCreatedAt());
    }

    private void touchCreate(Almacen entity) {
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(entity.getCreatedAt());
    }

    private void touchCreate(Categoria entity) {
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(entity.getCreatedAt());
    }

    private void touchCreate(Usuario entity) {
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(entity.getCreatedAt());
    }

    private void touchCreate(Rol entity) {
        entity.setCreatedAt(Instant.now());
    }

    private TipoRol resolveTipoRol(SaasRequests.RolRequest request) {
        if (request.getTipo() != null) {
            return request.getTipo();
        }
        if (Boolean.TRUE.equals(request.getEsSistema())) {
            return TipoRol.sistema;
        }
        return request.getEmpresaId() != null ? TipoRol.empresa : TipoRol.sistema;
    }

    private void createUsuarioEmpresaIfNeeded(Usuario usuario, Empresa empresa) {
        if (usuario == null || empresa == null) {
            return;
        }
        Long exists = entityManager.createQuery("""
                        select count(ue) from UsuarioEmpresa ue
                        where ue.usuario.id = :usuarioId and ue.empresa.id = :empresaId
                        """, Long.class)
                .setParameter("usuarioId", usuario.getId())
                .setParameter("empresaId", empresa.getId())
                .getSingleResult();
        if (exists > 0) {
            return;
        }
        UsuarioEmpresa relation = new UsuarioEmpresa();
        relation.setUsuario(usuario);
        relation.setEmpresa(empresa);
        relation.setEstado(com.codesfree.prueba.model.saas.EstadoGeneral.activo);
        relation.setEsDueno(false);
        relation.setCreatedAt(Instant.now());
        relation.setUpdatedAt(relation.getCreatedAt());
        entityManager.persist(relation);
    }

    private void createUsuarioTiendaIfNeeded(Usuario usuario, Empresa empresa, Tienda tienda) {
        if (usuario == null || tienda == null) {
            return;
        }
        Empresa resolvedEmpresa = empresa != null ? empresa : tienda.getEmpresa();
        createUsuarioEmpresaIfNeeded(usuario, resolvedEmpresa);
        Long exists = entityManager.createQuery("""
                        select count(ut) from UsuarioTienda ut
                        where ut.usuario.id = :usuarioId
                          and ut.empresa.id = :empresaId
                          and ut.tienda.id = :tiendaId
                        """, Long.class)
                .setParameter("usuarioId", usuario.getId())
                .setParameter("empresaId", resolvedEmpresa.getId())
                .setParameter("tiendaId", tienda.getId())
                .getSingleResult();
        if (exists > 0) {
            return;
        }
        UsuarioTienda relation = new UsuarioTienda();
        relation.setUsuario(usuario);
        relation.setEmpresa(resolvedEmpresa);
        relation.setTienda(tienda);
        relation.setEstado(com.codesfree.prueba.model.saas.EstadoGeneral.activo);
        relation.setCreatedAt(Instant.now());
        entityManager.persist(relation);
    }

    private void touchCreate(Proveedor entity) {
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(entity.getCreatedAt());
    }

    private void touchCreate(Producto entity) {
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(entity.getCreatedAt());
    }

    private void touchCreate(Cliente entity) {
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(entity.getCreatedAt());
    }

    private void touchCreate(OrdenCompra entity) {
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(entity.getCreatedAt());
    }

    private void touchUpdate(Empresa entity) {
        entity.setUpdatedAt(Instant.now());
    }

    private void touchUpdate(PlanSuscripcion entity) {
        entity.setUpdatedAt(Instant.now());
    }

    private void touchUpdate(AppUser entity) {
        entity.setUpdatedAt(Instant.now());
    }

    private void touchUpdate(OrdenCompra entity) {
        entity.setUpdatedAt(Instant.now());
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void logEvent(Empresa empresa, Tienda tienda, Usuario usuario, String modulo, String accion, String entidad, Long entidadId, String descripcion) {
        BitacoraEvento evento = new BitacoraEvento();
        evento.setEmpresa(empresa);
        evento.setTienda(tienda);
        evento.setUsuario(usuario);
        evento.setModulo(modulo);
        evento.setAccion(accion);
        evento.setEntidad(entidad);
        evento.setEntidadId(entidadId);
        evento.setDescripcion(descripcion);
        evento.setCreatedAt(Instant.now());
        entityManager.persist(evento);
    }

    private Map<String, Object> toPlanMap(PlanSuscripcion plan) {
        return orderedMap(
                "id", plan.getId(),
                "nombre", plan.getNombre(),
                "descripcion", plan.getDescripcion(),
                "precioMensual", plan.getPrecioMensual(),
                "limiteTiendas", plan.getLimiteTiendas(),
                "limiteAlmacenes", plan.getLimiteAlmacenes(),
                "limiteUsuarios", plan.getLimiteUsuarios(),
                "limiteProductos", plan.getLimiteProductos(),
                "incluyeCrm", plan.getIncluyeCrm(),
                "incluyeIa", plan.getIncluyeIa(),
                "soporte", plan.getSoporte(),
                "estado", plan.getEstado(),
                "createdAt", plan.getCreatedAt(),
                "updatedAt", plan.getUpdatedAt());
    }

    private void applyPlanRequest(PlanSuscripcion plan, SaasRequests.PlanRequest request) {
        plan.setNombre(request.getNombre());
        plan.setDescripcion(request.getDescripcion());
        plan.setPrecioMensual(request.getPrecioMensual());
        plan.setLimiteTiendas(request.getLimiteTiendas());
        plan.setLimiteAlmacenes(request.getLimiteAlmacenes());
        plan.setLimiteUsuarios(request.getLimiteUsuarios());
        plan.setLimiteProductos(request.getLimiteProductos());
        plan.setIncluyeCrm(request.getIncluyeCrm() == null ? Boolean.TRUE : request.getIncluyeCrm());
        plan.setIncluyeIa(request.getIncluyeIa() == null ? Boolean.FALSE : request.getIncluyeIa());
        plan.setSoporte(request.getSoporte());
        if (request.getEstado() != null) {
            plan.setEstado(request.getEstado());
        }
    }

    private Map<String, Object> toSystemUserMap(AppUser user) {
        return orderedMap(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "activo", user.getActivo(),
                "estadoSistema", Boolean.TRUE.equals(user.getActivo()) ? "activo" : "inactivo",
                "createdAt", user.getCreatedAt(),
                "updatedAt", user.getUpdatedAt());
    }

    private String requirePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria para crear usuarios del sistema");
        }
        return password;
    }

    private Map<String, Object> toEmpresaMap(Empresa empresa) {
        return orderedMap(
                "id", empresa.getId(),
                "nombreComercial", empresa.getNombreComercial(),
                "razonSocial", empresa.getRazonSocial(),
                "nit", empresa.getNit(),
                "correo", empresa.getCorreo(),
                "telefono", empresa.getTelefono(),
                "direccion", empresa.getDireccion(),
                "planActualId", idOf(empresa.getPlanActual()),
                "usuarioRegistroId", idOf(empresa.getUsuarioRegistro()),
                "estado", empresa.getEstado(),
                "createdAt", empresa.getCreatedAt());
    }

    private Map<String, Object> toSuscripcionMap(Suscripcion suscripcion) {
        return orderedMap(
                "id", suscripcion.getId(),
                "empresaId", idOf(suscripcion.getEmpresa()),
                "planSuscripcionId", idOf(suscripcion.getPlanSuscripcion()),
                "usuarioRegistroId", idOf(suscripcion.getUsuarioRegistro()),
                "fechaInicio", suscripcion.getFechaInicio(),
                "fechaFin", suscripcion.getFechaFin(),
                "estado", suscripcion.getEstado(),
                "montoPagado", suscripcion.getMontoPagado(),
                "renovacionAutomatica", suscripcion.getRenovacionAutomatica(),
                "createdAt", suscripcion.getCreatedAt());
    }

    private Map<String, Object> toTiendaMap(Tienda tienda) {
        return orderedMap(
                "id", tienda.getId(),
                "empresaId", idOf(tienda.getEmpresa()),
                "usuarioRegistroId", idOf(tienda.getUsuarioRegistro()),
                "nombre", tienda.getNombre(),
                "slug", tienda.getSlug(),
                "rubro", tienda.getRubro(),
                "descripcion", tienda.getDescripcion(),
                "correoContacto", tienda.getCorreoContacto(),
                "telefono", tienda.getTelefono(),
                "estado", tienda.getEstado(),
                "createdAt", tienda.getCreatedAt(),
                "updatedAt", tienda.getUpdatedAt());
    }

    private Map<String, Object> toAlmacenMap(Almacen almacen) {
        return orderedMap(
                "id", almacen.getId(),
                "empresaId", idOf(almacen.getEmpresa()),
                "tiendaId", idOf(almacen.getTienda()),
                "nombre", almacen.getNombre(),
                "direccion", almacen.getDireccion(),
                "referencia", almacen.getReferencia(),
                "estado", almacen.getEstado());
    }

    private Map<String, Object> toCategoriaMap(Categoria categoria) {
        return orderedMap(
                "id", categoria.getId(),
                "empresaId", idOf(categoria.getEmpresa()),
                "tiendaId", idOf(categoria.getTienda()),
                "categoriaPadreId", idOf(categoria.getCategoriaPadre()),
                "nombre", categoria.getNombre(),
                "slug", categoria.getSlug(),
                "descripcion", categoria.getDescripcion(),
                "estado", categoria.getEstado());
    }

    private Map<String, Object> toUsuarioMap(Usuario usuario) {
        UserScope scope = resolveUserScope(usuario);
        List<Map<String, Object>> empresas = entityManager.createQuery("""
                        select ue from UsuarioEmpresa ue
                        join fetch ue.empresa
                        where ue.usuario.id = :usuarioId
                        order by ue.id
                        """, UsuarioEmpresa.class)
                .setParameter("usuarioId", usuario.getId())
                .getResultList()
                .stream()
                .map(ue -> orderedMap(
                        "id", ue.getId(),
                        "empresaId", idOf(ue.getEmpresa()),
                        "empresaNombre", idName(ue.getEmpresa()),
                        "cargo", ue.getCargo(),
                        "esDueno", ue.getEsDueno(),
                        "estado", ue.getEstado()))
                .toList();
        List<Map<String, Object>> tiendas = entityManager.createQuery("""
                        select ut from UsuarioTienda ut
                        join fetch ut.empresa
                        join fetch ut.tienda
                        where ut.usuario.id = :usuarioId
                        order by ut.id
                        """, UsuarioTienda.class)
                .setParameter("usuarioId", usuario.getId())
                .getResultList()
                .stream()
                .map(ut -> orderedMap(
                        "id", ut.getId(),
                        "empresaId", idOf(ut.getEmpresa()),
                        "tiendaId", idOf(ut.getTienda()),
                        "tiendaNombre", idName(ut.getTienda()),
                        "estado", ut.getEstado()))
                .toList();
        List<Map<String, Object>> roles = entityManager.createQuery("""
                        select ur from UsuarioRol ur
                        join fetch ur.rol
                        left join fetch ur.empresa
                        left join fetch ur.tienda
                        where ur.usuario.id = :usuarioId
                        """, UsuarioRol.class)
                .setParameter("usuarioId", usuario.getId())
                .getResultList()
                .stream()
                .map(ur -> orderedMap(
                        "id", ur.getId(),
                        "rolId", idOf(ur.getRol()),
                        "rolCodigo", ur.getRol().getCodigo(),
                        "rolNombre", ur.getRol().getNombre(),
                        "empresaId", idOf(ur.getEmpresa()),
                        "tiendaId", idOf(ur.getTienda())))
                .toList();

        return orderedMap(
                "id", usuario.getId(),
                "fotoArchivoId", idOf(usuario.getFotoArchivo()),
                "empresaId", idOf(scope.empresa()),
                "tiendaId", idOf(scope.tienda()),
                "nombres", usuario.getNombres(),
                "apellidos", usuario.getApellidos(),
                "email", usuario.getEmail(),
                "telefono", usuario.getTelefono(),
                "estado", usuario.getEstado(),
                "empresas", empresas,
                "tiendas", tiendas,
                "roles", roles,
                "createdAt", usuario.getCreatedAt(),
                "updatedAt", usuario.getUpdatedAt());
    }

    private Map<String, Object> toRolMap(Rol rol) {
        List<Map<String, Object>> permisos = entityManager.createQuery("""
                        select rp from RolPermiso rp join fetch rp.permiso where rp.rol.id = :rolId
                        """, RolPermiso.class)
                .setParameter("rolId", rol.getId())
                .getResultList()
                .stream()
                .map(rp -> orderedMap(
                        "id", rp.getId(),
                        "permisoId", idOf(rp.getPermiso()),
                        "codigo", rp.getPermiso().getCodigo()))
                .toList();

        return orderedMap(
                "id", rol.getId(),
                "empresaId", idOf(rol.getEmpresa()),
                "codigo", rol.getCodigo(),
                "nombre", rol.getNombre(),
                "descripcion", rol.getDescripcion(),
                "tipo", rol.getTipo(),
                "esSistema", rol.getEsSistema(),
                "estado", rol.getEstado(),
                "permisos", permisos);
    }

    private UserScope resolveUserScope(Usuario usuario) {
        Empresa empresa = entityManager.createQuery("""
                        select ue.empresa from UsuarioEmpresa ue
                        where ue.usuario.id = :usuarioId
                        order by ue.id
                        """, Empresa.class)
                .setParameter("usuarioId", usuario.getId())
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
        Tienda tienda = entityManager.createQuery("""
                        select ut.tienda from UsuarioTienda ut
                        where ut.usuario.id = :usuarioId
                        order by ut.id
                        """, Tienda.class)
                .setParameter("usuarioId", usuario.getId())
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
        if (empresa == null && tienda != null) {
            empresa = tienda.getEmpresa();
        }
        return new UserScope(empresa, tienda);
    }

    private Map<String, Object> toPermisoMap(Permiso permiso) {
        return orderedMap(
                "id", permiso.getId(),
                "codigo", permiso.getCodigo(),
                "modulo", permiso.getModulo(),
                "accion", permiso.getAccion(),
                "descripcion", permiso.getDescripcion(),
                "estado", permiso.getEstado());
    }

    private Map<String, Object> toArchivoMap(Archivo archivo) {
        return orderedMap(
                "id", archivo.getId(),
                "empresaId", idOf(archivo.getEmpresa()),
                "usuarioSubidaId", idOf(archivo.getUsuarioSubida()),
                "nombreOriginal", archivo.getNombreOriginal(),
                "rutaArchivo", archivo.getRutaArchivo(),
                "urlPublica", archivo.getUrlPublica(),
                "tipoArchivo", archivo.getTipoArchivo(),
                "extension", archivo.getExtension(),
                "tamanioBytes", archivo.getTamanioBytes(),
                "estado", archivo.getEstado(),
                "createdAt", archivo.getCreatedAt());
    }

    private Map<String, Object> toProveedorMap(Proveedor proveedor) {
        return orderedMap(
                "id", proveedor.getId(),
                "empresaId", idOf(proveedor.getEmpresa()),
                "nombre", proveedor.getNombre(),
                "nit", proveedor.getNit(),
                "correo", proveedor.getCorreo(),
                "telefono", proveedor.getTelefono(),
                "direccion", proveedor.getDireccion(),
                "contactoPrincipal", proveedor.getContactoPrincipal(),
                "estado", proveedor.getEstado());
    }

    private Map<String, Object> toProductoMap(Producto producto) {
        return orderedMap(
                "id", producto.getId(),
                "empresaId", idOf(producto.getEmpresa()),
                "tiendaId", idOf(producto.getTienda()),
                "categoriaId", idOf(producto.getCategoria()),
                "proveedorPrincipalId", idOf(producto.getProveedorPrincipal()),
                "nombre", producto.getNombre(),
                "slug", producto.getSlug(),
                "sku", producto.getSku(),
                "descripcion", producto.getDescripcion(),
                "marca", producto.getMarca(),
                "precioVenta", producto.getPrecioVenta(),
                "costoReferencial", producto.getCostoReferencial(),
                "unidadMedida", producto.getUnidadMedida(),
                "estadoProducto", producto.getEstadoProducto(),
                "estado", producto.getEstado());
    }

    private Map<String, Object> toClienteMap(Cliente cliente) {
        return orderedMap(
                "id", cliente.getId(),
                "empresaId", idOf(cliente.getEmpresa()),
                "tiendaId", idOf(cliente.getTienda()),
                "nombres", cliente.getNombres(),
                "apellidos", cliente.getApellidos(),
                "email", cliente.getEmail(),
                "telefono", cliente.getTelefono(),
                "documentoIdentidad", cliente.getDocumentoIdentidad(),
                "direccion", cliente.getDireccion(),
                "estado", cliente.getEstado());
    }

    private Map<String, Object> toInventarioMap(Inventario inventario) {
        return orderedMap(
                "id", inventario.getId(),
                "empresaId", idOf(inventario.getEmpresa()),
                "almacenId", idOf(inventario.getAlmacen()),
                "productoId", idOf(inventario.getProducto()),
                "cantidadDisponible", inventario.getCantidadDisponible(),
                "cantidadReservada", inventario.getCantidadReservada(),
                "stockMinimo", inventario.getStockMinimo(),
                "stockMaximo", inventario.getStockMaximo(),
                "updatedAt", inventario.getUpdatedAt());
    }

    private Map<String, Object> toOrdenVentaMap(OrdenVenta ordenVenta) {
        List<Map<String, Object>> detalles = entityManager.createQuery("""
                        select d from OrdenVentaDetalle d join fetch d.producto
                        where d.ordenVenta.id = :ordenVentaId
                        """, OrdenVentaDetalle.class)
                .setParameter("ordenVentaId", ordenVenta.getId())
                .getResultList()
                .stream()
                .map(d -> orderedMap(
                        "id", d.getId(),
                        "productoId", idOf(d.getProducto()),
                        "producto", d.getProducto().getNombre(),
                        "cantidad", d.getCantidad(),
                        "precioUnitario", d.getPrecioUnitario(),
                        "descuento", d.getDescuento(),
                        "subtotal", d.getSubtotal()))
                .toList();

        List<Map<String, Object>> estados = entityManager.createQuery("""
                        select e from OrdenVentaEstado e left join fetch e.usuario
                        where e.ordenVenta.id = :ordenVentaId
                        order by e.id
                        """, OrdenVentaEstado.class)
                .setParameter("ordenVentaId", ordenVenta.getId())
                .getResultList()
                .stream()
                .map(e -> orderedMap(
                        "id", e.getId(),
                        "estadoAnterior", e.getEstadoAnterior(),
                        "estadoNuevo", e.getEstadoNuevo(),
                        "usuarioId", idOf(e.getUsuario()),
                        "observacion", e.getObservacion(),
                        "createdAt", e.getCreatedAt()))
                .toList();

        return orderedMap(
                "id", ordenVenta.getId(),
                "empresaId", idOf(ordenVenta.getEmpresa()),
                "tiendaId", idOf(ordenVenta.getTienda()),
                "clienteId", idOf(ordenVenta.getCliente()),
                "codigo", ordenVenta.getCodigo(),
                "estado", ordenVenta.getEstado(),
                "subtotal", ordenVenta.getSubtotal(),
                "descuentoTotal", ordenVenta.getDescuentoTotal(),
                "total", ordenVenta.getTotal(),
                "observaciones", ordenVenta.getObservaciones(),
                "createdAt", ordenVenta.getCreatedAt(),
                "detalles", detalles,
                "historialEstados", estados);
    }

    private Map<String, Object> toOrdenCompraMap(OrdenCompra ordenCompra) {
        List<Map<String, Object>> detalles = entityManager.createQuery("""
                        select d from OrdenCompraDetalle d join fetch d.producto
                        where d.ordenCompra.id = :ordenCompraId
                        """, OrdenCompraDetalle.class)
                .setParameter("ordenCompraId", ordenCompra.getId())
                .getResultList()
                .stream()
                .map(d -> orderedMap(
                        "id", d.getId(),
                        "productoId", idOf(d.getProducto()),
                        "producto", d.getProducto().getNombre(),
                        "cantidadSolicitada", d.getCantidadSolicitada(),
                        "cantidadRecibida", d.getCantidadRecibida(),
                        "costoUnitario", d.getCostoUnitario(),
                        "subtotal", d.getSubtotal()))
                .toList();

        return orderedMap(
                "id", ordenCompra.getId(),
                "empresaId", idOf(ordenCompra.getEmpresa()),
                "proveedorId", idOf(ordenCompra.getProveedor()),
                "almacenId", idOf(ordenCompra.getAlmacen()),
                "usuarioCreacionId", idOf(ordenCompra.getUsuarioCreacion()),
                "codigo", ordenCompra.getCodigo(),
                "fechaEmision", ordenCompra.getFechaEmision(),
                "fechaEstimadaRecepcion", ordenCompra.getFechaEstimadaRecepcion(),
                "estado", ordenCompra.getEstado(),
                "subtotal", ordenCompra.getSubtotal(),
                "total", ordenCompra.getTotal(),
                "observaciones", ordenCompra.getObservaciones(),
                "detalles", detalles);
    }

    private Map<String, Object> toRecepcionCompraMap(RecepcionCompra recepcion) {
        List<Map<String, Object>> detalles = entityManager.createQuery("""
                        select d from RecepcionCompraDetalle d join fetch d.producto
                        where d.recepcionCompra.id = :recepcionId
                        """, RecepcionCompraDetalle.class)
                .setParameter("recepcionId", recepcion.getId())
                .getResultList()
                .stream()
                .map(d -> orderedMap(
                        "id", d.getId(),
                        "productoId", idOf(d.getProducto()),
                        "cantidadRecibida", d.getCantidadRecibida(),
                        "costoUnitario", d.getCostoUnitario(),
                        "ordenCompraDetalleId", idOf(d.getOrdenCompraDetalle())))
                .toList();

        return orderedMap(
                "id", recepcion.getId(),
                "empresaId", idOf(recepcion.getEmpresa()),
                "ordenCompraId", idOf(recepcion.getOrdenCompra()),
                "almacenId", idOf(recepcion.getAlmacen()),
                "usuarioRecepcionId", idOf(recepcion.getUsuarioRecepcion()),
                "codigo", recepcion.getCodigo(),
                "fechaRecepcion", recepcion.getFechaRecepcion(),
                "observaciones", recepcion.getObservaciones(),
                "detalles", detalles);
    }

    private Map<String, Object> toMovimientoMap(MovimientoInventario movimiento) {
        return orderedMap(
                "id", movimiento.getId(),
                "empresaId", idOf(movimiento.getEmpresa()),
                "almacenId", idOf(movimiento.getAlmacen()),
                "productoId", idOf(movimiento.getProducto()),
                "tipo", movimiento.getTipo(),
                "cantidad", movimiento.getCantidad(),
                "cantidadResultante", movimiento.getCantidadResultante(),
                "motivo", movimiento.getMotivo(),
                "usuarioId", idOf(movimiento.getUsuario()),
                "ordenVentaDetalleId", idOf(movimiento.getOrdenVentaDetalle()),
                "recepcionCompraDetalleId", idOf(movimiento.getRecepcionCompraDetalle()),
                "loteDetalleId", idOf(movimiento.getLoteDetalle()),
                "createdAt", movimiento.getCreatedAt());
    }

    private Map<String, Object> toLoteMap(Lote lote) {
        List<Map<String, Object>> detalles = entityManager.createQuery("""
                        select ld from LoteDetalle ld join fetch ld.producto
                        where ld.lote.id = :loteId
                        """, LoteDetalle.class)
                .setParameter("loteId", lote.getId())
                .getResultList()
                .stream()
                .map(ld -> orderedMap(
                        "id", ld.getId(),
                        "productoId", idOf(ld.getProducto()),
                        "producto", ld.getProducto().getNombre(),
                        "cantidadInicial", ld.getCantidadInicial(),
                        "cantidadActual", ld.getCantidadActual(),
                        "costoUnitario", ld.getCostoUnitario()))
                .toList();

        return orderedMap(
                "id", lote.getId(),
                "empresaId", idOf(lote.getEmpresa()),
                "proveedorId", idOf(lote.getProveedor()),
                "almacenId", idOf(lote.getAlmacen()),
                "ordenCompraId", idOf(lote.getOrdenCompra()),
                "recepcionCompraId", idOf(lote.getRecepcionCompra()),
                "codigoLote", lote.getCodigoLote(),
                "fechaIngreso", lote.getFechaIngreso(),
                "fechaVencimiento", lote.getFechaVencimiento(),
                "estado", lote.getEstado(),
                "detalles", detalles);
    }

    private Map<String, Object> toBitacoraMap(BitacoraEvento evento) {
        return orderedMap(
                "id", evento.getId(),
                "empresaId", idOf(evento.getEmpresa()),
                "tiendaId", idOf(evento.getTienda()),
                "usuarioId", idOf(evento.getUsuario()),
                "modulo", evento.getModulo(),
                "accion", evento.getAccion(),
                "entidad", evento.getEntidad(),
                "entidadId", evento.getEntidadId(),
                "descripcion", evento.getDescripcion(),
                "createdAt", evento.getCreatedAt());
    }

    private Long idOf(Object entity) {
        if (entity == null) {
            return null;
        }
        return switch (entity) {
            case Empresa e -> e.getId();
            case PlanSuscripcion p -> p.getId();
            case Usuario u -> u.getId();
            case Tienda t -> t.getId();
            case Categoria c -> c.getId();
            case Proveedor p -> p.getId();
            case Producto p -> p.getId();
            case Cliente c -> c.getId();
            case Permiso p -> p.getId();
            case Rol r -> r.getId();
            case Almacen a -> a.getId();
            case OrdenVenta ov -> ov.getId();
            case OrdenVentaDetalle ovd -> ovd.getId();
            case OrdenCompra oc -> oc.getId();
            case OrdenCompraDetalle ocd -> ocd.getId();
            case RecepcionCompraDetalle rcd -> rcd.getId();
            case LoteDetalle ld -> ld.getId();
            case Archivo a -> a.getId();
            default -> null;
        };
    }

    private Map<String, Object> orderedMap(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(Objects.toString(values[i]), values[i + 1]);
        }
        return map;
    }

    private record UserScope(Empresa empresa, Tienda tienda) {
    }
}
