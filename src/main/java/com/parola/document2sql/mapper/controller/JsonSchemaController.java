package com.parola.document2sql.mapper.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.parola.document2sql.mapper.repository.JsonSchemaRepository;
import com.parola.document2sql.mapper.entity.JsonSchema;

@RestController
@RequestMapping(path = "/JsonSchema", produces = "application/json", consumes = "application/json")
public class JsonSchemaController {

    @Autowired
    JsonSchemaRepository jsonSchemaRepository;


    @PostMapping("/load")
    public ResponseEntity<String> loadJsonSchema(@RequestBody Map<String, Object> payload){

        // Generates a JsonSchema Object and populates the schema attribute with the mongo db schema input
        JsonSchema jsonSchema = new JsonSchema();
        jsonSchema.setSchema(payload.toString());

        // Persist the schema in the database
        jsonSchemaRepository.save(jsonSchema);

        return ResponseEntity.status(HttpStatus.OK).body("The schema was successfully loaded");
    }
}
