package org.com.backend.service;

import org.com.backend.model.Token;
import org.com.backend.model.ast.ASTNode;

import java.util.List;

public interface ParserService {

    ASTNode parsedJSON(List<Token> tokens);
}
