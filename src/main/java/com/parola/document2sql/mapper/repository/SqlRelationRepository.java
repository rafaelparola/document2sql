package com.parola.document2sql.mapper.repository;

import com.parola.document2sql.mapper.entity.SqlRelation;
import org.springframework.data.repository.ListCrudRepository;

public interface SqlRelationRepository extends ListCrudRepository<SqlRelation,Long>  {
}
