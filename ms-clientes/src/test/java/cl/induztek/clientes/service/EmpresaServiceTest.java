package cl.induztek.clientes.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.induztek.clientes.model.ContactoEmpresa;
import cl.induztek.clientes.model.Empresa;
import cl.induztek.clientes.model.Instalacion;
import cl.induztek.clientes.repository.EmpresaRepository;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository repository;

    @InjectMocks
    private EmpresaService service;

    private Empresa crearEmpresaBase() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setRazonSocial("Minera Andina SpA");
        empresa.setRut("76.123.456-7");
        empresa.setGiro("Servicios Industriales");
        return empresa;
    }

    @Test
    void listarTodas_retornaListaDeEmpresas() {
        when(repository.findAll()).thenReturn(List.of(crearEmpresaBase()));

        List<Empresa> resultado = service.listarTodas();

        assertEquals(1, resultado.size());
        assertEquals("Minera Andina SpA", resultado.get(0).getRazonSocial());
        verify(repository, times(1)).findAll();
    }

    @Test
    void obtenerPorId_existenteYNoExistente() {
        Empresa empresa = crearEmpresaBase();
        when(repository.findById(1L)).thenReturn(Optional.of(empresa));
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Empresa encontrada = service.obtenerPorId(1L);
        Empresa noEncontrada = service.obtenerPorId(99L);

        assertNotNull(encontrada);
        assertEquals("76.123.456-7", encontrada.getRut());
        assertNull(noEncontrada);
    }

    @Test
    void obtenerPorRut_existenteYNoExistente() {
        Empresa empresa = crearEmpresaBase();
        when(repository.findByRut("76.123.456-7")).thenReturn(Optional.of(empresa));
        when(repository.findByRut("11.111.111-1")).thenReturn(Optional.empty());

        Optional<Empresa> encontrada = service.obtenerPorRut("76.123.456-7");
        Optional<Empresa> vacia = service.obtenerPorRut("11.111.111-1");

        assertTrue(encontrada.isPresent());
        assertEquals(1L, encontrada.get().getId());
        assertTrue(vacia.isEmpty());
    }

    @Test
    void guardar_conContactosEInstalaciones_vinculaEmpresaYGuarda() {
        Empresa empresa = crearEmpresaBase();

        ContactoEmpresa contacto = new ContactoEmpresa();
        contacto.setNombre("Carlos Pérez");
        contacto.setCorreo("cperez@andina.ejemplo");
        empresa.setContactos(List.of(contacto));

        Instalacion instalacion = new Instalacion();
        instalacion.setNombreInstalacion("Planta Norte");
        instalacion.setComuna("Antofagasta");
        empresa.setInstalaciones(List.of(instalacion));

        when(repository.save(any(Empresa.class))).thenAnswer(inv -> inv.getArgument(0));

        Empresa guardada = service.guardar(empresa);

        assertNotNull(guardada);
        assertSame(empresa, contacto.getEmpresa());
        assertSame(empresa, instalacion.getEmpresa());
        verify(repository, times(1)).save(empresa);
    }

    @Test
    void guardar_sinListasAsociadas_guardaCorrectamente() {
        Empresa empresa = crearEmpresaBase();
        empresa.setContactos(null);
        empresa.setInstalaciones(null);

        when(repository.save(empresa)).thenReturn(empresa);

        Empresa guardada = service.guardar(empresa);

        assertNotNull(guardada);
        assertEquals("Minera Andina SpA", guardada.getRazonSocial());
        verify(repository, times(1)).save(empresa);
    }

    @Test
    void eliminarEmpresa_invocaDeleteById() {
        doNothing().when(repository).deleteById(1L);

        service.eliminarEmpresa(1L);

        verify(repository, times(1)).deleteById(1L);
    }
}