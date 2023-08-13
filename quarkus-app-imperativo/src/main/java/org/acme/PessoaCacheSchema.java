package org.acme;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(includeClasses = PessoaCache.class) 
public interface PessoaCacheSchema extends GeneratedSchema { 
}