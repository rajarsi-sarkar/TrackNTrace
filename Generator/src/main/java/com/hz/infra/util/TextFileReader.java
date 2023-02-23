package com.hz.infra.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TextFileReader {

    public static List<String> getLines(String aFileName) throws IOException {
        try (
                InputStream is = TextFileReader.class.getResourceAsStream("/" + aFileName);
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr)
        )
        {
            List<String> list = new ArrayList<>();
            br.lines().forEach(list::add);
            return list;
        }
    }
}
