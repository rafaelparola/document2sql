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
    private boolean isNullable;
    @Column
    private boolean isPk;
    @Column
    private boolean isFk;
    @Column
    private boolean isUnique;

    @ManyToOne
    private SqlTable sqlTable;


    public SqlColumn(String name, String dataType, boolean isPk, boolean isFk, boolean isUnique, boolean isNullable, SqlTable sqlTable) {
        this.name = name;
        this.dataType = dataType;
        this.isNullable = isNullable;
        this.sqlTable = sqlTable;
        this.isPk = isPk;
        this.isFk = isFk;
        this.isUnique = isUnique;
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

    public boolean isIsNullable() {
        return isNullable;
    }

    public void setIsNullable(boolean nullable) {
        this.isNullable = nullable;
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

    public boolean isIsPk() {
        return isPk;
    }

    public void setIsPk(boolean pk) {
        this.isPk = pk;
    }

    public boolean isIsFk() {
        return isFk;
    }

    public void setIsFk(boolean fk) {
        this.isFk = fk;
    }

    public boolean isIsUnique() {
        return isUnique;
    }

    public void setIsUnique(boolean unique) {
        this.isUnique = unique;
    }
}