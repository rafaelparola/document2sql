package com.parola.document2sql.mapper.service;

import com.parola.document2sql.mapper.entity.JsonSchema;
import com.parola.document2sql.mapper.entity.SqlSchema;
import com.parola.document2sql.mapper.repository.SqlSchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SqlSchemaService {

    @Autowired
    private SqlSchemaRepository sqlSchemaRepository;

    public void saveSqlSchema(JsonSchema jsonSchema) {
        SqlSchema sqlSchema = new SqlSchema();
        sqlSchema.setJsonSchema(jsonSchema);

        sqlSchemaRepository.save(sqlSchema);
    }
}
