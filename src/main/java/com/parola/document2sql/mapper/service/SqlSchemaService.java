package com.parola.document2sql.mapper.service;

import com.parola.document2sql.mapper.entity.*;
import com.parola.document2sql.mapper.repository.SqlColumnRepository;
import com.parola.document2sql.mapper.repository.SqlRelationRepository;
import com.parola.document2sql.mapper.repository.SqlSchemaRepository;
import com.parola.document2sql.mapper.repository.SqlTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SqlSchemaService {

    @Autowired
    private SqlSchemaRepository sqlSchemaRepository;

    @Autowired
    private SqlTableRepository sqlTableRepository;

    @Autowired
    private SqlColumnRepository sqlColumnRepository;

    @Autowired
    private SqlRelationRepository sqlRelationRepository;

    public void saveSqlSchema(JsonSchema jsonSchema) {
        SqlSchema sqlSchema = new SqlSchema();
        sqlSchema.setJsonSchema(jsonSchema);

        sqlSchemaRepository.save(sqlSchema);
    }
    public String getSqlSchemaDdl() {
        List<SqlTable> tables = sqlTableRepository.findAll();
        Map<Long, List<SqlRelation>> tableRelations = mapTableRelations();

        StringBuilder ddl = new StringBuilder();

        for (SqlTable table : tables) {
            ddl.append(createTableDdl(table, tableRelations.get(table.getId()))).append("\n");
        }

        return ddl.toString();
    }

    private Map<Long, List<SqlRelation>> mapTableRelations() {
        Map<Long, List<SqlRelation>> relationsMap = new HashMap<>();
        List<SqlRelation> relations = sqlRelationRepository.findAll();

        for (SqlRelation relation : relations) {
            relationsMap.computeIfAbsent(relation.getOriginTable().getId(), k -> new ArrayList<>()).add(relation);
        }

        return relationsMap;
    }

    private String createTableDdl(SqlTable table, List<SqlRelation> relations) {
        StringBuilder ddl = new StringBuilder();
        StringBuilder ddlIndex = new StringBuilder();

        String foreignKey = "";
        List<String> uniqueColumns = new ArrayList<>();

        ddl.append("CREATE TABLE ").append(table.getName()).append(" (");

        for (SqlColumn column : table.getColumns()) {
            ddl.append(column.getName()).append(" ")
                    .append(column.getDataType());

            if (column.isPk()) {
                ddl.append(" PRIMARY KEY DEFAULT gen_random_uuid()");
            }

            if (!column.isNullable()) {
                ddl.append(" NOT NULL");
            }

            if(column.isFk()) {
                foreignKey = column.getName();
            }

            if (column.isUniq()) {
                uniqueColumns.add(column.getName());
            }

            ddl.append(", ");
        }

        // Append Unique Constraints
        if (uniqueColumns.size() > 0){
            for (String column : uniqueColumns) {
                ddl.append("UNIQUE (").append(column).append(") ,");
            }
        }

        // Append foreign key constraints
        if (relations != null) {
            for (SqlRelation relation : relations) {
                if (!Objects.equals(relation.getType(), "1-1")){
                    SqlTable referencedTable = relation.getReferencedTable();
                    SqlTable originTable = relation.getOriginTable();
                    ddl.append("FOREIGN KEY (").append(referencedTable.getName()).append("_id) ")
                            .append("REFERENCES ").append(referencedTable.getName()).append(", ");
                    // Creates index for foreign key
                    ddlIndex.append("\n");
                    ddlIndex.append("CREATE INDEX ON ").append(originTable.getName()).append("(").append(referencedTable.getName()).append("_id);");

                } else {
                    SqlTable referencedTable = relation.getReferencedTable();
                    ddl.append("FOREIGN KEY (").append(foreignKey).append(") ")
                            .append("REFERENCES ").append(referencedTable.getName()).append(", ");
                }
            }
        }

        // Remove the trailing comma and space
        int lastIndex = ddl.lastIndexOf(", ");
        if (lastIndex >= 0) {
            ddl.delete(lastIndex, ddl.length());
        }

        ddl.append(");");
        ddl.append(ddlIndex);
        return ddl.toString();
    }
}
