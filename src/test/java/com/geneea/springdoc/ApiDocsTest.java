package com.geneea.springdoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class ApiDocsTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void whenMessageConverterIsAdded_thenApiDocsReturnMalformedJSON() throws Exception {

        String apiDocs = restTemplate.getForObject("http://localhost:" + port + "/api-docs", String.class);
        log.info("The api-docs response is:\n" + apiDocs);
        assertTrue(apiDocs.startsWith("\"{\\\"openapi\\\":"));
        assertThrows(MismatchedInputException.class, () -> OBJECT_MAPPER.readValue(apiDocs, Map.class));
    }
}