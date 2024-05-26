package com.study.scalable.crud.app;

import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.testcontainers.containers.PostgreSQLContainer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ScalableCrudServiceTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private UserController userController;

    @Autowired
    UserRepository userRepository;

    @ClassRule
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres");

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());
    }

    @BeforeEach
    public void setUp(){
        userRepository.deleteAll(); // Clear any existing data before each test
    }

    @Test
    void testContextLoads() {
    }

    @Test
    void testUserController() throws Exception {
        // mockMvc ...
        // Create some test users
        mockMvc.perform(post("/users")
                        .content("{\"name\": \"John Doe\", \"birthdate\":\"1995-01-03\", \"profession\":\"Software Engineer\"}")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andDo(log())
                .andReturn();

        mockMvc.perform(post("/users")
                        .content("{\"name\": \"Jane Din\", \"birthdate\":\"\", \"profession\":\"Doctor\"}")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andDo(log())
                .andReturn();

        // Perform GET request for all users
        MvcResult mvcResult = mockMvc.perform(get("/users").contentType("application/json"))
                .andExpect(status().isOk())
                .andDo(log())
                .andReturn();

        // Parse response as JSON string
        String responseJson = mvcResult.getResponse().getContentAsString();
        assertTrue(responseJson.contains("John") && responseJson.contains("Jane"));
    }

    @Test
    void testUserRepository(){
        User user = new User();
        user.setName("Bob Dylan");
        user.setProfession("Developer");
        user.setBirthdate(getDate("1995-02-09"));

        user = userRepository.save(user);
        assertNotNull(user);

        user.setProfession("Software Developer");
        userRepository.save(user);

        Optional<User> updatedUser = userRepository.findById(user.getId());
        assertTrue(updatedUser.isPresent());
        assertEquals("Software Developer", updatedUser.get().getProfession());

        userRepository.delete(user);
        Optional<User> noUser = userRepository.findById(user.getId());
        assertTrue(noUser.isEmpty());
    }

    private LocalDate getDate(String date){
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
