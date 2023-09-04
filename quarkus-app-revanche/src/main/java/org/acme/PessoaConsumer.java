package org.acme;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Deprecated
public class PessoaConsumer {

    // @Inject
    // EntityManager entityManager;

    @Inject
    SessionFactory sf;

    // @Inject
    // Session session;

    // private AtomicInteger counter = new AtomicInteger();

    
    @ConsumeEvent("pessoas")
    // @Blocking
    // @Transactional
    @WithTransaction
    public Uni<Pessoa> consume(InserirPessoa ip) throws SQLException {
        Pessoa pessoa = Pessoa.of(ip.id, ip.apelido, ip.nome, ip.nascimento, ip.stack==null?null:Arrays.toString(ip.stack));
        // Transaction transaction = session.beginTransaction();
        // entityManager.persist(pessoa);
        // transaction.commit();

        // int value = counter.incrementAndGet(); 
        // if(value == 10000) {
        //     System.out.println("PessoaConsumer.consume(ANALYZE) "+LocalDateTime.now());
        //     sf.withTransaction(session -> session.createNativeQuery("analyze (SKIP_LOCKED) pessoa").executeUpdate())
        //     .subscribe().with(
        //        result -> {System.out.println("ANALYZE OK!!!"+LocalDateTime.now());},
        //        failure -> failure.printStackTrace());
        // }
        return pessoa.persist()
        // return sf.withTransaction(session -> session.persist(pessoa))
            // .subscribe().with(
            //    result -> {},
            //    failure -> failure.printStackTrace());
        ;

        
    }


}
