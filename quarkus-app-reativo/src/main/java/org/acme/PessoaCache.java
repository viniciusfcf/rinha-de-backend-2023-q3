package org.acme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.infinispan.protostream.annotations.ProtoField;

public class PessoaCache {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @ProtoField(number = 1, required = true)
    public String publicID;

    @ProtoField(number = 2, required = true)
    public String apelido;

    @ProtoField(number = 3, required = true)
    public String nome;

    @ProtoField(number = 4, required = true)
    public String nascimento;

    @ProtoField(number = 5)
    public String[] stack;

    public static PessoaCache of(Pessoa p) {
        PessoaCache pc = new PessoaCache();
        pc.apelido = p.apelido;
        
        pc.publicID = p.publicID.toString();
        pc.nome = p.nome;
        
        pc.nascimento = p.nascimento.format(formatter);
        pc.stack = p.stack;
        return pc;
    }

    public static PessoaCache of(String apelido, String publicID, String nome, LocalDate nascimento, String[] stack) {
        PessoaCache pc = new PessoaCache();
        pc.apelido = apelido;
        
        pc.publicID = publicID.toString();
        pc.nome = nome;
        
        pc.nascimento = nascimento.format(formatter);
        pc.stack = stack;
        return pc;
    }

    @Override
    public String toString() {
        return "PessoaCache [publicID=" + publicID + ", apelido=" + apelido + ", nome=" + nome + ", nascimento="
                + nascimento + ", stack=" + Arrays.toString(stack) + "]";
    }
    
}
