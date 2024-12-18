package com.example.demo.repository;

import com.example.demo.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

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
    @DisplayName("이메일로 유저 검색 시 존재하는 유저 반환")
    void findByEmail() {
        // Given
        User user = new User("user", "user@mail.com", "user", "1234");
        User savedUser = userRepository.save(user);

        // When
        User findUser = userRepository.findByEmail("user@mail.com");

        // Then
        assertThat(findUser).isSameAs(savedUser);
    }
}