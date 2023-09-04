package org.acme;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InserirPessoa {

    public UUID id;

    @NotEmpty
    @Size(max = 32)
    public String apelido;

    @NotEmpty
    @Size(max = 100)
    public String nome;

    @NotNull
    public LocalDate nascimento;

    public String[] stack;
    
}
