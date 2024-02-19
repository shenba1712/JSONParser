package org.com.backend.model.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ArrayASTNode implements ASTNode {
    private List<ASTNode> value;
}
