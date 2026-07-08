package com.verona.cafe.controller;

import com.verona.cafe.service.MenuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PublicController {

    private final MenuService menuService;

    public PublicController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("activePage", "home");
        model.addAttribute("categories", menuService.getAllCategories());
        model.addAttribute("menuItems", menuService.getAllMenuItems());
        return "public/home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("activePage", "about");
        return "public/about";
    }

    @GetMapping("/menu")
    public String menu(Model model, @RequestParam(value = "search", required = false) String search) {
        model.addAttribute("activePage", "menu");
        model.addAttribute("categories", menuService.getAllCategories());
        model.addAttribute("menuItems", menuService.searchMenuItems(search));
        model.addAttribute("search", search);
        return "public/menu";
    }

    @GetMapping("/news")
    public String news(Model model) {
        model.addAttribute("activePage", "news");
        return "public/news";
    }

    @GetMapping("/services")
    public String services(Model model) {
        model.addAttribute("activePage", "services");
        return "public/services";
    }

    @GetMapping("/recruitment")
    public String recruitment(Model model) {
        model.addAttribute("activePage", "recruitment");
        return "public/recruitment";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("activePage", "contact");
        return "public/contact";
    }
}
