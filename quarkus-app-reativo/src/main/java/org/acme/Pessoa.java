package org.acme;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Pessoa {

    public Pessoa() {}

    public Pessoa(UUID publicID, String apelido, String nome, LocalDate nascimento, String stack) {
        this.publicID = publicID;
        this.apelido = apelido;
        this.nome = nome;
        this.nascimento = nascimento;
        String joinedMinusBrackets = stack.substring( 1, stack.length() - 1);

        this.stack = joinedMinusBrackets.split( ", "); 
    }

    @JsonProperty(value = "id")
    public UUID publicID;

    @NotEmpty
    @Size(max = 32)
    public String apelido;

    @NotEmpty
    @Size(max = 32)
    public String nome;

    @NotNull
    public LocalDate nascimento;

    public String[] stack;
    
}
