package com.parola.document2sql.mapper.repository;

import com.parola.document2sql.mapper.entity.SqlColumn;
import org.springframework.data.repository.ListCrudRepository;

public interface SqlColumnRepository extends ListCrudRepository<SqlColumn,Long> {
}
