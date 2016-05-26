package com.siemens.cto.aem.common.domain.model.ssh;

public class MockDecryptor {

    public String decryptBase64(String enc) {
        return "DECRYPT:" + enc;
    }

}
