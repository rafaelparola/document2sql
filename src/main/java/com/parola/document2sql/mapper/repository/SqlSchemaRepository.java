package com.parola.document2sql.mapper.repository;

import com.parola.document2sql.mapper.entity.SqlSchema;
import org.springframework.data.repository.ListCrudRepository;

public interface SqlSchemaRepository extends ListCrudRepository<SqlSchema,Long> {
}
