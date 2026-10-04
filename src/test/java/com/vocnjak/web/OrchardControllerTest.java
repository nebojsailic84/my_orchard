package com.vocnjak.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrchardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createOrchard_thenGridPageShowsEmptyCells() throws Exception {
        var createResult = mockMvc.perform(post("/orchards")
                        .param("name", "Backyard")
                        .param("rows", "2")
                        .param("columns", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orchards/*"))
                .andReturn();

        String redirectedUrl = createResult.getResponse().getRedirectedUrl();

        mockMvc.perform(get(redirectedUrl))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Backyard")));
    }

    @Test
    void listOrchards_showsCreatedOrchard() throws Exception {
        mockMvc.perform(post("/orchards")
                        .param("name", "Front Yard")
                        .param("rows", "1")
                        .param("columns", "1"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/orchards"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Front Yard")));
    }

    @Test
    void createOrchard_withBlankName_rendersFormErrors() throws Exception {
        mockMvc.perform(post("/orchards")
                        .param("name", "")
                        .param("rows", "2")
                        .param("columns", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void addRow_growsGridOnNextView() throws Exception {
        var createResult = mockMvc.perform(post("/orchards")
                        .param("name", "Growable")
                        .param("rows", "2")
                        .param("columns", "2"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        String orchardUrl = createResult.getResponse().getRedirectedUrl();

        mockMvc.perform(post(orchardUrl + "/rows"))
                .andExpect(status().is3xxRedirection());

        String gridPage = mockMvc.perform(get(orchardUrl))
                .andReturn().getResponse().getContentAsString();
        long emptyLinkCount = gridPage.split("empty-link", -1).length - 1;
        org.assertj.core.api.Assertions.assertThat(emptyLinkCount).isEqualTo(6);
    }

    @Test
    void addColumn_growsGridOnNextView() throws Exception {
        var createResult = mockMvc.perform(post("/orchards")
                        .param("name", "Growable Cols")
                        .param("rows", "2")
                        .param("columns", "2"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        String orchardUrl = createResult.getResponse().getRedirectedUrl();

        mockMvc.perform(post(orchardUrl + "/columns"))
                .andExpect(status().is3xxRedirection());

        String gridPage = mockMvc.perform(get(orchardUrl))
                .andReturn().getResponse().getContentAsString();
        long emptyLinkCount = gridPage.split("empty-link", -1).length - 1;
        org.assertj.core.api.Assertions.assertThat(emptyLinkCount).isEqualTo(6);
    }
}