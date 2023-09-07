package org.acme;

import java.net.URI;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
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

    static Map<String, PessoaCache> pessoasPorApelido = new HashMap<>(100_000);
    static Map<UUID, PessoaCache> pessoasPorID = new HashMap<>(100_000);


    @POST
    @Path("pessoas")
    public Uni<Response> post(
            InserirPessoa ip) throws JsonMappingException, JsonProcessingException, SQLException {
        
        validePessoa(ip);

        ip.id = UUID.randomUUID();
        PessoaCache pessoaCache = PessoaCache.of(ip);
        synchronized(PessoaResource.pessoasPorApelido) {
            PessoaResource.pessoasPorApelido.put(ip.apelido, pessoaCache);
            PessoaResource.pessoasPorID.put(ip.id, pessoaCache);
        }
        Pessoa pessoa = Pessoa.of(ip.id, ip.apelido, ip.nome, ip.nascimento, ip.stack==null?null:Arrays.toString(ip.stack));
        // Uni<Pessoa> pesquisa = Pessoa.find("apelido = ?1", "a").firstResult();
        // return pesquisa.onItem().transformToUni(a -> {
        //     if(a == null) {
        //         return pessoa.persist().replaceWith(Response.created(URI.create("/pessoas/" + ip.id)).build());
        //     }else {
        //         throw new WebApplicationException(RESPONSE_422);
        //     }
        // }).onFailure().transform(e -> new WebApplicationException(RESPONSE_422));
        return Panache.withTransaction(() ->
            pessoa.persist()).replaceWith(Response.created(URI.create("/pessoas/" + ip.id)).build());
    }

    private void validePessoa(InserirPessoa ip) {

        if (ip.nome == null || ip.nascimento == null || ip.apelido == null ||
            ip.nome.length() > 100 || ip.apelido.length() > 32
            ) {
            throw new WebApplicationException(RESPONSE_422);
        }


        if (pessoasPorApelido.containsKey(ip.apelido)) {
            throw new WebApplicationException(RESPONSE_422);
        }

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
    public Uni<List<Pessoa>> getAll(@QueryParam("t") String termo) throws SQLException {
        if (termo == null || "".equals(termo)) {
            throw new WebApplicationException(RESPONSE_400);
        }
        Uni<List<Pessoa>> r = Panache.withSession(() ->
            Pessoa.find("busca like '%' || ?1 || '%'", termo).page(0, 50).list());
        return r;
    }

    @GET
    @Path("pessoas/{id}")
    public Uni<PessoaCache> get(UUID id) throws SQLException {
        Uni<Pessoa> uniPessoa = Pessoa.findById(id);
        return uniPessoa.onItem().transform(p -> PessoaCache.of(p));
    }

    @GET
    @Path("contagem-pessoas")
    public Uni<Long> count() throws SQLException {
        return Pessoa.count();
    }

}
