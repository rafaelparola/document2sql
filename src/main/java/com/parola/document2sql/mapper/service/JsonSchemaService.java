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

    public static void printAttributes(JsonNode node) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                System.out.println("Attribute: " + entry.getKey());
                printAttributes(entry.getValue());
            });
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                printAttributes(element);
            }
        } else {
            // Handle leaf nodes (values)
            System.out.println("Value: " + node);
        }
    }

    public void mapToSql(long jsonSchemaId) throws JsonProcessingException {
        JsonSchema jsonSchema = this.getJsonSchemaById(jsonSchemaId);
        JsonNode node = this.getSchemaJsonNodeInJsonSchema(jsonSchema);

        System.out.println(node);
        System.out.println(node.isArray());
        System.out.println(node.isObject());
        System.out.println(node.getNodeType());

        System.out.println(node.get("restaurants"));
        JsonNode restaurants = node.get("restaurants");

        SqlTable sqlTable = new SqlTable("restaurants");
        this.createSqlObjects(restaurants, sqlTable);

        //createSqlObjects(restaurants);

    }

    public void createSqlObjects(JsonNode schema) {
        createSqlObjects(schema, null);
    }


    public void createSqlObjectsArray(JsonNode schema, SqlTable parentTable) {
        for (Iterator<String> it = schema.fieldNames(); it.hasNext(); ) {
            String type = it.next();

            if(type != OBJECT && type != ARRAY) {
                SqlColumn column = new SqlColumn();

                column.setName(parentTable.getName() + type);
                column.setSqlTable(parentTable);

                // Set the column object in the parent table
                parentTable.setColumn(column);
            }
            if(type == OBJECT) {
                SqlTable childTable = new SqlTable(parentTable.getName() + type);

                JsonNode structure = this.getArrayObjectStructure(schema);

                this.createSqlObjects(structure, childTable);
            }

        }
        sqlTableRepository.save(parentTable);
    }

    public void createSqlObjects(JsonNode schema, SqlTable parentTable) {
        for (Iterator<String> it = schema.fieldNames(); it.hasNext(); ) {

            // Get the actual Object
            String fieldName = it.next();
            JsonNode fieldNameNode = schema.get(fieldName);
            if (fieldName != "_id") {
                // Get the JSON node which contains the types of the given fieldName Object
                JsonNode nodeType = getNodeObjectType(fieldNameNode);
                // Get the first type in string (Change later)
                String  type = getStringObjectType(nodeType);

                if (type != OBJECT && type != ARRAY) {

                    // Creates the column object
                    SqlColumn column = new SqlColumn();

                    for (Iterator<String> attribute = fieldNameNode.fieldNames(); attribute.hasNext();) {
                        String attributeName = attribute.next();
                        if (attributeName == PRIMARY_KEY){
                            column.setIsPk(true);
                        } else if (attributeName == FOREIGN_KEY) {
                            column.setIsFk(true);
                        } else if (attributeName == KEY) {
                            column.setIsUnique(true);
                            column.setIsNullable(false);
                        }
                    }

                    column.setName(fieldName);
                    column.setSqlTable(parentTable);
                    // Populates the table object with the column object
                    parentTable.setColumn(column);

                } else if (type == OBJECT) {
                    // Creates child table
                    SqlTable childTable = new SqlTable(fieldName);

                    // Creates the relation between child table and parent table (Change later)
                    SqlRelation relation = new SqlRelation();
                    // Investigar melhor como fazer 1-1
                    relation.setType("1-N");
                    relation.setReferencedTable(parentTable);
                    childTable.setRelation(relation);

                    // Get the childNodeStructure
                    JsonNode childNodeStructure = this.getObjectStructure(fieldNameNode);
                    this.createSqlObjects(childNodeStructure, childTable);
                } else if (type == ARRAY) {
                    SqlTable childArrayTable = new SqlTable(fieldName);

                    // Creates the relation between child table and parent table (Change later)

                    // Gets Array Structure
                    JsonNode arrayStructure = getArrayStructure(fieldNameNode);

                    // Creates sqlTable from Array
                    this.createSqlObjectsArray(arrayStructure, childArrayTable);

                }
                sqlTableRepository.save(parentTable);

            }



/*
            JsonNode nodeStructure = null;

            if (fieldName != "_id") {
                System.out.println(nodeType.fieldNames().next());
                if (nodeType.fieldNames().next() == OBJECT ) {
                    // Creates child table
                    SqlTable childTable = new SqlTable(fieldName);
                    // Creates the relation
                    SqlRelation sqlRelation = new SqlRelation();
                    sqlRelation.setType("ManyToOne");
                    // Set the relation between two tables
                    sqlRelation.setReferencedTable(parentTable);
                    childTable.setRelation(sqlRelation);

                    nodeStructure = getStructure(nodeType, type);
                    System.out.println(nodeStructure);
                    //if (fieldName!)
                    createSqlObjects(nodeStructure, childTable);
                } else if (nodeType.fieldNames().next() == ARRAY) {


                    //--if(fieldName == OBJECT || fieldName == ARRAY){
                    fieldName = parentTable.getName();
                    //}

                    // Creates child table
                    *//**//*SqlTable childTable = null;
                    if(type != OBJECT && type != ARRAY) {
                        childTable = new SqlTable(fieldName);

                        // Creates the relation
                        SqlRelation sqlRelation = new SqlRelation();
                        sqlRelation.setType("ManyToOne");
                        // Set the relation between two tables
                        sqlRelation.setReferencedTable(parentTable);
                        childTable.setRelation(sqlRelation);
                    }*//**//*



                    nodeStructure = getStructure(nodeType, type);
                    nodeStructure = getNodeObjectType(nodeStructure);
                    System.out.println(nodeStructure);

                    //if (childTable != null){
                    //    createSqlObjects(nodeStructure, childTable);
                    createSqlObjects(nodeStructure, parentTable);
                    //}*//*

                }
                else {
                    //if(fieldName == OBJECT || fieldName == ARRAY){
                    fieldName = parentTable.getName();
                    //}
                    *//*SqlColumn sqlColumn = new SqlColumn();
                    sqlColumn.setName(fieldName);
                    sqlColumn.set*//*
                    parentTable.setColumn(new SqlColumn(fieldName, nodeType.fieldNames().next(), true, parentTable));
                }*/
                //sqlTableRepository.save(parentTable);


        }
    }

    /*public void createSqlObjects(JsonNode schema, SqlTable parentTable) {
        for (Iterator<String> it = schema.fieldNames(); it.hasNext(); ) {
            String fieldName = it.next();
            System.out.println(fieldName);
            //System.out.println(schema.get(fieldName));
            //System.out.println(getNodeObjectType(schema.get(fieldName)));
            //System.out.println(TYPES);
            JsonNode nodeType = null;
            if (schema.get(fieldName).fieldNames().next() == TYPES) {
                nodeType = getNodeObjectType(schema.get(fieldName));
            } else {
                nodeType = schema;
            }
            //JsonNode nodeType = getNodeObjectType(schema.get(fieldName));
            String  type = getStringObjectType(nodeType);
            JsonNode nodeStructure = null;

            if (fieldName != "_id") {
                System.out.println(nodeType.fieldNames().next());
                if (nodeType.fieldNames().next() == OBJECT ) {
                    // Creates child table
                    SqlTable childTable = new SqlTable(fieldName);
                    // Creates the relation
                    SqlRelation sqlRelation = new SqlRelation();
                    sqlRelation.setType("ManyToOne");
                    // Set the relation between two tables
                    sqlRelation.setReferencedTable(parentTable);
                    childTable.setRelation(sqlRelation);

                    nodeStructure = getStructure(nodeType, type);
                    System.out.println(nodeStructure);
                    //if (fieldName!)
                    createSqlObjects(nodeStructure, childTable);
                } else if (nodeType.fieldNames().next() == ARRAY) {


                    //--if(fieldName == OBJECT || fieldName == ARRAY){
                    fieldName = parentTable.getName();
                    //}

                    // Creates child table
                    *//*SqlTable childTable = null;
                    if(type != OBJECT && type != ARRAY) {
                        childTable = new SqlTable(fieldName);

                        // Creates the relation
                        SqlRelation sqlRelation = new SqlRelation();
                        sqlRelation.setType("ManyToOne");
                        // Set the relation between two tables
                        sqlRelation.setReferencedTable(parentTable);
                        childTable.setRelation(sqlRelation);
                    }*//*



                    nodeStructure = getStructure(nodeType, type);
                    nodeStructure = getNodeObjectType(nodeStructure);
                    System.out.println(nodeStructure);

                    //if (childTable != null){
                    //    createSqlObjects(nodeStructure, childTable);
                    createSqlObjects(nodeStructure, parentTable);
                    //}

                }
                else {
                    //if(fieldName == OBJECT || fieldName == ARRAY){
                    fieldName = parentTable.getName();
                    //}
                    *//*SqlColumn sqlColumn = new SqlColumn();
                    sqlColumn.setName(fieldName);
                    sqlColumn.set*//*
                    parentTable.setColumn(new SqlColumn(fieldName, nodeType.fieldNames().next(), true, parentTable));
                }
                sqlTableRepository.save(parentTable);
            }

        }
    }*/

    // Não esquecer que existem fields com mais de um tipo ex numero e string
    public JsonNode getNodeObjectType(JsonNode node) {
        return node.get(TYPES);
    }

    public String getStringObjectType(JsonNode node) {
        List<String> objectTypes = null;

        /*for(Iterator<String> itTypes = node.fieldNames(); itTypes.hasNext();){
            String innerFieldName = itTypes.next();
            objectTypes.add(innerFieldName);
            System.out.println(innerFieldName);
        }*/


        return node.fieldNames().next();
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
