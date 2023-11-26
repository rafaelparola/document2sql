package com.parola.document2sql.mapper.entity;

import jakarta.persistence.*;

@Entity(name = "SQL_COLUMN")
public class SqlColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column
    private String name;
    @Column
    private String dataType;
    @Column(columnDefinition = "boolean default true")
    private boolean nullable = true;
    @Column
    private boolean pk;
    @Column
    private boolean fk;
    @Column
    private boolean uniq;

    @ManyToOne
    private SqlTable sqlTable;


    public SqlColumn(String name, String dataType, boolean isPk, boolean isFk, boolean isUnique, boolean isNullable, SqlTable sqlTable) {
        this.name = name;
        this.dataType = dataType;
        this.nullable = isNullable;
        this.sqlTable = sqlTable;
        this.pk = isPk;
        this.fk = isFk;
        this.uniq = isUnique;
    }

    public SqlColumn() {
    }

    // Getters for name, dataType, and nullable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public SqlTable getSqlTable() {
        return sqlTable;
    }

    public void setSqlTable(SqlTable sqlTable) {
        this.sqlTable = sqlTable;
    }

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }

    public boolean isFk() {
        return fk;
    }

    public void setFk(boolean fk) {
        this.fk = fk;
    }

    public boolean isUniq() {
        return uniq;
    }

    public void setUniq(boolean unique) {
        this.uniq = unique;
    }
}