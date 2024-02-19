package org.com.backend.service;

import org.com.backend.model.Token;

import java.util.List;

public interface TokenService {
    List<Token> tokenize(String json);
}
