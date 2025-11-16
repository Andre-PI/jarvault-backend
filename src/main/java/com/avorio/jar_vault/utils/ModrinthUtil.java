package com.avorio.jar_vault.utils;

import org.springframework.stereotype.Component;

@Component
public class ModrinthUtil {

    public String buildModrinthParamPattern(String value) {
        return "[\"" +
                value +
                "\"]";
    }
}
