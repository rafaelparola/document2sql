package com.parola.document2sql.mapper.repository;

import com.parola.document2sql.mapper.entity.JsonSchema;
import org.springframework.data.repository.ListCrudRepository;

public interface JsonSchemaRepository extends ListCrudRepository<JsonSchema,Integer> {
}
