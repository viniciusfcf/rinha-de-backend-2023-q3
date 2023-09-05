package org.acme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class PessoaCache {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String id;

    public String apelido;

    public String nome;

    public String nascimento;

    public String[] stack;

    public static PessoaCache of(Pessoa p) {
        PessoaCache pessoa = PessoaResource.pessoasPorID.get(p.id);
        if(pessoa != null) {
            return pessoa;
        }
        PessoaCache pc = new PessoaCache();
        pc.apelido = p.apelido;
        
        pc.id = p.id.toString();
        pc.nome = p.nome;
        
        pc.nascimento = p.nascimento.format(formatter);
        if(p.stack != null) {
            String joinedMinusBrackets = p.stack.substring( 1, p.stack.length() - 1);
            pc.stack = joinedMinusBrackets.split( ", "); 
        }
        PessoaResource.pessoasPorID.put(p.id, pc);
        return pc;
    }

    public static PessoaCache of(InserirPessoa p) {
        PessoaCache pc = new PessoaCache();
        pc.apelido = p.apelido;
        
        pc.id = p.id.toString();
        pc.nome = p.nome;
        
        pc.nascimento = p.nascimento.format(formatter);
        pc.stack = p.stack; 
        return pc;
    }

    public static PessoaCache of(String apelido, String publicID, String nome, LocalDate nascimento, String[] stack) {
        PessoaCache pc = new PessoaCache();
        pc.apelido = apelido;
        
        pc.id = publicID.toString();
        pc.nome = nome;
        
        pc.nascimento = nascimento.format(formatter);
        pc.stack = stack;
        return pc;
    }

    @Override
    public String toString() {
        return "PessoaCache [id=" + id + ", apelido=" + apelido + ", nome=" + nome + ", nascimento="
                + nascimento + ", stack=" + Arrays.toString(stack) + "]";
    }
    
}
