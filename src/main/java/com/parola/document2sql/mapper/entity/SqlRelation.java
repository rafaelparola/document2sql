package com.parola.document2sql.mapper.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.Reference;

@Entity(name = "SQL_RELATION")
public class SqlRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(cascade=CascadeType.PERSIST)
    private SqlTable referencedTable;
    private String type; // e.g., "object" or "array"

    public SqlRelation(SqlTable referencedTable) {
        this.referencedTable = referencedTable;
    }

    public SqlRelation() {
    }

    // Getters for referencedTable and type

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public SqlTable getReferencedTable() {
        return referencedTable;
    }

    public void setReferencedTable(SqlTable referencedTable) {
        this.referencedTable = referencedTable;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
