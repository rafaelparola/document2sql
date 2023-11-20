package com.parola.document2sql.mapper.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name="SQL_TABLE")
public class SqlTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column
    private String name;

    @Column
    private int level;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date_time")
    private Date createdDateAndTime;

    @OneToMany(cascade=CascadeType.ALL)
    private List<SqlColumn> columns;


    public SqlTable(String name) {
        this.name = name;
        this.columns = new ArrayList<>();
/*
        this.relations = new ArrayList<>();
*/
    }

    public SqlTable(){

    }

    // Getters and setters for name, columns, and relations

    public void setColumn(SqlColumn column) {
        columns.add(column);
    }

/*
    public void setRelation(SqlRelation relation) {
        relations.add(relation);
    }
*/

    public List<SqlColumn> getColumns() {
        return columns;
    }

/*
    public List<SqlRelation> getRelations() {
        return relations;
    }
*/

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

    public Date getCreatedDateAndTime() {
        return createdDateAndTime;
    }

    public void setCreatedDateAndTime(Date createdDateAndTime) {
        this.createdDateAndTime = createdDateAndTime;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}