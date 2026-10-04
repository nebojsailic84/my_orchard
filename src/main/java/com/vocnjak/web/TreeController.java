package com.vocnjak.web;

import com.vocnjak.domain.FruitSpeciesCatalog;
import com.vocnjak.domain.Tree;
import com.vocnjak.service.OrchardService;
import com.vocnjak.service.TreatmentService;
import com.vocnjak.service.TreeService;
import com.vocnjak.service.exception.CellOccupiedException;
import com.vocnjak.service.exception.CodedException;
import com.vocnjak.service.exception.InvalidImageException;
import com.vocnjak.service.exception.OutOfBoundsException;
import com.vocnjak.web.dto.TreeForm;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orchards/{orchardId}/trees")
public class TreeController {

    private final TreeService treeService;
    private final OrchardService orchardService;
    private final TreatmentService treatmentService;
    private final MessageSource messageSource;

    public TreeController(TreeService treeService, OrchardService orchardService,
                           TreatmentService treatmentService, MessageSource messageSource) {
        this.treeService = treeService;
        this.orchardService = orchardService;
        this.treatmentService = treatmentService;
        this.messageSource = messageSource;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
    }

    @GetMapping("/new")
    public String newForm(@PathVariable Long orchardId,
                           @RequestParam int row,
                           @RequestParam int col,
                           Model model) {
        model.addAttribute("orchard", orchardService.getOrchard(orchardId));
        model.addAttribute("row", row);
        model.addAttribute("col", col);
        model.addAttribute("speciesIsOther", false);
        model.addAttribute("treeForm", new TreeForm());
        return "trees/form";
    }

    @PostMapping
    public String create(@PathVariable Long orchardId,
                          @RequestParam int row,
                          @RequestParam int col,
                          @Valid @ModelAttribute TreeForm treeForm,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        boolean speciesIsOther = isSpeciesOther(treeForm);
        validateSpeciesOther(treeForm, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("orchard", orchardService.getOrchard(orchardId));
            model.addAttribute("row", row);
            model.addAttribute("col", col);
            model.addAttribute("speciesIsOther", speciesIsOther);
            return "trees/form";
        }
        finalizeSpecies(treeForm);
        try {
            treeService.addTree(orchardId, row, col, treeForm, treeForm.getImage());
        } catch (CellOccupiedException | OutOfBoundsException | InvalidImageException e) {
            redirectAttributes.addFlashAttribute("error", resolve(e));
        }
        return "redirect:/orchards/" + orchardId;
    }

    @PostMapping("/empty")
    public String markEmpty(@PathVariable Long orchardId,
                             @RequestParam int row,
                             @RequestParam int col,
                             RedirectAttributes redirectAttributes) {
        try {
            treeService.markEmpty(orchardId, row, col);
        } catch (CellOccupiedException | OutOfBoundsException e) {
            redirectAttributes.addFlashAttribute("error", resolve(e));
        }
        return "redirect:/orchards/" + orchardId;
    }

    @GetMapping("/{treeId}")
    public String view(@PathVariable Long orchardId, @PathVariable Long treeId, Model model) {
        model.addAttribute("orchard", orchardService.getOrchard(orchardId));
        model.addAttribute("tree", treeService.getTree(treeId));
        model.addAttribute("treatments", treatmentService.listTreatments(treeId));
        return "trees/view";
    }

    @GetMapping("/{treeId}/edit")
    public String editForm(@PathVariable Long orchardId, @PathVariable Long treeId, Model model) {
        Tree tree = treeService.getTree(treeId);
        TreeForm form = toForm(tree);
        model.addAttribute("orchard", orchardService.getOrchard(orchardId));
        model.addAttribute("row", tree.getRowIndex());
        model.addAttribute("col", tree.getColIndex());
        model.addAttribute("treeId", treeId);
        model.addAttribute("hasImage", tree.hasImage());
        model.addAttribute("speciesIsOther", isSpeciesOther(form));
        model.addAttribute("treeForm", form);
        return "trees/form";
    }

