package com.parola.document2sql.mapper.service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parola.document2sql.mapper.entity.JsonSchema;
import com.parola.document2sql.mapper.entity.SqlSchema;
import com.parola.document2sql.mapper.repository.JsonSchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JsonSchemaService {

    @Autowired
    private JsonSchemaRepository jsonSchemaRepository;

    @Autowired
    SqlSchemaService sqlSchemaService;

    @Autowired
    private ObjectMapper objectMapper;

    public void saveJsonSchema(Map<String, Object> payload) {
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
            throw new NullPointerException("JSON schema not found with ID: " + jsonSchemaId);
        }
    }

    public JsonNode getSchemaJsonNodeInJsonSchema(JsonSchema jsonSchema) {
        String jsonString = jsonSchema.getSchema();
        try {
            // Parse the JSON string into a JsonNode using Spring Boot's ObjectMapper
            JsonNode jsonNode = objectMapper.readTree(jsonString);

            // Print the JSON data to the system out
            System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
            return jsonNode;
        } catch (IOException e) {
            // Handle any exceptions that may occur during JSON parsing
            e.printStackTrace();
        }
        return null;
    }

    public void mapToSql(long jsonSchemaId) {
        JsonSchema jsonSchema = this.getJsonSchemaById(jsonSchemaId);
        JsonNode node = this.getSchemaJsonNodeInJsonSchema(jsonSchema);

        sqlSchemaService.saveSqlSchema(jsonSchema);

    }
}
