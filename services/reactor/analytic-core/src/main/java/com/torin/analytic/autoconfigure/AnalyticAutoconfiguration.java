package com.torin.analytic.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

import com.torin.analytic.config.AnalyticConfig;

@AutoConfiguration
@Import(AnalyticConfig.class)
public class AnalyticAutoconfiguration {
    
}
