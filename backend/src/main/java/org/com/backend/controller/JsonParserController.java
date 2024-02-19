package org.com.backend.controller;

import org.com.backend.model.Token;
import org.com.backend.model.ast.ASTNode;
import org.com.backend.service.ParserService;
import org.com.backend.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("parser")
public class JsonParserController {
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ParserService parserService;

    @PostMapping()
    public boolean isJsonValid(@RequestBody String json) {
        try {
            List<Token> tokens  = tokenService.tokenize(json);
            ASTNode node = parserService.parsedJSON(tokens);
            System.out.println(node);
            return true;
        } catch (UnsupportedOperationException | IllegalArgumentException | IndexOutOfBoundsException exception) {
            System.err.println(exception.getMessage());
            return false;
        }

    }
}
