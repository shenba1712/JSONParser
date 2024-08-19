package org.com.backend.serviceImpl;

import org.com.backend.model.EscapeSequence;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;

@Service
public class ParserServerImpl implements ParserService {

    private static final int NESTING_LEVEL_LIMIT = 20;

    @Override
    public ASTNode parsedJSON(List<Token> tokens) {
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Nothing to parse");
        }
        // Keeps track of the token position. Mainly used to handle some tokens like comma, colon, braces, brackets, etc.
        AtomicInteger index = new AtomicInteger(0);
        // used to keep track of the nesting level. JSON doesn't permit infinite nesting as it possess security risks.
        // So, we allow a max of 20 nesting levels, beyond which an error is thrown.
        AtomicInteger nestingLevel = new AtomicInteger(0);
        ASTNode node = parseValue(tokens, index, nestingLevel);
        if (index.get() < (tokens.size()-1)) {
            throw new IllegalArgumentException("Malformed JSON string. Unexpected token: " + tokens.get(index.get() + 1).getValue());
        }
        return node;
    }

    private ASTNode parseValue(List<Token> tokens, AtomicInteger index, AtomicInteger nestingLevel) {
        if (nestingLevel.get() == NESTING_LEVEL_LIMIT) {
            throw new IllegalArgumentException("Nesting is too deep!");
        }
        Token token = tokens.get(index.get());
        switch(token.getTokenType()) {
            case STRING -> {
                if(PatternHelperService.containsLineBreaks(token.getValue())) {
                    throw new UnsupportedOperationException("JSON cannot have line breaks");
                } else if (PatternHelperService.containsTabs(token.getValue())) {
                    throw new UnsupportedOperationException("JSON cannot have tab spaces");
                } else if (containsInvalidBackslashes(token.getValue())) {
                    throw new UnsupportedOperationException("Values cannot have backslashes.");
                } else {
                    return new StringASTNode(token.getValue());
                }
            }
            case NUMBER -> {
                return new NumberASTNode(new BigDecimal(token.getValue()));
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
                nestingLevel.set(nestingLevel.incrementAndGet());
                index.set(index.incrementAndGet());
                return parseObject(tokens, index, nestingLevel);
            }
            case BRACKET_OPEN -> {
                nestingLevel.set(nestingLevel.incrementAndGet());
                index.set(index.incrementAndGet());
                return parseArray(tokens, index, nestingLevel);
            }
            default -> throw new IllegalArgumentException("Unexpected token: " + token.getValue());
        }
    }

    private ObjectASTNode parseObject(List<Token> tokens, AtomicInteger index, AtomicInteger nestingLevel) {
        try {
            Map<String, ASTNode> nodeMap = new HashMap<>();
            Token token = tokens.get(index.get());
            while (token.getTokenType() != TokenType.BRACE_CLOSE) {
                if (token.getTokenType() == TokenType.STRING) {
                    String key = token.getValue();
                    token = tokens.get(index.incrementAndGet());
                    if (token.getTokenType() != TokenType.COLON) {
                        throw new IllegalArgumentException("Invalid JSON! Expected key-value format");
                    }
                    index.set(index.incrementAndGet());
                    ASTNode valueNode = parseValue(tokens, index, nestingLevel);
                    nodeMap.put(key, valueNode);
                } else {
                    throw new IllegalArgumentException("Valid JSON should have a string as key");
                }
                token = tokens.get(index.incrementAndGet());
                if (token.getTokenType() == TokenType.COMMA) {
                    token = tokens.get(index.incrementAndGet());
                    if (token.getTokenType() == TokenType.BRACE_CLOSE) {
                        throw new IllegalArgumentException("Unexpected token: " + token.getValue());
                    }
                }
            }
            return new ObjectASTNode(nodeMap);
        } catch (IndexOutOfBoundsException exception) {
            throw new IllegalArgumentException("Malformed JSON. Cannot parse string as it is not properly closed.");
        } catch (Exception exception) {
            throw new IllegalArgumentException("Malformed JSON. Cannot parse string. " + exception.getMessage());
        }
    }

    private ArrayASTNode parseArray(List<Token> tokens, AtomicInteger index, AtomicInteger nestingLevel) {
        try {
            List<ASTNode> nodes = new ArrayList<>();
            Token token = tokens.get(index.get());
            while (token.getTokenType() != TokenType.BRACKET_CLOSE) {
                ASTNode node = parseValue(tokens, index, nestingLevel);
                nodes.add(node);
                token = tokens.get(index.incrementAndGet());
                if (token.getTokenType() == TokenType.COMMA) {
                    token = tokens.get(index.incrementAndGet());
                    if (token.getTokenType() == TokenType.BRACKET_CLOSE) {
                        throw new IllegalArgumentException("Unexpected token: " + token.getValue());
                    }
                }
            }
            return new ArrayASTNode(nodes);
        } catch (IndexOutOfBoundsException exception) {
            throw new IllegalArgumentException("Malformed JSON. Cannot parse string as it is not properly closed.");
        } catch (Exception exception) {
            throw new IllegalArgumentException("Malformed JSON. Cannot parse string. " + exception.getMessage());
        }
    }

    // JSON allows even numbered backslashes.
    // JSON allows valid escape sequences, unicode and octal values.
    // So, throw error only for invalid strings.
    private boolean containsInvalidBackslashes(String value) {
        boolean isEvenSlashes = value.matches(PatternHelperService.evenSlashesPatternString());
        if (!isEvenSlashes) {
            // remove all double backslashes, leaving only the single backslashes to check for escape sequences
            // remove all escape sequences, leaving only unicode, octal, and invalid sequences
            String tempValue = value.replaceAll(PatternHelperService.doubleSlashesPatternString(), "")
                    .replaceAll(EscapeSequence.getEscapeSequencesAsString(), "");

            // remove all unicode values
            List<String> unicodeMatcherResult = PatternHelperService.getUnicodeEscapeSequencePattern()
                    .matcher(tempValue)
                    .results()
                    .map(MatchResult::group)
                    .toList();
            if (!unicodeMatcherResult.isEmpty()) {
                tempValue = tempValue.replaceAll(PatternHelperService.unicodePatternString(), "");
            }

            // remove all octal values
            List<String> octalMatcherResult = PatternHelperService.getOctalEscapeSequencePattern()
                    .matcher(tempValue)
                    .results()
                    .map(MatchResult::group)
                    .toList();
            if (!octalMatcherResult.isEmpty()) {
                tempValue = tempValue.replaceAll(PatternHelperService.octalPatternString(), "");
            }

            // check if there are any more single backslashes. If yes, then throw error.
            return tempValue.matches(PatternHelperService.oddSlashPatternString()) || tempValue.contains("\\");
        }
        return false;
    }
}
