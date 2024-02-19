package org.com.backend.model.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class NumberASTNode implements ASTNode {
    private BigDecimal value;
}
