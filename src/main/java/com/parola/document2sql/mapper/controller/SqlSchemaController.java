package com.parola.document2sql.mapper.controller;

import com.parola.document2sql.mapper.service.SqlSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/SqlSchema", produces = "application/json", consumes = "application/json")
public class SqlSchemaController {

    @Autowired
    SqlSchemaService sqlSchemaService;

    @GetMapping("/sql-schema-ddl")
    public ResponseEntity<String> getSqlSchemaDdl(){

        String ddl;
        ddl = sqlSchemaService.getSqlSchemaDdl();

        return ResponseEntity.status(HttpStatus.OK).body(ddl);
    }
}
