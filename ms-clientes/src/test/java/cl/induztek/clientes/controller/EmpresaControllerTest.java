package cl.induztek.clientes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.induztek.clientes.model.Empresa;
import cl.induztek.clientes.service.EmpresaService;

@WebMvcTest(EmpresaController.class)
class EmpresaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmpresaService service;

    private Empresa crearEmpresaValida(Long id) {
        Empresa empresa = new Empresa();
        empresa.setId(id);
        empresa.setRazonSocial("Minera Andina SpA");
        empresa.setRut("76.123.456-7");
        empresa.setGiro("Servicios Industriales");
        return empresa;
    }

    @Test
    void debeListarEmpresas() throws Exception {
        when(service.listarTodas()).thenReturn(List.of(crearEmpresaValida(1L)));

        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].rut").value("76.123.456-7"));
    }

    @Test
    void debeObtenerEmpresaPorId_existenteYNoExistente() throws Exception {
        when(service.obtenerPorId(1L)).thenReturn(crearEmpresaValida(1L));
        when(service.obtenerPorId(99L)).thenReturn(null);

        mockMvc.perform(get("/api/empresas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razonSocial").value("Minera Andina SpA"));

        mockMvc.perform(get("/api/empresas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void debeCrearEmpresa() throws Exception {
        Empresa request = crearEmpresaValida(null);
        Empresa creada = crearEmpresaValida(1L);

        when(service.guardar(any(Empresa.class))).thenReturn(creada);

        mockMvc.perform(post("/api/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.razonSocial").value("Minera Andina SpA"));
    }

    @Test
    void debeEliminarEmpresa() throws Exception {
        doNothing().when(service).eliminarEmpresa(1L);

        mockMvc.perform(delete("/api/empresas/1"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).eliminarEmpresa(1L);
    }
}