package org.com.backend.serviceImpl;

import org.com.backend.model.Token;
import org.com.backend.model.TokenType;
import org.com.backend.service.TokenService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@Service
public class TokenServiceImpl implements TokenService {

    @Override
    public List<Token> tokenize(String json) {
        int currentPointer = 0;
        List<Token> tokens = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        while (currentPointer < json.length()) {
            char currentChar = json.charAt(currentPointer);
            switch (currentChar) {
                case '{':
                    tokens.add(new Token(TokenType.BRACE_OPEN, String.valueOf(currentChar)));
                    break;
                case '}':
                    tokens.add(new Token(TokenType.BRACE_CLOSE, String.valueOf(currentChar)));
                    break;
                case '[':
                    tokens.add(new Token(TokenType.BRACKET_OPEN, String.valueOf(currentChar)));
                    break;
                case ']':
                    tokens.add(new Token(TokenType.BRACKET_CLOSE, String.valueOf(currentChar)));
                    break;
                case ':':
                    tokens.add(new Token(TokenType.COLON, String.valueOf(currentChar)));
                    break;
                case ',':
                    tokens.add(new Token(TokenType.COMMA, String.valueOf(currentChar)));
                    break;
                case '"':
                    currentChar = json.charAt(++currentPointer);
                    while (currentChar != '"') {
                        value.append(currentChar);
                        currentChar = json.charAt(++currentPointer);
                    }
                    tokens.add(new Token(TokenType.STRING, value.toString()));
                    break;
                default:
                    value = new StringBuilder();
                    // Skip whitespace
                    if (String.valueOf(currentChar).matches("\\s")) {
                        break;
                    }
                    while (String.valueOf(currentChar).matches("\\w")) {
                        value.append(currentChar);
                        currentChar = json.charAt(++currentPointer);
                    }
                    if (isNumeric(value.toString())) {
                        tokens.add(new Token(TokenType.NUMBER, value.toString()));
                        break;
                    } else if (value.toString().equals("true")) {
                        tokens.add(new Token(TokenType.TRUE, value.toString()));
                        break;
                    } else if (value.toString().equals("false")) {
                        tokens.add(new Token(TokenType.FALSE, value.toString()));
                        break;
                    } else if (value.toString().equals("null")) {
                        tokens.add(new Token(TokenType.NULL, value.toString()));
                        break;
                    } else {
                        throw new UnsupportedOperationException("Invalid character. Cannot Tokenize json string");
                    }
            }
            currentPointer++;
        }
        return tokens;
    }

    private static boolean isNumeric(String strNum) {
        if (isNull(strNum)) {
            return false;
        }
        try {
            new BigDecimal(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
