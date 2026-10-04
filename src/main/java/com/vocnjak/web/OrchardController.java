package com.vocnjak.web;

import com.vocnjak.domain.Orchard;
import com.vocnjak.service.OrchardService;
import com.vocnjak.service.TreeService;
import com.vocnjak.service.exception.OutOfBoundsException;
import com.vocnjak.web.dto.OrchardCreateForm;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrchardController {

    private final OrchardService orchardService;
    private final TreeService treeService;
    private final MessageSource messageSource;

    public OrchardController(OrchardService orchardService, TreeService treeService, MessageSource messageSource) {
        this.orchardService = orchardService;
        this.treeService = treeService;
        this.messageSource = messageSource;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/orchards";
    }

    @GetMapping("/orchards")
    public String list(Model model) {
        model.addAttribute("orchards", orchardService.listOrchards());
        return "orchards/list";
    }

    @GetMapping("/orchards/new")
    public String newForm(Model model) {
        model.addAttribute("orchardCreateForm", new OrchardCreateForm());
        return "orchards/new";
    }

    @PostMapping("/orchards")
    public String create(@Valid @ModelAttribute OrchardCreateForm orchardCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "orchards/new";
        }
        Orchard orchard = orchardService.createOrchard(
                orchardCreateForm.getName(), orchardCreateForm.getRows(), orchardCreateForm.getColumns(),
                orchardCreateForm.getOrientationNote());
        return "redirect:/orchards/" + orchard.getId();
    }

    @GetMapping("/orchards/{id}")
    public String viewGrid(@PathVariable Long id, Model model) {
        Orchard orchard = orchardService.getOrchard(id);
        model.addAttribute("orchard", orchard);
        model.addAttribute("grid", treeService.buildGrid(orchard));
        return "orchards/grid";
    }

    @PostMapping("/orchards/{id}/delete")
    public String deleteOrchard(@PathVariable Long id) {
        orchardService.deleteOrchard(id);
        return "redirect:/orchards";
    }

    @PostMapping("/orchards/{id}/rows")
    public String addRow(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orchardService.addRow(id);
        } catch (OutOfBoundsException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage(e.getCode(), e.getArgs(), LocaleContextHolder.getLocale()));
        }
        return "redirect:/orchards/" + id;
    }

    @PostMapping("/orchards/{id}/columns")
    public String addColumn(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orchardService.addColumn(id);
        } catch (OutOfBoundsException e) {
            redirectAttributes.addFlashAttribute("error",
                    messageSource.getMessage(e.getCode(), e.getArgs(), LocaleContextHolder.getLocale()));
        }
        return "redirect:/orchards/" + id;
    }
}