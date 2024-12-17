package com.example.demo.repository;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    User user;
    @BeforeEach
    void setUp() {
        //user = new User(1L, "user@mail.com", "user", "1234", "NORMAL", Role.USER);
        user = new User("user", "user@mail.com", "user", "1234");
        userRepository.save(user);
    }

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
    @Order(1)
    void findByEmail() {
        User findUser = userRepository.findByEmail("user@mail.com");
        System.out.println(findUser.getNickname());

        assertThat(findUser).isNotNull();
        assertThat(findUser.getId()).isEqualTo(user.getId());
        assertThat(findUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(findUser.getNickname()).isEqualTo(user.getNickname());
        assertThat(findUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(findUser.getStatus()).isEqualTo(user.getStatus());
        assertThat(findUser.getRole()).isEqualTo(user.getRole());
    }
}