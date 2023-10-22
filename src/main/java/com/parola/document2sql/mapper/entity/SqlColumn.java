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
    @Column
    private boolean nullable;
    @ManyToOne
    private SqlTable sqlTable;

    public SqlColumn(String name, String dataType, boolean nullable, SqlTable sqlTable) {
        this.name = name;
        this.dataType = dataType;
        this.nullable = nullable;
        this.sqlTable = sqlTable;
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
}