package org.com.backend.controller;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JsonParserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @MethodSource("getFilePath")
    void parserTest(String fileName, String path) throws Exception {
        List<String> jsonLines = Files.readAllLines(Path.of(path));
        String json = String.join("\n", jsonLines);

        String expectedValue = fileName.startsWith("fail") ? "false" : "true";

        mockMvc.perform(post("/parser")
                        .content(json)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(expectedValue));
    }

    private static Stream<Arguments> getFilePath() {
        return JsonParserControllerTest.extractFilePaths()
                .entrySet()
                .stream()
                .map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
    }

    private static Map<String, String> extractFilePaths() {
        File folder = new File("/Users/shenba/Documents/workspace/jsonParser/backend/src/test/resources/samples");
        File[] listOfFiles = folder.listFiles();

        if(nonNull(listOfFiles)) {
            return Arrays.stream(listOfFiles)
                    .filter(File::isFile)
                    .collect(Collectors.toMap(
                            File::getName,
                            File::getPath
                    ));
        } else {
            return Collections.emptyMap();
        }
    }
}
