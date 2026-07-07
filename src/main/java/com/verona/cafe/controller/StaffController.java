package com.verona.cafe.controller;

import com.verona.cafe.model.*;
import com.verona.cafe.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/staff")
public class StaffController {

    private final TableService tableService;
    private final OrderService orderService;
    private final MenuService menuService;
    private final UserService userService;

    public StaffController(TableService tableService,
                           OrderService orderService,
                           MenuService menuService,
                           UserService userService) {
        this.tableService = tableService;
        this.orderService = orderService;
        this.menuService = menuService;
        this.userService = userService;
    }

    @GetMapping("/tables")
    public String viewTables(Model model) {
        model.addAttribute("tables", tableService.getAllTables());
        return "staff/tables";
    }

    @GetMapping("/pos/{tableId}")
    public String posConsole(@PathVariable Long tableId, Model model, Principal principal) {
        CafeTable table = tableService.getTableById(tableId);
        Optional<Order> activeOrder = orderService.getActiveOrderByTable(tableId);
        
        model.addAttribute("table", table);
        model.addAttribute("categories", menuService.getAllCategories());
        model.addAttribute("menuItems", menuService.getAllMenuItems());
        
        if (activeOrder.isPresent()) {
            model.addAttribute("order", activeOrder.get());
        } else {
            model.addAttribute("order", null);
        }
        
        return "staff/pos";
    }

    @PostMapping("/pos/{tableId}/add-item")
    public String addItem(@PathVariable Long tableId,
                          @RequestParam Long menuItemId,
                          @RequestParam(defaultValue = "1") int quantity,
                          Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        orderService.addItemToOrder(tableId, menuItemId, quantity, user);
        return "redirect:/staff/pos/" + tableId;
    }

    @PostMapping("/pos/{tableId}/update-item")
    public String updateItemQuantity(@PathVariable Long tableId,
                                     @RequestParam Long menuItemId,
                                     @RequestParam int quantity) {
        orderService.updateItemQuantity(tableId, menuItemId, quantity);
        return "redirect:/staff/pos/" + tableId;
    }

    @GetMapping("/pos/{tableId}/remove-item/{menuItemId}")
    public String removeItem(@PathVariable Long tableId, @PathVariable Long menuItemId) {
        orderService.removeItemFromOrder(tableId, menuItemId);
        return "redirect:/staff/pos/" + tableId;
    }

    @PostMapping("/pos/{tableId}/checkout")
    public String checkout(@PathVariable Long tableId,
                           @RequestParam Double amountPaid,
                           @RequestParam String paymentMethod) {
        Order order = orderService.completeOrder(tableId, amountPaid, paymentMethod);
        return "redirect:/staff/order/" + order.getId() + "/receipt";
    }

    @GetMapping("/pos/{tableId}/cancel")
    public String cancelOrder(@PathVariable Long tableId) {
        orderService.cancelOrder(tableId);
        return "redirect:/staff/tables";
    }

    @GetMapping("/order/{orderId}/receipt")
    public String showReceipt(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        return "staff/receipt";
    }
}
