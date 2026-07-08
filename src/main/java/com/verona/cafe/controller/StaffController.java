package com.verona.cafe.controller;

import com.verona.cafe.model.*;
import com.verona.cafe.service.*;
import com.verona.cafe.repository.CustomerRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/staff")
public class StaffController {

    private final TableService tableService;
    private final OrderService orderService;
    private final MenuService menuService;
    private final UserService userService;
    private final CustomerRepository customerRepository;

    public StaffController(TableService tableService,
                           OrderService orderService,
                           MenuService menuService,
                           UserService userService,
                           CustomerRepository customerRepository) {
        this.tableService = tableService;
        this.orderService = orderService;
        this.menuService = menuService;
        this.userService = userService;
        this.customerRepository = customerRepository;
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

    @PostMapping("/pos/{tableId}/start")
    public String startOrder(@PathVariable Long tableId,
                             @RequestParam String customerPhone,
                             @RequestParam String customerName,
                             Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        orderService.startOrderWithCustomer(tableId, customerPhone, customerName, user);
        return "redirect:/staff/pos/" + tableId;
    }

    @GetMapping("/api/customers/lookup")
    @ResponseBody
    public java.util.Map<String, String> lookupCustomer(@RequestParam String phone) {
        java.util.Map<String, String> response = new java.util.HashMap<>();
        customerRepository.findByPhoneNumber(phone).ifPresentOrElse(
            c -> {
                response.put("name", c.getName());
                response.put("found", "true");
            },
            () -> {
                response.put("found", "false");
            }
        );
        return response;
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

    @GetMapping("/history")
    public String viewOrderHistory(Model model,
                                   @RequestParam(value = "date", required = false) String date) {
        model.addAttribute("activePage", "history");
        if (date != null && !date.isBlank()) {
            model.addAttribute("orders", orderService.getCompletedOrdersByDate(LocalDate.parse(date)));
            model.addAttribute("selectedDate", date);
        } else {
            model.addAttribute("orders", orderService.getAllCompletedOrders());
            model.addAttribute("selectedDate", "");
        }
        return "staff/history";
    }
}
