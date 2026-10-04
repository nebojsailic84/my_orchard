package com.vocnjak.web;

import com.vocnjak.service.PdfExportService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ExportController {

    private final PdfExportService pdfExportService;

    public ExportController(PdfExportService pdfExportService) {
        this.pdfExportService = pdfExportService;
    }

    @GetMapping("/orchards/{id}/export")
    @ResponseBody
    public ResponseEntity<byte[]> export(@PathVariable Long id,
                                          @RequestParam(defaultValue = "detailed") String format) {
        boolean map = "map".equals(format);
        byte[] pdf = map
                ? pdfExportService.exportMap(id, LocaleContextHolder.getLocale())
                : pdfExportService.exportDetailed(id, LocaleContextHolder.getLocale());

        String filename = "orchard-" + id + "-" + (map ? "map" : "detailed") + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf);
    }
}
