package com.verona.cafe.controller;

import com.verona.cafe.model.Customer;
import com.verona.cafe.model.Category;
import com.verona.cafe.model.MenuItem;
import com.verona.cafe.model.OrderItem;
import com.verona.cafe.model.Order;
import com.verona.cafe.model.Role;
import com.verona.cafe.model.User;
import com.verona.cafe.service.MenuService;
import com.verona.cafe.service.OrderService;
import com.verona.cafe.service.TableService;
import com.verona.cafe.service.UserService;

import com.verona.cafe.model.Attendance;
import com.verona.cafe.service.AttendanceService;
import com.verona.cafe.repository.CustomerRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final OrderService orderService;
    private final MenuService menuService;
    private final TableService tableService;
    private final UserService userService;

    private final AttendanceService attendanceService;

    private final CustomerRepository customerRepository;


    public AdminController(OrderService orderService,
                           MenuService menuService,
                           TableService tableService,
                           UserService userService,

                           AttendanceService attendanceService) {
=======
                           CustomerRepository customerRepository) {

        this.orderService = orderService;
        this.menuService = menuService;
        this.tableService = tableService;
        this.userService = userService;

        this.attendanceService = attendanceService;
        this.customerRepository = customerRepository;

    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("totalCost", orderService.getTotalCost());
        model.addAttribute("totalProfit", orderService.getTotalProfit());
        
        model.addAttribute("dailyRevenue", orderService.getDailyRevenue());
        model.addAttribute("dailyCost", orderService.getDailyCost());
        model.addAttribute("dailyProfit", orderService.getDailyProfit());
        
        model.addAttribute("tableCount", tableService.getAllTables().size());
        model.addAttribute("menuItemCount", menuService.getAllMenuItems().size());
        model.addAttribute("recentOrders", orderService.getAllCompletedOrders());

        // Construct chart data (revenue vs profit per menu item)
        List<MenuItem> items = menuService.getAllMenuItems();
        List<String> itemNames = new ArrayList<>();
        List<Double> itemRevenues = new ArrayList<>();
        List<Double> itemProfits = new ArrayList<>();
        
        for (MenuItem item : items) {
            double revenue = 0.0;
            double profit = 0.0;
            for (Order order : orderService.getAllCompletedOrders()) {
                for (OrderItem orderItem : order.getOrderItems()) {
                    if (orderItem.getMenuItem().getId().equals(item.getId())) {
                        double price = orderItem.getPrice();
                        double cost = item.getCostPrice() != null ? item.getCostPrice() : 0.0;
                        int qty = orderItem.getQuantity();
                        revenue += price * qty;
                        profit += (price - cost) * qty;
                    }
                }
            }
            if (revenue > 0) {
                itemNames.add(item.getName());
                itemRevenues.add(revenue);
                itemProfits.add(profit);
            }
        }
        
        model.addAttribute("chartLabels", itemNames);
        model.addAttribute("chartRevenues", itemRevenues);
        model.addAttribute("chartProfits", itemProfits);

        return "admin/dashboard";
    }

    @GetMapping("/menu")
    public String menuManagement(Model model) {
        model.addAttribute("categories", menuService.getAllCategories());
        model.addAttribute("menuItems", menuService.getAllMenuItems());
        model.addAttribute("newCategory", new Category());
        model.addAttribute("newMenuItem", new MenuItem());
        return "admin/menu";
    }

    @PostMapping("/menu/category/add")
    public String addCategory(@ModelAttribute Category category) {
        menuService.saveCategory(category);
        return "redirect:/admin/menu";
    }

    @GetMapping("/menu/category/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        try {
            menuService.deleteCategory(id);
        } catch (Exception e) {
            return "redirect:/admin/menu?error=categoryHasItems";
        }
        return "redirect:/admin/menu";
    }

    @PostMapping("/menu/item/add")
    public String addMenuItem(@RequestParam(required = false) Long id,
                              @RequestParam String name,
                              @RequestParam String description,
                              @RequestParam Double price,
                              @RequestParam Double costPrice,
                              @RequestParam Long categoryId,
                              @RequestParam String imageUrl) {
        try {
            Category category = menuService.getCategoryById(categoryId);
            MenuItem menuItem = MenuItem.builder()
                    .id(id)
                    .name(name)
                    .description(description)
                    .price(price)
                    .costPrice(costPrice)
                    .category(category)
                    .imageUrl(imageUrl)
                    .build();
            menuService.saveMenuItem(menuItem);
        } catch (Exception e) {
            return "redirect:/admin/menu?error=saveItemFailed";
        }
        return "redirect:/admin/menu";
    }

    @GetMapping("/menu/item/delete/{id}")
    public String deleteMenuItem(@PathVariable Long id) {
        try {
            menuService.deleteMenuItem(id);
        } catch (Exception e) {
            return "redirect:/admin/menu?error=cannotDelete";
        }
        return "redirect:/admin/menu";
    }

    @GetMapping("/staff")
    public String staffManagement(Model model) {
        model.addAttribute("staffs", userService.getAllUsers());
        model.addAttribute("newUser", new User());
        model.addAttribute("roles", Role.values());
        return "admin/staff";
    }

    @GetMapping("/attendance")
    public String attendanceOverview(Model model,
                                     @RequestParam(value = "date", required = false) String dateStr,
                                     @RequestParam(value = "userId", required = false) Long userId) {
        model.addAttribute("activePage", "attendance");
        java.time.LocalDate date = dateStr == null || dateStr.isBlank() ? java.time.LocalDate.now() : java.time.LocalDate.parse(dateStr);
        model.addAttribute("selectedDate", date.toString());
        model.addAttribute("users", userService.getAllUsers());
        if (userId != null) {
            com.verona.cafe.model.User u = userService.getAllUsers().stream().filter(x -> x.getId().equals(userId)).findFirst().orElse(null);
            model.addAttribute("attendances", u != null ? attendanceService.getByUser(u) : java.util.List.of());
        } else {
            model.addAttribute("attendances", attendanceService.getByDate(date));
        }
        return "admin/attendance";
    }

    @PostMapping("/staff/add")
    public String addStaff(@ModelAttribute User user) {
        try {
            userService.saveUser(user);
        } catch (Exception e) {
            return "redirect:/admin/staff?error=duplicateUsername";
        }
        return "redirect:/admin/staff";
    }

    @GetMapping("/staff/delete/{id}")
    public String deleteStaff(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/staff";
    }

    @GetMapping("/customers")
    public String customerManagement(Model model, @RequestParam(required = false) String search) {
        model.addAttribute("activePage", "customers");
        List<Customer> customers;
        if (search != null && !search.isBlank()) {
            customers = customerRepository.findByNameContainingIgnoreCaseOrPhoneNumberContaining(search, search);
        } else {
            customers = customerRepository.findAllByOrderByTotalSpentDesc();
        }
        model.addAttribute("customers", customers);
        model.addAttribute("search", search != null ? search : "");
        return "admin/customers";
    }
   
}
