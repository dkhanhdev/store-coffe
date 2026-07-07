package com.verona.cafe.config;

import com.verona.cafe.model.*;
import com.verona.cafe.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final CafeTableRepository cafeTableRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           MenuItemRepository menuItemRepository,
                           CafeTableRepository cafeTableRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
        this.cafeTableRepository = cafeTableRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Users
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Chủ quán Verona")
                    .role(Role.ADMIN)
                    .build();

            User staff = User.builder()
                    .username("staff")
                    .password(passwordEncoder.encode("staff123"))
                    .fullName("Thu ngân Maria")
                    .role(Role.STAFF)
                    .build();

            userRepository.saveAll(Arrays.asList(admin, staff));
        }

        // 2. Seed Categories
        Category coffee = null;
        Category food = null;
        Category bakery = null;

        if (categoryRepository.count() == 0) {
            coffee = Category.builder().name("Cà Phê & Trà").description("Các món cà phê và trà được làm thủ công").build();
            food = Category.builder().name("Món Ăn Chính").description("Các món ăn no ngon miệng và bổ dưỡng").build();
            bakery = Category.builder().name("Bánh Ngọt & Pizza").description("Bánh ngọt tươi và các loại pizza thơm ngon").build();

            categoryRepository.saveAll(Arrays.asList(coffee, food, bakery));
        } else {
            coffee = categoryRepository.findByName("Cà Phê & Trà").orElse(null);
            food = categoryRepository.findByName("Món Ăn Chính").orElse(null);
            bakery = categoryRepository.findByName("Bánh Ngọt & Pizza").orElse(null);
        }

        // 3. Seed Menu Items
        if (menuItemRepository.count() == 0 && coffee != null && food != null && bakery != null) {
            MenuItem columbia = MenuItem.builder()
                    .name("Cà Phê Sữa Đá Verona")
                    .description("Cà phê robusta đậm đà kết hợp sữa đặc hảo hạng")
                    .price(5.10)
                    .costPrice(2.00)
                    .category(coffee)
                    .imageUrl("/images/columbia.jpg")
                    .build();

            MenuItem decafPike = MenuItem.builder()
                    .name("Cà Phê Đen Nóng")
                    .description("Cà phê pha phin truyền thống thơm nồng")
                    .price(5.00)
                    .costPrice(1.80)
                    .category(coffee)
                    .imageUrl("/images/pike.jpg")
                    .build();

            MenuItem decafVerona = MenuItem.builder()
                    .name("Bạc Xỉu Sữa Nóng")
                    .description("Cà phê sữa nhiều sữa nóng hổi béo ngậy")
                    .price(3.80)
                    .costPrice(1.50)
                    .category(coffee)
                    .imageUrl("/images/veronika.jpg")
                    .build();

            MenuItem chicken = MenuItem.builder()
                    .name("Ức Gà Áp Chảo Sốt Bơ")
                    .description("Ức gà hữu cơ mềm mọng ăn kèm sốt bơ tỏi thơm lừng")
                    .price(22.00)
                    .costPrice(10.00)
                    .category(food)
                    .imageUrl("/images/chicken.jpg")
                    .build();

            MenuItem fishBrochette = MenuItem.builder()
                    .name("Cá Hồi Nướng Rau Củ")
                    .description("Cá hồi phi lê nướng kèm măng tây và cà rốt")
                    .price(8.50)
                    .costPrice(4.50)
                    .category(food)
                    .imageUrl("/images/fish.jpg")
                    .build();

            MenuItem comboPizza = MenuItem.builder()
                    .name("Pizza Thập Cẩm Đặc Biệt")
                    .description("Đế pizza giòn rụm với salami, pepperoni và phô mai mozzarella")
                    .price(8.90)
                    .costPrice(4.00)
                    .category(bakery)
                    .imageUrl("/images/pizza.jpg")
                    .build();

            menuItemRepository.saveAll(Arrays.asList(columbia, decafPike, decafVerona, chicken, fishBrochette, comboPizza));
        }

        // 4. Seed Cafe Tables
        if (cafeTableRepository.count() == 0) {
            CafeTable table1 = CafeTable.builder().tableNumber("T-01").seatCount(2).status(TableStatus.AVAILABLE).build();
            CafeTable table2 = CafeTable.builder().tableNumber("T-02").seatCount(4).status(TableStatus.AVAILABLE).build();
            CafeTable table3 = CafeTable.builder().tableNumber("T-03").seatCount(2).status(TableStatus.AVAILABLE).build();
            CafeTable table4 = CafeTable.builder().tableNumber("T-04").seatCount(6).status(TableStatus.AVAILABLE).build();
            CafeTable table5 = CafeTable.builder().tableNumber("T-05").seatCount(4).status(TableStatus.AVAILABLE).build();
            CafeTable table6 = CafeTable.builder().tableNumber("T-06").seatCount(2).status(TableStatus.AVAILABLE).build();
            CafeTable table7 = CafeTable.builder().tableNumber("T-07").seatCount(4).status(TableStatus.AVAILABLE).build();
            CafeTable table8 = CafeTable.builder().tableNumber("T-08").seatCount(8).status(TableStatus.AVAILABLE).build();

            cafeTableRepository.saveAll(Arrays.asList(table1, table2, table3, table4, table5, table6, table7, table8));
        }
    }
}
