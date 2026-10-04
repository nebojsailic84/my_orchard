package com.vocnjak.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String createOrchardWithTree(String orchardName) throws Exception {
        MvcResult orchardResult = mockMvc.perform(post("/orchards")
                        .param("name", orchardName)
                        .param("rows", "2")
                        .param("columns", "2"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        String orchardUrl = orchardResult.getResponse().getRedirectedUrl();

        mockMvc.perform(post(orchardUrl + "/trees")
                        .param("row", "0")
                        .param("col", "0")
                        .param("species", "Apple")
                        .param("variety", "Golden Delicious"))
                .andExpect(status().is3xxRedirection());

        return orchardUrl;
    }

    @Test
    void exportDetailed_returnsPdf() throws Exception {
        String orchardUrl = createOrchardWithTree("PDF Detailed Orchard");

        MvcResult result = mockMvc.perform(get(orchardUrl + "/export").param("format", "detailed"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        assertThat(bytes.length).isGreaterThan(0);
        assertThat(new String(bytes, 0, 4, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }

    @Test
    void exportMap_returnsPdf() throws Exception {
        String orchardUrl = createOrchardWithTree("PDF Map Orchard");

        MvcResult result = mockMvc.perform(get(orchardUrl + "/export").param("format", "map"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        assertThat(bytes.length).isGreaterThan(0);
        assertThat(new String(bytes, 0, 4, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF");
    }
}
