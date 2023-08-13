package org.acme;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.jackson.JacksonMixin;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Immutable
@JsonInclude
public class Pessoa {
    
    @Id
    @GeneratedValue
    @JsonIgnore
    public Long internalID;

    @JsonProperty(value = "id")
    public UUID publicID = UUID.randomUUID();;

    @NotEmpty
    @Size(max = 32)
    public String apelido;

    @NotEmpty
    @Size(max = 32)
    public String nome;

    @NotNull
    public LocalDate nascimento;

    // public List<?> stack;

}
