package org.acme;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class Pessoa {

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

    @NotEmpty
    @Size(max = 32)
    public String apelido;

    @NotEmpty
    @Size(max = 100)
    public String nome;

    @NotNull
    public LocalDate nascimento;

    public String stack;
    
}
