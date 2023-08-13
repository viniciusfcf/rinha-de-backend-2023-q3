package org.acme;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.infinispan.client.hotrod.RemoteCache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.quarkus.infinispan.client.Remote;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.event.Observes;
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
import jakarta.ws.rs.core.Response.ResponseBuilder;

@Path("")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PessoaResource {


    private static final Response RESPONSE_400 = Response.status(Response.Status.BAD_REQUEST).build();
    private static final Response RESPONSE_422 = Response.status(422).build();

    private static final Pessoa PESSOA_STUB = new Pessoa(UUID.randomUUID(), "Vini", "Vinicius Ferraz", LocalDate.now(), "[Java]");

    @Inject
    PgPool dataSource;

    @Inject
    @Remote("apelidos")
    RemoteCache<String, PessoaCache> pessoasPorApelido;

    public void start(@Observes StartupEvent event) {
        for (int i = 0; i < 10; i++) {
            try {insertPessoa(PESSOA_STUB);} catch (Exception e) {}
            try {getAll("t");} catch (Exception e) {}
            try {getAll("asdf");} catch (Exception e) {}
            try {get(UUID.randomUUID());} catch (Exception e) {}
            try {get(PESSOA_STUB.publicID);} catch (Exception e) {}
            
        }
        
        
    }

    @POST
    @Path("pessoas")
    @NonBlocking
    public Uni<Object> post(
            @Valid Pessoa p
    ) throws JsonMappingException, JsonProcessingException {

        if (pessoasPorApelido.containsKey(p.apelido)) {
            throw new WebApplicationException(RESPONSE_422);
        }

        // TODO melhorar um dia
        if (p.stack != null) {
            for (int i = 0; i < p.stack.length; i++) {
                if (p.stack[i].length() > 32) {
                    throw new WebApplicationException(RESPONSE_422);
                }
                p.stack[i] = p.stack[i].toLowerCase();
            }
        }

        p.publicID = UUID.randomUUID();
        p.apelido = p.apelido.toLowerCase();
        p.nome = p.nome.toLowerCase();

        return insertPessoa(p)
            .onItem().transform(x -> Response.created(URI.create("/pessoas/" + p.publicID)).build());

    }

    private Uni<RowSet<Row>> insertPessoa(Pessoa p) {
        PessoaCache pessoaCache = PessoaCache.of(p);
        // pessoasPorId.putAsync(p.publicID.toString(), pessoaCache);
        pessoasPorApelido.putAsync(p.apelido, pessoaCache);
        return dataSource.preparedQuery("INSERT INTO public.pessoa(internalid, apelido, nascimento, nome, publicid, stack) VALUES (nextval('serial'), $1, $2, $3, $4, $5)")
            .execute(Tuple.of(p.apelido, p.nascimento, p.nome, p.publicID, Arrays.toString(p.stack)));
        
    }

    @GET
    @Path("pessoas")
    public Multi<Pessoa> getAll(@QueryParam("t") String termo) {
        if (termo == null || "".equals(termo)) {
            throw new WebApplicationException(RESPONSE_400);
        }
        return dataSource.preparedQuery("SELECT apelido, nascimento, nome, publicid, stack from Pessoa where BUSCA_TRGM like $1 limit 50").execute(Tuple.of("%"+termo.toLowerCase()+"%"))
            .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
            .onItem().transform(row -> new Pessoa(
                row.getUUID("publicid"),
                row.getString("apelido"),row.getString("nome"), row.getLocalDate("nascimento"), row.getString("stack")));
    }

    @GET
    @Path("pessoas/{id}")
    public Uni<Response> get(UUID id) {
        return findById(id)
                    .onItem().transform(fruit -> fruit != null ? Response.ok(fruit) : Response.status(jakarta.ws.rs.core.Response.Status.NOT_FOUND)) 
                    .onItem().transform(ResponseBuilder::build);
    }

    public Uni<Pessoa> findById(UUID id) {
        return dataSource.preparedQuery("SELECT apelido, nascimento, nome, publicid, stack from Pessoa where publicid = $1").execute(Tuple.of(id)) 
                .onItem().transform(RowSet::iterator) 
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null); 
    }

    private Pessoa from(Row row) {
        return new Pessoa(row.getUUID("publicid"),
                    row.getString("apelido"),row.getString("nome"), row.getLocalDate("nascimento"), row.getString("stack"));
    }

    @GET
    @Path("contagem-pessoas")
    public Uni<Long> count() {
        return dataSource.preparedQuery("select count(*) as qtd from Pessoa").execute()
            .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong("qtd"));
    }

}
