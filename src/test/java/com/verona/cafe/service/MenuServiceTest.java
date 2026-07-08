package com.verona.cafe.service;

import com.verona.cafe.model.MenuItem;
import com.verona.cafe.repository.CategoryRepository;
import com.verona.cafe.repository.MenuItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    void searchMenuItemsShouldReturnMatchingItemsIgnoringCase() {
        MenuItem coffee = MenuItem.builder()
                .id(1L)
                .name("Cappuccino")
                .description("Classic coffee with milk")
                .build();

        when(menuItemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("coffee", "coffee"))
                .thenReturn(List.of(coffee));

        List<MenuItem> result = menuService.searchMenuItems("coffee");

        assertThat(result).containsExactly(coffee);
        verify(menuItemRepository).findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("coffee", "coffee");
    }
}
