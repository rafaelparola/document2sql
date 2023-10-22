package com.parola.document2sql.mapper.repository;

import com.parola.document2sql.mapper.entity.SqlTable;
import org.springframework.data.repository.ListCrudRepository;

public interface SqlTableRepository extends ListCrudRepository<SqlTable,Long> {
}
