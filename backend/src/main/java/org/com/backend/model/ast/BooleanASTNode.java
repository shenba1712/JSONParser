package org.com.backend.model.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BooleanASTNode implements ASTNode {
    private boolean value;
}
