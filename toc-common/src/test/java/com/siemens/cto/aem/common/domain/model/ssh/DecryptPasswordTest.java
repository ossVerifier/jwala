package com.siemens.cto.aem.common.domain.model.ssh;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DecryptPasswordTest {

    @Test
    public void testDecrypt() {
        String decryptor =
                "new com.siemens.cto.aem.common.domain.model.ssh.MockDecryptor().decryptBase64( #stringToDecrypt )";
        String dummyPassword = "abcd";
        assertEquals("DECRYPT:abcd", new DecryptPassword(decryptor).decrypt(dummyPassword));
    }
}
