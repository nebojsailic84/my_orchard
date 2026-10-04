package com.vocnjak.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vocnjak.domain.Orchard;
import com.vocnjak.domain.Tree;
import com.vocnjak.domain.TreatmentRecord;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.ISpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PdfExportService {

    private final OrchardService orchardService;
    private final TreeService treeService;
    private final TreatmentService treatmentService;
    private final ISpringTemplateEngine templateEngine;

    public PdfExportService(OrchardService orchardService,
                             TreeService treeService,
                             TreatmentService treatmentService,
                             ISpringTemplateEngine templateEngine) {
        this.orchardService = orchardService;
        this.treeService = treeService;
        this.treatmentService = treatmentService;
        this.templateEngine = templateEngine;
    }

    public byte[] exportDetailed(Long orchardId, Locale locale) {
        Orchard orchard = orchardService.getOrchard(orchardId);
        List<List<Tree>> grid = treeService.buildGrid(orchard);

        List<Tree> trees = new ArrayList<>();
        Map<Long, List<TreatmentRecord>> treatmentsByTreeId = new HashMap<>();
        for (List<Tree> row : grid) {
            for (Tree tree : row) {
                if (tree != null && !tree.isEmptySpot()) {
                    trees.add(tree);
                    treatmentsByTreeId.put(tree.getId(), treatmentService.listTreatments(tree.getId()));
                }
            }
        }

        Context context = new Context(locale);
        context.setVariable("orchard", orchard);
        context.setVariable("trees", trees);
        context.setVariable("treatmentsByTreeId", treatmentsByTreeId);
        return renderPdf(templateEngine.process("export/detailed", context));
    }

    public byte[] exportMap(Long orchardId, Locale locale) {
        Orchard orchard = orchardService.getOrchard(orchardId);
        List<List<Tree>> grid = treeService.buildGrid(orchard);

        Context context = new Context(locale);
        context.setVariable("orchard", orchard);
        context.setVariable("grid", grid);
        return renderPdf(templateEngine.process("export/map", context));
    }

    private byte[] renderPdf(String html) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(() -> getClass().getResourceAsStream("/fonts/DejaVuSans.ttf"),
                    "DejaVu Sans", 400, com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(() -> getClass().getResourceAsStream("/fonts/DejaVuSans-Bold.ttf"),
                    "DejaVu Sans", 700, com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle.NORMAL, true);
            builder.useSVGDrawer(new com.openhtmltopdf.svgsupport.BatikSVGDrawer());
            builder.withHtmlContent(html, "/");
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
