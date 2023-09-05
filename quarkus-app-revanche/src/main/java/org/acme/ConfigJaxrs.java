package org.acme;

import java.util.List;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.arc.All;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;

@ApplicationScoped
public class ConfigJaxrs {

    @ServerExceptionMapper
    public RestResponse<String> mapException(org.hibernate.exception.ConstraintViolationException constraint) {
        return RestResponse.status(422);
    }

    @ServerExceptionMapper
    public RestResponse<String> mapInvalidDefinitionException(com.fasterxml.jackson.databind.exc.InvalidDefinitionException constraint) {
        return RestResponse.status(400);
    }
    
    @Singleton
    @Produces
    ObjectMapper objectMapper(@All List<ObjectMapperCustomizer> customizers) {
        ObjectMapper mapper = JsonMapper.builder().disable(MapperFeature.ALLOW_COERCION_OF_SCALARS).addModule(new JavaTimeModule()).build();
        mapper.coercionConfigFor(LogicalType.Textual)
            .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
            .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail)
            .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
            ;
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // Apply all ObjectMapperCustomizer beans (incl. Quarkus)
        for (ObjectMapperCustomizer customizer : customizers) {
            customizer.customize(mapper);
        }

        return mapper;
    }
}
