package org.acme;

import java.net.URI;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.reactive.mutiny.Mutiny;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
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

    // @Inject
    // EventBus eventBus;

    // @Inject
    // EntityManager entityManager;

    // @Inject
    // Mutiny.SessionFactory sf; 

    static Map<String, PessoaCache> pessoasPorApelido = new HashMap<>(60_000);
    static Map<UUID, PessoaCache> pessoasPorID = new HashMap<>(60_000);


    @POST
    @Path("pessoas")
    // @RunOnVirtualThread
    @WithTransaction
    public Uni<Response> post(
            InserirPessoa ip) throws JsonMappingException, JsonProcessingException, SQLException {
        
        validatePessoa(ip);

        ip.id = UUID.randomUUID();
        PessoaCache pessoaCache = PessoaCache.of(ip);
        synchronized(PessoaResource.pessoasPorApelido) {
            PessoaResource.pessoasPorApelido.put(ip.apelido, pessoaCache);
            PessoaResource.pessoasPorID.put(ip.id, pessoaCache);
        }
        // eventBus.publish("pessoas", p);
        Pessoa pessoa = Pessoa.of(ip.id, ip.apelido, ip.nome, ip.nascimento, ip.stack==null?null:Arrays.toString(ip.stack));
        
        return pessoa.persist().replaceWith(Response.created(URI.create("/pessoas/" + ip.id)).build());

    }

    private void validatePessoa(InserirPessoa ip) {

        if (ip.nome == null || ip.nascimento == null || ip.apelido == null ||
            ip.nome.length() > 100 || ip.apelido.length() > 32
            ) {
            throw new WebApplicationException(RESPONSE_422);
        }


        if (pessoasPorApelido.containsKey(ip.apelido)) {
            throw new WebApplicationException(RESPONSE_422);
        }

        // TODO melhorar um dia
        if (ip.stack != null) {
            for (int i = 0; i < ip.stack.length; i++) {
                if (ip.stack[i].length() > 32) {
                    throw new WebApplicationException(RESPONSE_422);
                }
            }
        }
    }

    @GET
    @Path("pessoas")
    @WithSession
    public Uni<List<Pessoa>> getAll(@QueryParam("t") String termo) throws SQLException {
        if (termo == null || "".equals(termo)) {
            throw new WebApplicationException(RESPONSE_400);
        }
        Uni<List<Pessoa>> r = Pessoa.find("busca like '%' || ?1 || '%'", termo).page(0, 50).list();
        // Uni<List<Pessoa>> r = sf.withSession(s -> {
        //     org.hibernate.reactive.mutiny.Mutiny.SelectionQuery<Pessoa> nativeQuery = s.createNativeQuery("SELECT publicid, nome, apelido, nascimento, stack from Pessoa where BUSCA_TRGM like ? limit 50", Pessoa.class);
        //     nativeQuery.setParameter(1, "%" + termo + "%");
        //     return nativeQuery.getResultList();
        // });
        return r;
        // return r.onItem()
        // .transformToMulti(item -> Multi.createFrom().iterable(item))
        //     .onItem().transform(p -> PessoaCache.of(p))
        //     ;
        // Query nativeQuery = entityManager.createNativeQuery("SELECT publicid, nome, apelido, nascimento, stack from Pessoa where BUSCA_TRGM like ? limit 50", Pessoa.class);
        // nativeQuery.setParameter(1, "%" + termo + "%");
        // return nativeQuery.getResultList();
    }

    @GET
    @Path("pessoas/{id}")
    // @RunOnVirtualThread
    public Uni<PessoaCache> get(UUID id) throws SQLException {
        Uni<Pessoa> uniPessoa = Pessoa.findById(id);
        return uniPessoa.onItem().transform(p -> PessoaCache.of(p));
        // return Uni.createFrom().item(pessoa.get(id));
    }

    @GET
    @Path("contagem-pessoas")
    @WithTransaction
    public Uni<Long> count() throws SQLException {
        return Pessoa.count();
        // return sf.withStatelessSession(s -> s.createNativeQuery("SELECT count(*) from Pessoa", Integer.class).getSingleResult());
        // return (Integer) entityManager.createNativeQuery("SELECT count(*) from Pessoa", Integer.class).getSingleResult();
    }

}
