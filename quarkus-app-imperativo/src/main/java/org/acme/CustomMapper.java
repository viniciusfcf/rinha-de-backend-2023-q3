package org.acme;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

@Singleton
public class CustomMapper implements ObjectMapperCustomizer {

    public void customize(ObjectMapper mapper) {
        mapper.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS);
    }
}