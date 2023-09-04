package org.acme;

import java.time.LocalDate;
import java.util.UUID;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class InserirPessoa {

    public UUID id;

    public String apelido;

    public String nome;

    public LocalDate nascimento;

    public String[] stack;
    
}
