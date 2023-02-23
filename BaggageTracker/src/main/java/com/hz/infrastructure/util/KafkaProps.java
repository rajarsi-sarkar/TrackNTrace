package com.hz.infrastructure.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Properties;

@Data
@AllArgsConstructor
public class KafkaProps {
    private Properties props;
}
