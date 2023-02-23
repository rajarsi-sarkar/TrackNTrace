package com.hz.infra.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyFileReader {

    public static Properties getProps(String aFileName) throws IOException {
        try (
                InputStream is = PropertyFileReader.class.getResourceAsStream("/" + aFileName)
        )
        {
            Properties props = new Properties();
            props.load(is);
            return props;
        }
    }
}
