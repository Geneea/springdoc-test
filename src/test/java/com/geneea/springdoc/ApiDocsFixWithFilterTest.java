package com.geneea.springdoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("reformat_apidocs_filter")
@Slf4j
public class ApiDocsFixWithFilterTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void whenReformatingTheJSONResponse_thenApiDocsReturnsCorrectJSON() throws Exception {

        String apiDocs = restTemplate.getForObject("http://localhost:" + port + "/api-docs", String.class);
        log.info("The api-docs response is:\n" + apiDocs);
        Map docs = OBJECT_MAPPER.readValue(apiDocs, Map.class);
        assertEquals("3.0.1", docs.get("openapi"), "field 'openapi' is not present");
    }
}