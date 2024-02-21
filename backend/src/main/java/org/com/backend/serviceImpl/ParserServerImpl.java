package org.com.backend.serviceImpl;

import org.com.backend.model.Token;
import org.com.backend.model.TokenType;
import org.com.backend.model.ast.*;
import org.com.backend.service.ParserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParserServerImpl implements ParserService {

    @Override
    public ASTNode parsedJSON(List<Token> tokens) {
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Nothing to parse");
        }
        return parseValue(tokens, 0);
    }

    private ASTNode parseValue(List<Token> tokens, int index) {
        Token token = tokens.get(index);
        switch(token.getTokenType()) {
            case STRING -> {
                return new StringASTNode(token.getValue());
            }
            case NUMBER -> {
                return new NumberASTNode(BigDecimal.valueOf(Long.parseLong(token.getValue())));
            }
            case NULL -> {
                return new NullASTNode();
            }
            case TRUE -> {
                return new BooleanASTNode(true);
            }
            case FALSE -> {
                return new BooleanASTNode(false);
            }
            case BRACE_OPEN -> {
                return parseObject(tokens, ++index);
            }
            case BRACKET_OPEN -> {
                return parseArray(tokens, ++index);
            }
            default -> throw new IllegalArgumentException("Unexpected token: " + token.getValue());
        }
    }

    private ObjectASTNode parseObject(List<Token> tokens, int index) {
        Map<String, ASTNode> nodeMap = new HashMap<>();
        Token token = tokens.get(index);
        while (token.getTokenType() != TokenType.BRACE_CLOSE) {
            if (token.getTokenType() == TokenType.STRING) {
                String key = token.getValue();
                token = tokens.get(++index);
                if (token.getTokenType() != TokenType.COLON) {
                    throw new IllegalArgumentException(" Invalid JSON! Expected key-value format");
                }
                ASTNode valueNode = parseValue(tokens, ++index);
                nodeMap.put(key, valueNode);
            } else {
                throw new IllegalArgumentException("Valid JSON should have a string as key");
            }
            token = tokens.get(++index);
            if (token.getTokenType() == TokenType.COMMA) {
                token = tokens.get(++index);
                if (token.getTokenType() == TokenType.BRACE_CLOSE) {
                    throw new IllegalArgumentException("Unexpected token: " + token.getValue());
                }
            }
        }
        return new ObjectASTNode(nodeMap);
    }

    private ArrayASTNode parseArray(List<Token> tokens, int index) {
        List<ASTNode> nodes = new ArrayList<>();
        Token token = tokens.get(index);
        while (token.getTokenType() != TokenType.BRACKET_CLOSE) {
            ASTNode node = parseValue(tokens, index);
            nodes.add(node);
            token = tokens.get(++index);
            if (token.getTokenType() == TokenType.COMMA) {
                token = tokens.get(++index);
                if (token.getTokenType() == TokenType.BRACKET_CLOSE) {
                    throw new IllegalArgumentException("Unexpected token: " + token.getValue());
                }
            }
        }
        return new ArrayASTNode(nodes);
    }
}
