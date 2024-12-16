package com.example.demo.entity;

import com.example.demo.repository.ItemRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ItemTest {
    @Autowired
    ItemRepository itemRepository;

    @TestConfiguration
    static class QueryDslTestConfig {
        @Autowired
        private EntityManager entityManager;

        @Bean
        public JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(entityManager);
        }
    }

    @Test
    @DisplayName("아이템 생성 테스트")
    void crateItemTest() {
        // Given
        User owner = new User("user", "owner@gmail.com", "owner", "1234"); // 가상의 User 객체
        User manager = new User("user", "manager@gmail.com", "manager", "1234"); // 가상의 User 객체
        Item item = new Item("name", "descripton", owner, manager);

        // When
        itemRepository.save(item); //Item 엔티티의 @DynamicInsert를 지우면 컬럼 제약조건인 nullable = false에 대한 예외 출력
        Item findItem = itemRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("해당 아이템이 없음"));

        // Then
        System.out.println(findItem.getStatus());
        assertThat(findItem.getStatus()).isNull();
    }
}