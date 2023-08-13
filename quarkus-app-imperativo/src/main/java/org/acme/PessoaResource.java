package org.acme;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.arc.All;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("pessoas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PessoaResource {
    
    // @Inject
    // SessionFactory sf;

    @Inject
    EntityManager em;

    @Inject
    Logger logger;

    @POST
    @Transactional
    public void post(
        @Valid Pessoa p
        // @RequestBody String body
        ) throws JsonMappingException, JsonProcessingException {
        

        // Pessoa p = mapper.readValue(body, Pessoa.class);
        logger.info("post");
        // try (StatelessSession s = sf.openStatelessSession()) {
            // Pessoa p = new Pessoa();
            // p.apelido = "a";
            // p.nascimento = LocalDate.now();
            // p.nome = "Vinicius";
            em.persist(p);
            // s.insert(s);
        // }
    }

    @GET
    public List<Pessoa> get() {
        logger.info("get");
        return em.createQuery("SELECT p FROM Pessoa p", Pessoa.class).getResultList();
        // try (StatelessSession s = sf.openStatelessSession()) {
        //     return s.createQuery("SELECT p FROM Pessoa p", Pessoa.class).getResultList();
        // }
    }

    @ServerExceptionMapper
    public RestResponse<String> mapException(ConstraintViolationException constraint) {
        return RestResponse.status(422);
    }
    
    @Singleton
    @Produces
    ObjectMapper objectMapper(@All List<ObjectMapperCustomizer> customizers) {
        logger.info("NEW ObjectMapper");
        ObjectMapper mapper = JsonMapper.builder().disable(MapperFeature.ALLOW_COERCION_OF_SCALARS).addModule(new JavaTimeModule()).build();
        mapper.coercionConfigFor(LogicalType.Textual)
            .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
            .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail)
            .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
            ;
        // Apply all ObjectMapperCustomizer beans (incl. Quarkus)
        for (ObjectMapperCustomizer customizer : customizers) {
            customizer.customize(mapper);
        }

        return mapper;
    }
}
