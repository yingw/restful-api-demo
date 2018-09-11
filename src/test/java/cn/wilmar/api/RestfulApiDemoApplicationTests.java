package cn.wilmar.api;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestfulApiDemoApplicationTests {

    Logger logger = LoggerFactory.getLogger(RestfulApiDemoApplicationTests.class);

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Autowired TestRestTemplate restTemplate;

//    private final

    @Before
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void testUsers() {
        ResponseEntity<List> entity = this.restTemplate.getForEntity("/api/users", List.class);
        assertThat(entity.getStatusCode().equals(HttpStatus.OK));
        logger.info("Body = " + entity.getBody().toString());

        entity.getHeaders().forEach((s, strings) -> logger.debug("key: " + s + ", value: " + strings));

        assertThat(entity.getBody().toString().contains("yinguowei"));
    }

    @Test
    public void testSaveUser() {
        User newUser = new User("Test user", "test", "test@example.com");
        ResponseEntity<User> entity = this.restTemplate.postForEntity("/api/users", newUser, User.class);
        assertThat(entity.getStatusCode().equals(HttpStatus.CREATED));

        logger.info("Body = " + entity.getBody().toString());

        assertThat(entity.getBody().getLogin().equals("test"));
    }

}
