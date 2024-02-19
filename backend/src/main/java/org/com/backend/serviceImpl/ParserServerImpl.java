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
    private int index;
    private final List<Token> allTokens = new ArrayList<>();

    @Override
    public ASTNode parsedJSON(List<Token> tokens) {
        index = 0;
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Nothing to parse");
        }
        allTokens.addAll(tokens);
        return parseValue(tokens.get(index));
    }

    private ASTNode parseValue(Token token) {
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
                return parseObject(nextToken());
            }
            case BRACKET_OPEN -> {
                return parseArray(nextToken());
            }
            default -> throw new IllegalArgumentException("Unexpected token: " + token.getValue());
        }
    }

    private ObjectASTNode parseObject(Token token) {
        Map<String, ASTNode> nodeMap = new HashMap<>();
        while (token.getTokenType() != TokenType.BRACE_CLOSE) {
            if (token.getTokenType() == TokenType.STRING) {
                String key = token.getValue();
                token = nextToken();
                if (token.getTokenType() != TokenType.COLON) {
                    throw new IllegalArgumentException(" Invalid JSON! Expected key-value format");
                }
                token = nextToken();
                ASTNode valueNode = parseValue(token);
                nodeMap.put(key, valueNode);
            } else {
                throw new IllegalArgumentException("Valid JSON should have a string as key");
            }
            token = nextToken();
            if (token.getTokenType() == TokenType.COMMA) {
                token = nextToken();
            }
        }
        return new ObjectASTNode(nodeMap);
    }

    private ArrayASTNode parseArray(Token token) {
        List<ASTNode> nodes = new ArrayList<>();
        while (token.getTokenType() != TokenType.BRACKET_CLOSE) {
            ASTNode node = parseValue(token);
            nodes.add(node);
            token = nextToken();
            if (token.getTokenType() == TokenType.COMMA) {
                token = nextToken();
            }
        }
        return new ArrayASTNode(nodes);
    }

    private Token nextToken() {
        if (index < allTokens.size()) {
            return allTokens.get(++index);
        } else {
            throw new IndexOutOfBoundsException("Invalid JSON!");
        }
    }
}