    @PostMapping("/{treeId}")
    public String update(@PathVariable Long orchardId,
                          @PathVariable Long treeId,
                          @Valid @ModelAttribute TreeForm treeForm,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        boolean speciesIsOther = isSpeciesOther(treeForm);
        validateSpeciesOther(treeForm, bindingResult);
        if (bindingResult.hasErrors()) {
            Tree tree = treeService.getTree(treeId);
            model.addAttribute("orchard", orchardService.getOrchard(orchardId));
            model.addAttribute("row", tree.getRowIndex());
            model.addAttribute("col", tree.getColIndex());
            model.addAttribute("treeId", treeId);
            model.addAttribute("speciesIsOther", speciesIsOther);
            return "trees/form";
        }
        finalizeSpecies(treeForm);
        try {
            treeService.updateTree(treeId, treeForm, treeForm.getImage());
        } catch (InvalidImageException e) {
            redirectAttributes.addFlashAttribute("error", resolve(e));
            return "redirect:/orchards/" + orchardId + "/trees/" + treeId + "/edit";
        }
        return "redirect:/orchards/" + orchardId;
    }

    @PostMapping("/{treeId}/delete")
    public String delete(@PathVariable Long orchardId, @PathVariable Long treeId) {
        treeService.deleteTree(treeId);
        return "redirect:/orchards/" + orchardId;
    }

    @GetMapping("/{treeId}/image")
    @ResponseBody
    public ResponseEntity<byte[]> image(@PathVariable Long orchardId, @PathVariable Long treeId) {
        Tree tree = treeService.getTree(treeId);
        byte[] bytes = treeService.loadImage(treeId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(tree.getImageContentType()))
                .body(bytes);
    }

    private String resolve(CodedException e) {
        return messageSource.getMessage(e.getCode(), e.getArgs(), LocaleContextHolder.getLocale());
    }

    private boolean isSpeciesOther(TreeForm form) {
        return FruitSpeciesCatalog.OTHER.equals(form.getSpecies());
    }

    private void validateSpeciesOther(TreeForm form, BindingResult bindingResult) {
        if (isSpeciesOther(form) && (form.getSpeciesOther() == null || form.getSpeciesOther().isBlank())) {
            bindingResult.rejectValue("speciesOther", "required",
                    messageSource.getMessage("error.speciesOtherRequired", null, LocaleContextHolder.getLocale()));
        }
    }

    private void finalizeSpecies(TreeForm form) {
        if (isSpeciesOther(form)) {
            form.setSpecies(form.getSpeciesOther().trim());
        }
    }

    private TreeForm toForm(Tree tree) {
        TreeForm form = new TreeForm();
        if (FruitSpeciesCatalog.isCanonical(tree.getSpecies())) {
            form.setSpecies(tree.getSpecies());
        } else {
            form.setSpecies(FruitSpeciesCatalog.OTHER);
            form.setSpeciesOther(tree.getSpecies());
        }
        form.setVariety(tree.getVariety());
        form.setRootstock(tree.getRootstock());
        form.setPlantingMonth(tree.getPlantingMonth());
        form.setPlantingYear(tree.getPlantingYear());
        form.setBudBreakMonth(tree.getBudBreakMonth());
        form.setBloomMonth(tree.getBloomMonth());
        form.setFruitSetMonth(tree.getFruitSetMonth());
        form.setHarvestMonth(tree.getHarvestMonth());
        form.setPests(tree.getPests());
        form.setDiseases(tree.getDiseases());
        form.setWeatherDamage(tree.getWeatherDamage());
        form.setNutrientSymptoms(tree.getNutrientSymptoms());
        form.setTreatmentsUsed(tree.getTreatmentsUsed());
        form.setNotes(tree.getNotes());
        return form;
    }
}
