package org.acme;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.infinispan.client.hotrod.RemoteCache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.agroal.api.AgroalDataSource;
import io.quarkus.infinispan.client.Remote;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.NonBlocking;
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

@Path("")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PessoaResource {

    private static final Response RESPONSE_400 = Response.status(Response.Status.BAD_REQUEST).build();
    private static final Response RESPONSE_422 = Response.status(422).build();

    private static final Pessoa PESSOA_STUB = new Pessoa(UUID.randomUUID(), "Vini", "Vinicius Ferraz", LocalDate.now(),
            "[Java]");

    @Inject
    AgroalDataSource dataSource;

    @Inject
    @Remote("apelidos")
    RemoteCache<String, PessoaCache> pessoasPorApelido;

    // @Inject
    // @Remote("porid")
    // RemoteCache<String, PessoaCache> pessoasPorId;

    public void start(@Observes StartupEvent event) {
        try {insertPessoa(PESSOA_STUB);} catch (Exception e) {}
        try {getAll("a");} catch (Exception e) {}
        try {get(UUID.randomUUID());} catch (Exception e) {}
        
            
    }

    @POST
    @Path("pessoas")
    @NonBlocking
    public Response post(
            @Valid Pessoa p) throws JsonMappingException, JsonProcessingException, SQLException {

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

        insertPessoa(p);
        return Response.created(URI.create("/pessoas/" + p.publicID)).build();

    }

    private void insertPessoa(Pessoa p) throws SQLException {
        PessoaCache pessoaCache = PessoaCache.of(p);
        // pessoasPorId.putAsync(p.publicID.toString(), pessoaCache);
        pessoasPorApelido.putAsync(p.apelido, pessoaCache);
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO public.pessoa(apelido, nascimento, nome, publicid, stack) VALUES (?, ?, ?, ?, ?)");) {

            statement.setString(1, p.apelido);
            Date d = Date.from(p.nascimento.atStartOfDay(ZoneId.systemDefault()).toInstant());
            statement.setDate(2, new java.sql.Date(d.getTime()));
            statement.setString(3, p.nome);
            statement.setObject(4, p.publicID);
            statement.setString(5, Arrays.toString(p.stack));

            statement.execute();
        }

    }

    @GET
    @Path("pessoas")
    public List<Pessoa> getAll(@QueryParam("t") String termo) throws SQLException {
        if (termo == null || "".equals(termo)) {
            throw new WebApplicationException(RESPONSE_400);
        }
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT apelido, nascimento, nome, publicid, stack from Pessoa where nome like ? or apelido like ? or stack like ? limit 50");) {

            statement.setString(1, "%" + termo + "%");
            statement.setString(2, "%" + termo + "%");
            statement.setString(3, "%" + termo + "%");

            try (ResultSet rs = statement.executeQuery()) {

                List<Pessoa> pessoas = new ArrayList<>(100);
                while (rs.next()) {
                    pessoas.add(from(rs));
                }
                return pessoas;
            }
        }
    }

    private Pessoa from(ResultSet rs) throws SQLException {
        return new Pessoa(
                (UUID) rs.getObject("publicid"),
                rs.getString("apelido"), rs.getString("nome"),
                rs.getDate("nascimento").toLocalDate(),
                rs.getString("stack"));
    }

    @GET
    @Path("pessoas/{id}")
    public Response get(UUID id) throws SQLException {
        Pessoa pessoa = findById(id);
        return pessoa != null ? Response.ok(pessoa).build()
                : Response.status(jakarta.ws.rs.core.Response.Status.NOT_FOUND).build();
    }

    public Pessoa findById(UUID id) throws SQLException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT apelido, nascimento, nome, publicid, stack from Pessoa where publicid = ?"); ) {

            statement.setObject(1, id);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return from(rs);
                }
            }
            return null;
        }
    }

    @GET
    @Path("contagem-pessoas")
    public int count() throws SQLException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT count(internalid) from Pessoa");ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

}
