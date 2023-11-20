package com.parola.document2sql.mapper.repository;

import com.parola.document2sql.mapper.entity.SqlTable;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface SqlTableRepository extends ListCrudRepository<SqlTable,Long> {
    List<SqlTable> findByLevel(int level);

    SqlTable findByName(String name);
}
