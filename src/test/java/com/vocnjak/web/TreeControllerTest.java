package com.vocnjak.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TreeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String createOrchard(String name, int rows, int columns) throws Exception {
        MvcResult result = mockMvc.perform(post("/orchards")
                        .param("name", name)
                        .param("rows", String.valueOf(rows))
                        .param("columns", String.valueOf(columns)))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        return result.getResponse().getRedirectedUrl();
    }

    @Test
    void addTree_thenAppearsInGrid() throws Exception {
        String orchardUrl = createOrchard("Grid Orchard", 2, 2);

        mockMvc.perform(post(orchardUrl + "/trees")
                        .param("row", "0")
                        .param("col", "0")
                        .param("species", "Apple")
                        .param("variety", "Golden Delicious"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get(orchardUrl))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Apple")));
    }

    @Test
    void addSecondTree_toSameCell_isRejected() throws Exception {
        String orchardUrl = createOrchard("Conflict Orchard", 2, 2);

        mockMvc.perform(post(orchardUrl + "/trees")
                        .param("row", "0")
                        .param("col", "0")
                        .param("species", "Pear"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post(orchardUrl + "/trees")
                        .param("row", "0")
                        .param("col", "0")
                        .param("species", "Plum"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get(orchardUrl))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pear")))
                .andExpect(content().string(not(containsString("Plum"))));
    }

    @Test
    void editTree_updatesFieldsShownOnViewPage() throws Exception {
        String orchardUrl = createOrchard("Edit Orchard", 2, 2);

        mockMvc.perform(post(orchardUrl + "/trees")
                        .param("row", "0")
                        .param("col", "0")
                        .param("species", "Apple")
                        .param("variety", "Fuji"))
                .andExpect(status().is3xxRedirection());

        String gridPage = mockMvc.perform(get(orchardUrl))
                .andReturn().getResponse().getContentAsString();
        String treeUrl = extractTreeUrl(gridPage, orchardUrl);

        mockMvc.perform(post(treeUrl)
                        .param("species", "Apple")
                        .param("variety", "Granny Smith"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get(treeUrl))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Granny Smith")));
    }

    @Test
    void deleteTree_removesFromGrid() throws Exception {
        String orchardUrl = createOrchard("Delete Orchard", 2, 2);

        mockMvc.perform(post(orchardUrl + "/trees")
                        .param("row", "0")
                        .param("col", "0")
                        .param("species", "Cherry"))
                .andExpect(status().is3xxRedirection());

        String gridPage = mockMvc.perform(get(orchardUrl))
                .andReturn().getResponse().getContentAsString();
        String treeUrl = extractTreeUrl(gridPage, orchardUrl);

        mockMvc.perform(post(treeUrl + "/delete"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get(orchardUrl))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Cherry"))));
    }

    @Test
    void addTree_withImage_thenImageEndpointServesIt() throws Exception {
        String orchardUrl = createOrchard("Photo Orchard", 2, 2);
        byte[] imageBytes = "fake-image-bytes".getBytes();

        mockMvc.perform(multipart(orchardUrl + "/trees")
                        .file(new MockMultipartFile("image", "photo.jpg", "image/jpeg", imageBytes))
                        .param("row", "0")
                        .param("col", "0")
                        .param("species", "Apple"))
                .andExpect(status().is3xxRedirection());

        String gridPage = mockMvc.perform(get(orchardUrl))
                .andReturn().getResponse().getContentAsString();
        String treeUrl = extractTreeUrl(gridPage, orchardUrl);

        mockMvc.perform(get(treeUrl + "/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imageBytes));
    }

    @Test
    void markEmpty_thenShowsPlaceholderInGrid_andCanBeRemoved() throws Exception {
        String orchardUrl = createOrchard("Placeholder Orchard", 2, 2);

        mockMvc.perform(post(orchardUrl + "/trees/empty")
                        .param("row", "0")
                        .param("col", "0"))
                .andExpect(status().is3xxRedirection());

        String gridPage = mockMvc.perform(get(orchardUrl))
                .andReturn().getResponse().getContentAsString();
        assertThat(gridPage).contains("cell placeholder");

        String marker = orchardUrl + "/trees/";
        int start = gridPage.indexOf(marker);
        int end = gridPage.indexOf('"', start);
        String deleteUrl = gridPage.substring(start, end);

        mockMvc.perform(post(deleteUrl))
                .andExpect(status().is3xxRedirection());

        String gridPageAfter = mockMvc.perform(get(orchardUrl))
                .andReturn().getResponse().getContentAsString();
        assertThat(gridPageAfter).doesNotContain("cell placeholder");
    }

    @Test
    void copyTree_thenPasteIntoEmptyCell_createsMatchingTree() throws Exception {
        String orchardUrl = createOrchard("Copy Orchard", 2, 2);

        mockMvc.perform(post(orchardUrl + "/trees")
                        .param("row", "0")
                        .param("col", "0")
                        .param("species", "Apple")
                        .param("variety", "Golden Delicious"))
                .andExpect(status().is3xxRedirection());

        String gridPage = mockMvc.perform(get(orchardUrl))
                .andReturn().getResponse().getContentAsString();
        String treeUrl = extractTreeUrl(gridPage, orchardUrl);

        mockMvc.perform(post(treeUrl + "/copy"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post(orchardUrl + "/trees/paste")
                        .param("row", "1")
                        .param("col", "1"))
                .andExpect(status().is3xxRedirection());

        String gridAfterPaste = mockMvc.perform(get(orchardUrl))
                .andReturn().getResponse().getContentAsString();
        int occurrences = gridAfterPaste.split("Golden Delicious", -1).length - 1;
        assertThat(occurrences).isEqualTo(2);
    }

    private String extractTreeUrl(String gridPageHtml, String orchardUrl) {
        String marker = orchardUrl + "/trees/";
        int start = gridPageHtml.indexOf(marker);
        int end = gridPageHtml.indexOf('"', start);
        return gridPageHtml.substring(start, end);
    }
}