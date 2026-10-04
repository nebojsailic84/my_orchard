package com.vocnjak.web;

import com.vocnjak.service.TreatmentService;
import com.vocnjak.service.TreeService;
import com.vocnjak.web.dto.TreatmentForm;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orchards/{orchardId}/trees/{treeId}/treatments")
public class TreatmentController {

    private final TreatmentService treatmentService;
    private final TreeService treeService;

    public TreatmentController(TreatmentService treatmentService, TreeService treeService) {
        this.treatmentService = treatmentService;
        this.treeService = treeService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
    }

    @GetMapping("/new")
    public String newForm(@PathVariable Long orchardId, @PathVariable Long treeId, Model model) {
        model.addAttribute("orchardId", orchardId);
        model.addAttribute("tree", treeService.getTree(treeId));
        model.addAttribute("treatmentForm", new TreatmentForm());
        return "treatments/form";
    }

    @PostMapping
    public String create(@PathVariable Long orchardId,
                          @PathVariable Long treeId,
                          @Valid @ModelAttribute TreatmentForm treatmentForm,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("orchardId", orchardId);
            model.addAttribute("tree", treeService.getTree(treeId));
            return "treatments/form";
        }
        treatmentService.addTreatment(treeId, treatmentForm);
        return "redirect:/orchards/" + orchardId + "/trees/" + treeId;
    }

    @PostMapping("/{treatmentId}/delete")
    public String delete(@PathVariable Long orchardId, @PathVariable Long treeId, @PathVariable Long treatmentId) {
        treatmentService.deleteTreatment(treatmentId);
        return "redirect:/orchards/" + orchardId + "/trees/" + treeId;
    }
}
