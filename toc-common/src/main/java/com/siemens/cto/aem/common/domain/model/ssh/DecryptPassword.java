package com.siemens.cto.aem.common.domain.model.ssh;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class DecryptPassword {

    private final String encryptExpressionString =
            "new com.siemens.cto.infrastructure.StpCryptoService().decryptBase64( #stringToDecrypt )";
    private final String decryptorImpl;

    public DecryptPassword() {
        decryptorImpl = encryptExpressionString;
    }

    public DecryptPassword(String decryptImpl) {
        decryptorImpl = decryptImpl;
    }

    public String decrypt(String encryptedValue) {
        final ExpressionParser expressionParser = new SpelExpressionParser();
        final Expression decryptExpression = expressionParser.parseExpression(decryptorImpl);

        final StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("stringToDecrypt", encryptedValue);
        return decryptExpression.getValue(context, String.class);
    }
}
