package com.parola.document2sql.mapper.service;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parola.document2sql.mapper.entity.*;
import com.parola.document2sql.mapper.repository.JsonSchemaRepository;
import com.parola.document2sql.mapper.repository.SqlColumnRepository;
import com.parola.document2sql.mapper.repository.SqlRelationRepository;
import com.parola.document2sql.mapper.repository.SqlTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JsonSchemaService {

    @Autowired
    private JsonSchemaRepository jsonSchemaRepository;

    @Autowired
    private SqlTableRepository sqlTableRepository;

    @Autowired
    private SqlColumnRepository sqlColumnRepository;

    @Autowired
    private SqlRelationRepository sqlRelationRepository;

    @Autowired
    SqlSchemaService sqlSchemaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    DynamicMongoService dynamicMongoService;

    public static final String TYPES = "types";
    public static final String PRIMARY_KEY = "primaryKey";
    public static final String KEY = "key";
    public static final String FOREIGN_KEY = "foreignKey";
    public static final String REFERENCES = "references";
    public static final String OBJECT = "Object";
    public static final String STRUCTURE = "structure";
    public static final  String ARRAY = "Array";

    public void saveJsonSchema(JsonNode payload) {
        // Generates a JsonSchema Object and populates the schema attribute with the mongo db schema input
        JsonSchema jsonSchema = new JsonSchema();
        jsonSchema.setSchema(payload.toString());

        // Persist the schema in the database
        jsonSchemaRepository.save(jsonSchema);
    }

    public List<JsonSchema> getAllJsonSchemas() {
        return jsonSchemaRepository.findAll();
    }

    public JsonSchema getJsonSchemaById(long jsonSchemaId) {
        // Get the JsonSchema by ID
        Optional<JsonSchema> jsonSchemaOptional = jsonSchemaRepository.findById(jsonSchemaId);

        if (jsonSchemaOptional.isPresent()) {
            return jsonSchemaOptional.get();
        } else {
            throw new NoSuchElementException("JSON schema not found with ID: " + jsonSchemaId);
        }
    }

    public JsonNode getSchemaJsonNodeInJsonSchema(JsonSchema jsonSchema) {
        String jsonString = jsonSchema.getSchema();
        try {
            // Parse the JSON string into a JsonNode using Spring Boot's ObjectMapper

            JsonNode jsonNode = objectMapper.readTree(jsonString.substring( 1, jsonString.length() - 1 ).replace("\\", "") );
            //JsonNode jsonNode = objectMapper.readTree(jsonString);

            // Print the JSON data to the system out
            System.out.println(jsonNode);
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
            return jsonNode;
        } catch (IOException e) {
            // Handle any exceptions that may occur during JSON parsing
            e.printStackTrace();
        }
        return null;
    }

    public void mapToSql(long jsonSchemaId) throws JsonProcessingException {
        JsonSchema jsonSchema = this.getJsonSchemaById(jsonSchemaId);
        JsonNode node = this.getSchemaJsonNodeInJsonSchema(jsonSchema);

        System.out.println(node);
        System.out.println(node.isArray());
        System.out.println(node.isObject());
        System.out.println(node.getNodeType());


        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String tableName = it.next().replace(" ", "_");
            SqlTable sqlTable = new SqlTable(tableName);
            sqlTable.setCreatedDateAndTime(new Date());
            sqlTable.setLevel(1); // Set as first json lvl
            JsonNode tableNode = node.get(tableName);
            System.out.println(sqlTable.getName());
            this.createSqlObjects(tableNode, sqlTable);

            dynamicMongoService.analyzeCollectionCardinality(tableName);
        }


    }


    public void createSqlObjectsArray(JsonNode schema, SqlTable parentTable) {
        // Creates the table primary key
        SqlColumn primaryKey = new SqlColumn();
        primaryKey.setName(parentTable.getName()+"_gen_uuid");
        primaryKey.setIsPk(true);
        primaryKey.setDataType("UUID");
        primaryKey.setSqlTable(parentTable);
        parentTable.setColumn(primaryKey);

        for (Iterator<String> it = schema.fieldNames(); it.hasNext(); ) {
                    String type = it.next();

            if(type != OBJECT && type != ARRAY && type != "Null") {
                SqlColumn column = new SqlColumn();

                column.setName(parentTable.getName().replace(" ", "_") + type);
                if(type == "number"){
                    column.setDataType("float");
                } else {
                    column.setDataType(type);
                }
                column.setSqlTable(parentTable);

                // Set the column object in the parent table
                parentTable.setColumn(column);
            }
            if(type == OBJECT) {
                JsonNode structure = this.getArrayObjectStructure(schema);

                this.createSqlObjects(structure, parentTable);
            }

        }
        sqlTableRepository.save(parentTable);
    }

    public void createSqlObjects(JsonNode schema, SqlTable parentTable) {
        // Creates the table primary key

        if (!parentTable.getColumns().stream().anyMatch(SqlColumn::isIsPk)) {
            SqlColumn primaryKey = new SqlColumn();
            primaryKey.setName(parentTable.getName()+"_gen_uuid");
            primaryKey.setIsPk(true);
            primaryKey.setDataType("UUID");
            primaryKey.setSqlTable(parentTable);
            parentTable.setColumn(primaryKey);
        }

        for (Iterator<String> it = schema.fieldNames(); it.hasNext(); ) {

            // Get the actual Object
            String fieldName = it.next();
            JsonNode fieldNameNode = schema.get(fieldName);
            if (fieldName != "_id") {
                // Get the JSON node which contains the types of the given fieldName Object
                JsonNode nodeType = getNodeObjectType(fieldNameNode);
                // Get the first type in string (Change later)
                String  type = getStringObjectType(nodeType);

                if (type != OBJECT && type != ARRAY && type != "Null") {

                    // Creates the column object
                    SqlColumn column = new SqlColumn();

                    for (Iterator<String> attribute = fieldNameNode.fieldNames(); attribute.hasNext();) {
                        String attributeName = attribute.next().replace(" ", "_");
                        /*if (attributeName == PRIMARY_KEY){
                            column.setIsPk(true);
                        }*/ /*else if (attributeName == FOREIGN_KEY) {
                            column.setIsFk(true);
                        }*/ /*else*/ if (attributeName == KEY) {
                            column.setIsUnique(true);
                            column.setIsNullable(false);
                        }
                    }

                    column.setName(fieldName.replace(" ", "_"));
                    if(type == "number"){
                        column.setDataType("float");
                    } else {
                        column.setDataType(type);
                    }
                    column.setSqlTable(parentTable);
                    // Populates the table object with the column object
                    parentTable.setColumn(column);

                } else if (type == OBJECT) {
                    // Creates child table
                    SqlTable childTable = new SqlTable(parentTable.getName() +"__"+ fieldName.replace(" ", "_"));
                    childTable.setCreatedDateAndTime(new Date());
                    childTable.setLevel(parentTable.getLevel() + 1);
                    // Creates the relation between child table and parent table (Change later)
                       //SqlRelation relation = new SqlRelation();
                    // If it is one object containing one object is 1-1????
                    // Perguntar ao professor como é possível realizar uma 1-1 nesse cenário
                    // N pra um zips vai possuir muitos loc
                       //relation.setType("1-N");
                       //relation.setReferencedTable(parentTable);
                       //childTable.setRelation(relation);

                    // Creates child table reference key
                    /*SqlColumn foreignKey = new SqlColumn();
                    foreignKey.setName(parentTable.getName()+"_id");
                    foreignKey.setIsFk(true);
                    foreignKey.setDataType("UUID");
                    foreignKey.setSqlTable(childTable);
                    childTable.setColumn(foreignKey);*/

                    // Get the childNodeStructure
                    JsonNode childNodeStructure = this.getObjectStructure(fieldNameNode);
                    this.createSqlObjects(childNodeStructure, childTable);
                } else if (type == ARRAY) {
                    SqlTable childArrayTable = new SqlTable(parentTable.getName() +"__"+fieldName.replace(" ", "_"));

                    // Creates child table reference key
                    /*SqlColumn foreignKey = new SqlColumn();
                    foreignKey.setName(parentTable.getName()+"_id");
                    foreignKey.setIsFk(true);
                    foreignKey.setDataType("UUID");
                    foreignKey.setSqlTable(childArrayTable);
                    childArrayTable.setColumn(foreignKey);*/
                    //childArrayTable.setLevel(parentTable.getLevel() + 1);
                    // Creates the relation between child table and parent table (Change later)
                       //SqlRelation relation = new SqlRelation();
                    // If it is one object containing one object is 1-1????
                    // Perguntar ao professor como é possível realizar uma 1-1 nesse cenário
                    // N pra um zips vai possuir muitos loc
                       //relation.setType("1-N");
                       //relation.setReferencedTable(parentTable);
                       //childArrayTable.setRelation(relation);


                    // Gets Array Structure
                    JsonNode arrayStructure = getArrayStructure(fieldNameNode);

                    // Creates sqlTable from Array
                    this.createSqlObjectsArray(arrayStructure, childArrayTable);

                }
                System.out.println(parentTable.getName());
                sqlTableRepository.save(parentTable);

            }
        }
    }

    // Não esquecer que existem fields com mais de um tipo ex numero e string
    public JsonNode getNodeObjectType(JsonNode node) {
        return node.get(TYPES);
    }

    /*public String getStringObjectType(JsonNode node) {
        for(Iterator<String> it = node.fieldNames(); it.hasNext();) {

            if (it.next() != "Null") {
                return it.next();
            }
        }
        return node.fieldNames().next();
    }*/

    public String getStringObjectType(JsonNode node) {
        for (Iterator<String> it = node.fieldNames(); it.hasNext();) {
            String fieldName = it.next(); // Store the next field name
            if (!"Null".equals(fieldName)) { // Correct string comparison
                return fieldName; // Return the stored field name
            }
        }
        // If there are no field names or all field names are "Null", return null or some default value
        return null;
    }

    public void createRelations() {

    }

    public JsonNode getStructure(JsonNode node, String objectType) {
        return node.get(objectType).get(STRUCTURE);
    }

    public JsonNode getObjectStructure(JsonNode node) {
        return node.get(TYPES).get(OBJECT).get(STRUCTURE);
    }

    public JsonNode getArrayStructure(JsonNode node) {
        return node.get(TYPES).get(ARRAY).get(STRUCTURE).get(TYPES);
    }

    public JsonNode getArrayObjectStructure(JsonNode node) {
        return node.get(OBJECT).get(STRUCTURE);
    }

}
