package org.acme;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.Generated;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@RegisterForReflection
public class Pessoa extends PanacheEntityBase {

    public Pessoa() {}

    public static Pessoa of(UUID id,String apelido,String nome, LocalDate nascimento, String stack) {
        Pessoa p = new Pessoa();
        p.id = id;
        p.apelido = apelido;
        p.nome = nome;
        p.nascimento = nascimento;
        p.stack = stack;
        return p;
    }

    @Id
    @Column(name = "publicID")
    public UUID id;

    public String apelido;

    public String nome;

    public LocalDate nascimento;

    public String stack;

    @Column(name = "BUSCA_TRGM")
    @Generated
    @JsonIgnore
    public String busca;
    
}
