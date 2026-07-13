package com.torin.es.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

import com.torin.es.config.EsModuleConfig;

@AutoConfiguration
@Import(EsModuleConfig.class)
public class EsPortsAutoconfiguration {
    
}
