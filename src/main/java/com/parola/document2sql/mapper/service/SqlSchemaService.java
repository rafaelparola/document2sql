package com.parola.document2sql.mapper.service;

import com.parola.document2sql.mapper.entity.*;
import com.parola.document2sql.mapper.repository.SqlColumnRepository;
import com.parola.document2sql.mapper.repository.SqlRelationRepository;
import com.parola.document2sql.mapper.repository.SqlSchemaRepository;
import com.parola.document2sql.mapper.repository.SqlTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        /*String ddl = "Meu create table";

        List<SqlTable> tables = sqlTableRepository.findAll();

        for (SqlTable table : tables) {
            // Faça algo com o objeto "table" aqui
            // Por exemplo, você pode acessar os atributos da tabela:
            String tableName = table.getTableName();
            int tableId = table.getTableId();

            // Realize as operações necessárias com os dados da tabela
        }*/

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
    }
}
