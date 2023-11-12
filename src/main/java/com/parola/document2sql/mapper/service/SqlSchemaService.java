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

    /*public String getSqlSchemaDdl() {
        List<SqlTable> tables = sqlTableRepository.findAll();

        // Construa um grafo de dependências entre as tabelas
        Map<SqlTable, List<SqlTable>> dependencies = new HashMap<>();
        for (SqlTable table : tables) {
            dependencies.put(table, new ArrayList<>());
        }

        for (SqlTable table : tables) {
            for (SqlRelation relation : table.getRelations()) {
                SqlTable referencedTable = relation.getReferencedTable();
                dependencies.get(referencedTable).add(table);
            }
        }

        // Realize a ordenação topológica
        Set<SqlTable> visited = new HashSet<>();
        Stack<SqlTable> stack = new Stack<>();

        for (SqlTable table : tables) {
            if (!visited.contains(table)) {
                topologicalSort(table, visited, stack, dependencies);
            }
        }

        // Gere as declarações DDL na ordem correta
        StringBuilder ddl = new StringBuilder();
        while (!stack.isEmpty()) {
            SqlTable table = stack.pop();
            // Gere a declaração DDL para a tabela e suas colunas como feito anteriormente
            ddl.append("CREATE TABLE ").append(table.getName()).append(" (");

            List<SqlColumn> columns = table.getColumns();
            for (SqlColumn column : columns) {
                ddl.append(column.getName()).append(" ");
                ddl.append(column.getDataType());

                if (column.isIsPk()) {
                    ddl.append(" PRIMARY KEY DEFAULT gen_random_uuid()");
                }

                if (!column.isIsNullable()) {
                    ddl.append(" NOT NULL");
                }

                // Adicione outras opções de coluna, se necessário

                ddl.append(", ");
            }

            // Adicione as declarações de relação
            List<SqlRelation> relations = table.getRelations();
            for (SqlRelation relation : relations) {
                ddl.append("FOREIGN KEY (").append(relation.getReferencedTable().getName()).append("_id) ");
                ddl.append("REFERENCES ").append(relation.getReferencedTable().getName()).append(", ");
            }

            // Remova a vírgula extra no final das declarações
            if (ddl.charAt(ddl.length() - 2) == ',') {
                ddl.deleteCharAt(ddl.length() - 2);
            }

            ddl.append(");\n");
        }

        return ddl.toString();
    }

    private void topologicalSort(SqlTable table, Set<SqlTable> visited, Stack<SqlTable> stack, Map<SqlTable, List<SqlTable>> dependencies) {
        visited.add(table);

        for (SqlTable dependentTable : dependencies.get(table)) {
            if (!visited.contains(dependentTable)) {
                topologicalSort(dependentTable, visited, stack, dependencies);
            }
        }

        stack.push(table);
    }*/

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
        ddl.append("CREATE TABLE ").append(table.getName()).append(" (");

        for (SqlColumn column : table.getColumns()) {
            ddl.append(column.getName()).append(" ")
                    .append(column.getDataType());

            if (column.isIsPk()) {
                ddl.append(" PRIMARY KEY DEFAULT gen_random_uuid()");
            }

            if (!column.isIsNullable()) {
                ddl.append(" NOT NULL");
            }

            ddl.append(", ");
        }

        // Append foreign key constraints
        if (relations != null) {
            for (SqlRelation relation : relations) {
                SqlTable referencedTable = relation.getReferencedTable();
                ddl.append("FOREIGN KEY (").append(referencedTable.getName()).append("_id) ")
                        .append("REFERENCES ").append(referencedTable.getName()).append(", ");
            }
        }

        // Remove the trailing comma and space
        int lastIndex = ddl.lastIndexOf(", ");
        if (lastIndex >= 0) {
            ddl.delete(lastIndex, ddl.length());
        }

        ddl.append(");");
        return ddl.toString();
    }

    /*public String getSqlSchemaDdl() {
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
            relationsMap.computeIfAbsent(relation.getReferencedTable().getId(), k -> new ArrayList<>()).add(relation);
        }

        return relationsMap;
    }

    private String createTableDdl(SqlTable table, List<SqlRelation> relations) {
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE ").append(table.getName()).append(" (");

        for (SqlColumn column : table.getColumns()) {
            ddl.append(column.getName()).append(" ")
                    .append(column.getDataType());

            if (column.isIsPk()) {
                ddl.append(" PRIMARY KEY DEFAULT gen_random_uuid()");
            }

            if (!column.isIsNullable()) {
                ddl.append(" NOT NULL");
            }

            ddl.append(", ");
        }

        // Append foreign key constraints
        if (relations != null) {
            for (SqlRelation relation : relations) {
                ddl.append("FOREIGN KEY (").append(table.getName()).append("_id) ")
                        .append("REFERENCES ").append(relation.getOriginTable().getName()).append(", ");
            }
        }

        // Remove the trailing comma and space
        int lastIndex = ddl.lastIndexOf(", ");
        if (lastIndex >= 0) {
            ddl.delete(lastIndex, ddl.length());
        }

        ddl.append(");");
        return ddl.toString();
    }*/


}
