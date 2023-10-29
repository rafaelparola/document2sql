package com.parola.document2sql.mapper.entity;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity(name="SQL_TABLE")
public class SqlTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column
    private String name;

    @OneToMany(cascade=CascadeType.ALL, mappedBy = "sqlTable")
    private List<SqlColumn> columns;

    @ManyToMany(cascade=CascadeType.ALL)
    private List<SqlRelation> relations;

    public SqlTable(String name) {
        this.name = name;
        this.columns = new ArrayList<>();
        this.relations = new ArrayList<>();
    }

    public SqlTable(){

    }

    // Getters and setters for name, columns, and relations

    public void setColumn(SqlColumn column) {
        columns.add(column);
    }

    public void setRelation(SqlRelation relation) {
        relations.add(relation);
    }

    public List<SqlColumn> getColumns() {
        return columns;
    }

    public List<SqlRelation> getRelations() {
        return relations;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}