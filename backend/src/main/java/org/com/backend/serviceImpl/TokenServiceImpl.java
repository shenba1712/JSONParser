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
            if (currentPointer == 0 && currentChar != '{' && currentChar != '[') {
                throw new IllegalArgumentException("Json should either be enclosed with brackets [] or braces {}");
            }
            switch (currentChar) {
                case '{':
                    tokens.add(new Token(TokenType.BRACE_OPEN, String.valueOf(currentChar)));
                    currentPointer++;
                    break;
                case '}':
                    tokens.add(new Token(TokenType.BRACE_CLOSE, String.valueOf(currentChar)));
                    currentPointer++;
                    break;
                case '[':
                    tokens.add(new Token(TokenType.BRACKET_OPEN, String.valueOf(currentChar)));
                    currentPointer++;
                    break;
                case ']':
                    tokens.add(new Token(TokenType.BRACKET_CLOSE, String.valueOf(currentChar)));
                    currentPointer++;
                    break;
                case ':':
                    tokens.add(new Token(TokenType.COLON, String.valueOf(currentChar)));
                    currentPointer++;
                    break;
                case ',':
                    tokens.add(new Token(TokenType.COMMA, String.valueOf(currentChar)));
                    currentPointer++;
                    break;
                case '"':
                    currentChar = json.charAt(++currentPointer);
                    while (currentChar != '"') {
                        value.append(currentChar);
                        // \ could denote escape sequence. So, any char that comes after this should be included.
                        if (currentChar == '\\') {
                            currentChar = json.charAt(++currentPointer);
                            value.append(currentChar);
                        }
                        currentChar = json.charAt(++currentPointer);
                    }
                    tokens.add(new Token(TokenType.STRING, value.toString()));
                    currentPointer++;
                    break;
                default:
                    value = new StringBuilder();
                    // Skip whitespace
                    if (String.valueOf(currentChar).matches("\\s")) {
                        currentPointer++;
                        break;
                    }
                    while (String.valueOf(currentChar).matches("[\\w.\\-\\\\+]")) {
                        value.append(currentChar);
                        currentChar = json.charAt(++currentPointer);
                    }
                    if (isNumeric(value.toString())) {
                        if (!isNumberStartingWithZero(value.toString())) {
                            tokens.add(new Token(TokenType.NUMBER, value.toString()));
                            break;
                        } else {
                            throw new UnsupportedOperationException("Json does not support numbers starting with zero.");
                        }
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
                        System.out.println(tokens);
                        throw new UnsupportedOperationException("Invalid character: '" + currentChar + "' Cannot Tokenize json string");
                    }
            }
        }
        return tokens;
    }

    private static boolean isNumeric(String strNum) {
        if (isNull(strNum)) {
            return false;
        }
        try {
            new BigDecimal(strNum);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    // should allow decimals and the number zero
    private static boolean isNumberStartingWithZero(String strNum) {
        return strNum.startsWith("0") && !strNum.contains(".") && strNum.length() > 1;
    }
}
