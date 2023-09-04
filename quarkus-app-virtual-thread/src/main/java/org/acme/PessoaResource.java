package org.acme;

import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.reactive.mutiny.Mutiny;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.EventBus;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PessoaResource {

    private static final Response RESPONSE_400 = Response.status(Response.Status.BAD_REQUEST).build();
    private static final Response RESPONSE_422 = Response.status(422).build();

    @Inject
    EventBus eventBus;

    // @Inject
    // EntityManager entityManager;

    @Inject
    Mutiny.SessionFactory sf; 

    static Map<String, PessoaCache> pessoasPorApelido = new HashMap<>(50_000);
    static Map<UUID, PessoaCache> pessoasPorID = new HashMap<>(50_000);


    @POST
    @Path("pessoas")
    @RunOnVirtualThread
    public Response post(
            @Valid InserirPessoa p) throws JsonMappingException, JsonProcessingException, SQLException {
        if (pessoasPorApelido.containsKey(p.apelido)) {
            throw new WebApplicationException(RESPONSE_422);
        }

        // TODO melhorar um dia
        if (p.stack != null) {
            for (int i = 0; i < p.stack.length; i++) {
                if (p.stack[i].length() > 32) {
                    throw new WebApplicationException(RESPONSE_422);
                }
            }
        }
        p.id = UUID.randomUUID();
        PessoaCache pessoaCache = PessoaCache.of(p);
        synchronized(PessoaResource.pessoasPorApelido) {
            PessoaResource.pessoasPorApelido.put(p.apelido, pessoaCache);
            PessoaResource.pessoasPorID.put(p.id, pessoaCache);
        }
        eventBus.publish("pessoas", p);
        
        return Response.created(URI.create("/pessoas/" + p.id)).build();

    }

    @GET
    @Path("pessoas")
    public Multi<PessoaCache> getAll(@QueryParam("t") String termo) throws SQLException {
        if (termo == null || "".equals(termo)) {
            throw new WebApplicationException(RESPONSE_400);
        }
        Uni<List<Pessoa>> r = sf.withSession(s -> {
            org.hibernate.reactive.mutiny.Mutiny.SelectionQuery<Pessoa> nativeQuery = s.createNativeQuery("SELECT publicid, nome, apelido, nascimento, stack from Pessoa where BUSCA_TRGM like ? limit 50", Pessoa.class);
            nativeQuery.setParameter(1, "%" + termo + "%");
            return nativeQuery.getResultList();
        });
        return r.onItem()
        .transformToMulti(item -> Multi.createFrom().iterable(item))
            .onItem().transform(p -> PessoaCache.of(p))
            ;
        // Query nativeQuery = entityManager.createNativeQuery("SELECT publicid, nome, apelido, nascimento, stack from Pessoa where BUSCA_TRGM like ? limit 50", Pessoa.class);
        // nativeQuery.setParameter(1, "%" + termo + "%");
        // return nativeQuery.getResultList();
    }

    @GET
    @Path("pessoas/{id}")
    @RunOnVirtualThread
    public PessoaCache get(UUID id) throws SQLException {
        return pessoasPorID.get(id);
    }

    @GET
    @Path("contagem-pessoas")
    public Uni<Integer> count() throws SQLException {
        return sf.withStatelessSession(s -> s.createNativeQuery("SELECT count(*) from Pessoa", Integer.class).getSingleResult());
        // return (Integer) entityManager.createNativeQuery("SELECT count(*) from Pessoa", Integer.class).getSingleResult();
    }

}
