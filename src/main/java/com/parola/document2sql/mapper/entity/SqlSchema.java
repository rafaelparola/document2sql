package com.parola.document2sql.mapper.entity;

import jakarta.persistence.*;

@Entity(name = "SQL_SCHEMA")
public class SqlSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "json_schema_id")
    private JsonSchema jsonSchema;

    public SqlSchema(){
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public JsonSchema getJsonSchema() {
        return jsonSchema;
    }

    public void setJsonSchema(JsonSchema jsonSchema) {
        this.jsonSchema = jsonSchema;
    }
}
