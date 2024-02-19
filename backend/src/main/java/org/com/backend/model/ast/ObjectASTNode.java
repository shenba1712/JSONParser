package org.com.backend.model.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ObjectASTNode implements ASTNode {
    private Map<String, ASTNode> value;
}
