package com.parola.document2sql.mapper.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.parola.document2sql.mapper.repository.SqlSchemaRepository;
import com.parola.document2sql.mapper.service.JsonSchemaService;
import com.parola.document2sql.mapper.service.SqlSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

import com.parola.document2sql.mapper.repository.JsonSchemaRepository;
import com.parola.document2sql.mapper.entity.JsonSchema;

@RestController
@RequestMapping(path = "/JsonSchema", produces = "application/json", consumes = "application/json")
public class JsonSchemaController {

    @Autowired
    JsonSchemaService jsonSchemaService;

    @PostMapping("/load")
    public ResponseEntity<String> loadJsonSchema(@RequestBody JsonNode payload){
        // Persist the schema in the database

        System.out.println(payload.toString());
        System.out.println(payload.get("theaters"));
        jsonSchemaService.saveJsonSchema(payload);

        return ResponseEntity.status(HttpStatus.OK).body("The schema was successfully loaded");
    }

    @GetMapping("/map-to-sql/{schemaId}")
    public ResponseEntity<String> mapToSql(@PathVariable long schemaId){

        try {
            jsonSchemaService.mapToSql(schemaId);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no schema for the given id: " + schemaId);
        }
          catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.status(HttpStatus.OK).body("The mapping was succesfull");
    }
}
