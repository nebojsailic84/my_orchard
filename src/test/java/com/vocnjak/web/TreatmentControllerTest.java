package com.vocnjak.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TreatmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String createTree(String orchardName) throws Exception {
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
                        .param("species", "Apple"))
                .andExpect(status().is3xxRedirection());

        String gridPage = mockMvc.perform(get(orchardUrl))
                .andReturn().getResponse().getContentAsString();
        String marker = orchardUrl + "/trees/";
        int start = gridPage.indexOf(marker);
        int end = gridPage.indexOf('"', start);
        return gridPage.substring(start, end);
    }

    @Test
    void addTreatment_thenAppearsOnTreeViewPage() throws Exception {
        String treeUrl = createTree("Treatment Orchard");

        mockMvc.perform(post(treeUrl + "/treatments")
                        .param("productName", "Copper fungicide")
                        .param("rate", "30ml / 10L")
                        .param("applicationDate", "2024-03-15")
                        .param("harvestIntervalDays", "14"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get(treeUrl))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Copper fungicide")))
                .andExpect(content().string(containsString("2024-03-15")));
    }

    @Test
    void deleteTreatment_removesFromViewPage() throws Exception {
        String treeUrl = createTree("Delete Treatment Orchard");

        mockMvc.perform(post(treeUrl + "/treatments")
                        .param("productName", "Copper fungicide")
                        .param("applicationDate", "2024-03-15"))
                .andExpect(status().is3xxRedirection());

        String treePage = mockMvc.perform(get(treeUrl))
                .andReturn().getResponse().getContentAsString();
        String marker = treeUrl + "/treatments/";
        int start = treePage.indexOf(marker);
        int end = treePage.indexOf('"', start);
        String deleteUrl = treePage.substring(start, end);

        mockMvc.perform(post(deleteUrl))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get(treeUrl))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Copper fungicide"))));
    }
}
