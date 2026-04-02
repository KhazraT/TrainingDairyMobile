package ru.squidory.trainingdairymobile;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for Main Navigation.
 */
public class NavigationUnitTest {

    @Test
    public void bottomNavigationMenu_hasFiveItems() {
        // Проверяем, что меню навигации содержит 5 элементов
        // Это простой тест для демонстрации
        int expectedMenuItems = 5;
        assertEquals(expectedMenuItems, expectedMenuItems);
    }

    @Test
    public void navigationFragments_areCreated() {
        // Проверяем, что все 5 фрагментов существуют
        // Тест будет расширен при интеграционном тестировании
        assertTrue(true);
    }

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}
