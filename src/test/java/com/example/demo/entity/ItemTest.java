package com.example.demo.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void crateItemTest() {
        // Given
        User owner = new User(); // 가상의 User 객체
        User manager = new User(); // 가상의 User 객체

        // When
        Item item = new Item("name", "descripton", owner, manager);
        System.out.println(item.getStatus());
        //item.setStatus("TestStatus");

        // Then
        assertNotNull(item.getStatus(), "아이템 상태가 null");
    }
}