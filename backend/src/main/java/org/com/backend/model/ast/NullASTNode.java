package org.com.backend.model.ast;

import lombok.Getter;

@Getter
public class NullASTNode implements ASTNode {
    private final Object value = null;
}
