package com.siemens.cto.aem.common.domain.model.ssh;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DecryptPasswordTest {
    String decryptor =
            "new com.siemens.cto.aem.common.domain.model.ssh.MockDecryptor().decryptBase64( #stringToDecrypt )";
    String encryptor =
            "new com.siemens.cto.aem.common.domain.model.ssh.MockDecryptor().encryptToBase64( #stringToEncrypt )";

    @Test
    public void testDecrypt() {
        String dummyPassword = "abcd";
        assertEquals("DECRYPT:abcd", new DecryptPassword(encryptor, decryptor).decrypt(dummyPassword));
    }

    @Test
    public void testEncrypt() {
        String dummyPassword = "abcd";
        assertEquals("ENCRYPT:abcd", new DecryptPassword(encryptor, decryptor).encrypt(dummyPassword));
    }
}
