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
        *//*String ddl = "Meu create table";

        List<SqlTable> tables = sqlTableRepository.findAll();

        for (SqlTable table : tables) {
            // Faça algo com o objeto "table" aqui
            // Por exemplo, você pode acessar os atributos da tabela:
            String tableName = table.getTableName();
            int tableId = table.getTableId();

            // Realize as operações necessárias com os dados da tabela
        }*//*

        List<SqlTable> tables = sqlTableRepository.findAll();

        StringBuilder ddl = new StringBuilder();

        for (SqlTable table : tables) {
            // Crie a declaração da tabela
            ddl.append("CREATE TABLE ").append(table.getName()).append(" (");

            // Adicione as declarações das colunas
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

                // Adicione outras opções de relação, se necessário
            }

            // Remova a vírgula extra no final das declarações
            if (ddl.charAt(ddl.length() - 2) == ',') {
                ddl.deleteCharAt(ddl.length() - 2);
            }

            ddl.append(");\n");
        }


        return ddl.toString();
    }*/

    public String getSqlSchemaDdl() {
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
    }


}
