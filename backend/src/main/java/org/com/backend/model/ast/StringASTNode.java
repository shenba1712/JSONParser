package org.com.backend.model.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StringASTNode implements ASTNode {
    private String value;
}
