package com.verona.cafe.controller;

import com.verona.cafe.model.*;
import com.verona.cafe.service.*;
import com.verona.cafe.model.Shift;
import com.verona.cafe.model.Attendance;
import com.verona.cafe.service.ShiftService;
import com.verona.cafe.service.AttendanceService;
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
    private final ShiftService shiftService;
    private final AttendanceService attendanceService;

    public StaffController(TableService tableService,
                           OrderService orderService,
                           MenuService menuService,
                           UserService userService,
                           ShiftService shiftService,
                           AttendanceService attendanceService) {
        this.tableService = tableService;
        this.orderService = orderService;
        this.menuService = menuService;
        this.userService = userService;
        this.shiftService = shiftService;
        this.attendanceService = attendanceService;
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

    @GetMapping("/shifts")
    public String viewShifts(Model model) {
        model.addAttribute("activePage", "shifts");
        model.addAttribute("shifts", shiftService.getAllShifts());
        model.addAttribute("users", userService.getAllUsers());
        return "staff/shifts";
    }

    @GetMapping("/shifts/new")
    public String newShiftForm(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("shift", new Shift());
        return "staff/shifts";
    }

    @PostMapping("/shifts")
    public String saveShift(@ModelAttribute Shift shift) {
        shiftService.saveShift(shift);
        return "redirect:/staff/shifts";
    }

    @GetMapping("/shifts/{id}/delete")
    public String deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return "redirect:/staff/shifts";
    }

    @GetMapping("/attendance")
    public String viewAttendance(Model model, Principal principal,
                                 @RequestParam(value = "date", required = false) String dateStr) {
        model.addAttribute("activePage", "attendance");
        String username = principal.getName();
        com.verona.cafe.model.User user = userService.getUserByUsername(username);
        java.time.LocalDate date = dateStr == null || dateStr.isBlank() ? java.time.LocalDate.now() : java.time.LocalDate.parse(dateStr);
        Attendance att = attendanceService.getByUserAndDate(user, date);
        model.addAttribute("attendance", att);
        model.addAttribute("attendances", attendanceService.getByUser(user));
        model.addAttribute("selectedDate", date.toString());
        return "staff/attendance";
    }

    @PostMapping("/attendance/clockin")
    public String clockIn(Principal principal) {
        String username = principal.getName();
        com.verona.cafe.model.User user = userService.getUserByUsername(username);
        java.time.LocalDate today = java.time.LocalDate.now();
        Attendance att = attendanceService.getByUserAndDate(user, today);
        if (att == null) {
            att = Attendance.builder()
                    .user(user)
                    .date(today)
                    .clockIn(java.time.LocalDateTime.now())
                    .build();
        } else {
            att.setClockIn(java.time.LocalDateTime.now());
        }
        attendanceService.save(att);
        return "redirect:/staff/attendance";
    }

    @PostMapping("/attendance/clockout")
    public String clockOut(Principal principal) {
        String username = principal.getName();
        com.verona.cafe.model.User user = userService.getUserByUsername(username);
        java.time.LocalDate today = java.time.LocalDate.now();
        Attendance att = attendanceService.getByUserAndDate(user, today);
        if (att == null) {
            att = Attendance.builder()
                    .user(user)
                    .date(today)
                    .clockOut(java.time.LocalDateTime.now())
                    .build();
        } else {
            att.setClockOut(java.time.LocalDateTime.now());
        }
        attendanceService.save(att);
        return "redirect:/staff/attendance";
    }
}
