package com.cerner.jwala.common.domain.model.ssh;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class DecryptPassword {

    private final String decryptExpressionString =
            "new com.siemens.cto.infrastructure.StpCryptoService().decryptBase64( #stringToDecrypt )";
    private final String encryptExpressionString =
            "new com.siemens.cto.infrastructure.StpCryptoService().encryptToBase64( #stringToEncrypt )";

    private final String decryptorImpl;
    private final String encryptorImpl;

    public DecryptPassword() {
        decryptorImpl = decryptExpressionString;
        encryptorImpl = encryptExpressionString;
    }

    public DecryptPassword(String encryptImpl, String decryptImpl) {
        encryptorImpl = encryptImpl;
        decryptorImpl = decryptImpl;
    }

    public String decrypt(String encryptedValue) {
        if (encryptedValue==null) {
            return null;
        }
        
        final ExpressionParser expressionParser = new SpelExpressionParser();
        final Expression decryptExpression = expressionParser.parseExpression(decryptorImpl);

        final StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("stringToDecrypt", encryptedValue);
        return decryptExpression.getValue(context, String.class);
    }
    
    public String encrypt(String unencryptedValue) {
        
        if (unencryptedValue==null) {
            return null;
        }
        
        final ExpressionParser expressionParser = new SpelExpressionParser();
        final Expression encryptExpression = expressionParser.parseExpression(encryptorImpl);

        final StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("stringToEncrypt", unencryptedValue);
        return encryptExpression.getValue(context, String.class);
    }
}
