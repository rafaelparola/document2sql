package com.parola.document2sql.mapper.service;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.parola.document2sql.mapper.entity.SqlColumn;
import com.parola.document2sql.mapper.entity.SqlRelation;
import com.parola.document2sql.mapper.entity.SqlTable;
import com.parola.document2sql.mapper.repository.SqlColumnRepository;
import com.parola.document2sql.mapper.repository.SqlRelationRepository;
import com.parola.document2sql.mapper.repository.SqlTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import javax.print.Doc;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DynamicMongoService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SqlRelationRepository sqlRelationRepository;

    @Autowired
    private SqlTableRepository sqlTableRepository;

    @Autowired
    private SqlColumnRepository sqlColumnRepository;



    //SqlRelation sqlRelation;

    public void analyzeCollectionCardinality(String collectionName) {
        MongoDatabase database = mongoTemplate.getDb();
        MongoCollection<Document> collection = database.getCollection(collectionName);

        // For structures without identifiable unique keys, using string representation as a "structure hash"
        Map<String, Map<String, Integer>> subDocumentStructureOccurrences = new HashMap<>();
        // For arrays, tracking their "structure hash"
        Map<String, Map<String, Integer>> arrayStructureOccurrences = new HashMap<>();

        FindIterable<Document> documents = collection.find();
        for (Document document : documents) {
            analyzeDocument(document, subDocumentStructureOccurrences, arrayStructureOccurrences, null);
        }

        subDocumentStructureOccurrences.forEach((field, structureMap) -> {
            if (structureMap.values().stream().anyMatch(count -> count > 1)) {
                String parentTableName = "";
                String childTableName = "";

                System.out.println("Sub-document field '" + field + "' has a 1-N relationship.");

                if(field.contains(".")) {
                    parentTableName = collectionName+"__" + field.substring(0, field.lastIndexOf(".")).replace(".","__");
                    childTableName = collectionName+"__"+field.replace(".","__");
                    //System.out.println(parentTableName);
                    //System.out.println(childTableName);
                } else {
                    parentTableName = collectionName;
                    childTableName = collectionName+"__"+field.replace(".","__");
                    //System.out.println(parentTableName);
                    //System.out.println(childTableName);
                }

                SqlTable parentTable = sqlTableRepository.findByName(parentTableName);
                SqlTable childTable  = sqlTableRepository.findByName(childTableName);

                SqlRelation sqlRelation = new SqlRelation();
                sqlRelation.setOriginTable(childTable);
                sqlRelation.setReferencedTable(parentTable);
                sqlRelation.setType("1-N");

                SqlColumn foreignKey = new SqlColumn();
                foreignKey.setName(parentTable.getName()+"_id");
                foreignKey.setIsFk(true);
                foreignKey.setDataType("UUID");
                foreignKey.setSqlTable(childTable);
                childTable.setColumn(foreignKey);

                sqlRelationRepository.save(sqlRelation);
                sqlTableRepository.save(childTable);

            } else {
                String parentTableName = "";
                String childTableName = "";
                System.out.println("Sub-document field '" + field + "' has a 1-1 relationship.");

                if(field.contains(".")) {
                    parentTableName = collectionName+"__" + field.substring(0, field.lastIndexOf(".")).replace(".","__").replace(" ", "_");
                    childTableName = collectionName+"__"+field.replace(".","__").replace(" ", "_");
                } else {
                    parentTableName = collectionName.replace(" ", "_");
                    childTableName = collectionName+"__"+field.replace(".","__").replace(" ", "_");
                    System.out.println(parentTableName);
                    System.out.println(childTableName);
                }
                SqlTable parentTable = sqlTableRepository.findByName(parentTableName);
                SqlTable childTable  = sqlTableRepository.findByName(childTableName);

                SqlRelation sqlRelation = new SqlRelation();
                sqlRelation.setOriginTable(childTable);
                sqlRelation.setReferencedTable(parentTable);
                sqlRelation.setType("1-1");

                //SqlColumn foreignKey = new SqlColumn();
                List<SqlColumn> columns = childTable.getColumns();
                for (SqlColumn column : columns) {
                    if (column.isIsPk()) {
                        //childTable.getColumns().get((int) column.getId()).setIsFk(true);
                        column.setIsFk(true);
                        sqlColumnRepository.save(column);
                    }
                }
                /*foreignKey.setName(parentTable.getName()+"_id");
                foreignKey.setIsFk(true);
                foreignKey.setDataType("UUID");
                foreignKey.setSqlTable(childTable);
                childTable.setColumn(foreignKey);*/

                sqlRelationRepository.save(sqlRelation);
                sqlTableRepository.save(childTable);

            }
        });

        arrayStructureOccurrences.forEach((field, structureMap) -> {
            String parentTableName = "";
            String childTableName = "";
            if (structureMap.values().stream().anyMatch(count -> count > 1)) {
                for(int i = 0; i < structureMap.size(); i++) {
                    System.out.println(structureMap);
                }
                System.out.println(structureMap.values());
                System.out.println("Array field '" + field + "' appears to have an M-N relationship across documents. ou seja M-N");
                if(field.contains(".")) {
                    parentTableName = collectionName+"__" + field.substring(0, field.lastIndexOf(".")).replace(".","__");
                    childTableName = collectionName+"__"+field.replace(".","__");
                    //System.out.println(parentTableName);
                    //System.out.println(childTableName);
                } else {
                    parentTableName = collectionName;
                    childTableName = collectionName+"__"+field.replace(".","__");
                    //System.out.println(parentTableName);
                    //System.out.println(childTableName);
                }
                SqlTable sqlTable = new SqlTable(parentTableName+"__R__"+childTableName);

                // Creates the table primary key
                SqlColumn primaryKey = new SqlColumn();
                primaryKey.setName(sqlTable.getName()+"_gen_uuid");
                primaryKey.setIsPk(true);
                primaryKey.setDataType("UUID");
                primaryKey.setSqlTable(sqlTable);
                sqlTable.setColumn(primaryKey);

                SqlTable parentTable = sqlTableRepository.findByName(parentTableName);
                SqlTable childTable  = sqlTableRepository.findByName(childTableName);

                SqlRelation sqlRelationParent = new SqlRelation();
                sqlRelationParent.setOriginTable(sqlTable);
                sqlRelationParent.setReferencedTable(parentTable);
                sqlRelationParent.setType("M-N");

                SqlRelation sqlRelationChild = new SqlRelation();
                sqlRelationChild.setOriginTable(sqlTable);
                sqlRelationChild.setReferencedTable(childTable);
                sqlRelationChild.setType("M-N");

                SqlColumn foreignKeyOriginTable = new SqlColumn();
                foreignKeyOriginTable.setName(parentTable.getName()+"_id");
                foreignKeyOriginTable.setIsFk(true);
                foreignKeyOriginTable.setDataType("UUID");
                foreignKeyOriginTable.setSqlTable(sqlTable);
                sqlTable.setColumn(foreignKeyOriginTable);

                SqlColumn foreignKeyReferencedTable = new SqlColumn();
                foreignKeyReferencedTable.setName(childTable.getName()+"_id");
                foreignKeyReferencedTable.setIsFk(true);
                foreignKeyReferencedTable.setDataType("UUID");
                foreignKeyReferencedTable.setSqlTable(sqlTable);
                sqlTable.setColumn(foreignKeyReferencedTable);

                sqlRelationRepository.save(sqlRelationParent);
                sqlRelationRepository.save(sqlRelationChild);


            } else {
                System.out.println("Array field '" + field + "' does not appear to have an M-N relationship across documents. Ou seja 1-N");
                if(field.contains(".")) {
                    parentTableName = collectionName+"__" + field.substring(0, field.lastIndexOf(".")).replace(".","__");
                    childTableName = collectionName+"__"+field.replace(".","__");
                    //System.out.println(parentTableName);
                    //System.out.println(childTableName);
                } else {
                    parentTableName = collectionName;
                    childTableName = collectionName+"__"+field.replace(".","__");
                    //System.out.println(parentTableName);
                    //System.out.println(childTableName);
                }

                SqlTable parentTable = sqlTableRepository.findByName(parentTableName);
                SqlTable childTable  = sqlTableRepository.findByName(childTableName);

                SqlRelation sqlRelation = new SqlRelation();
                sqlRelation.setOriginTable(childTable);
                sqlRelation.setReferencedTable(parentTable);
                sqlRelation.setType("1-N");

                SqlColumn foreignKey = new SqlColumn();
                foreignKey.setName(parentTable.getName()+"_id");
                foreignKey.setIsFk(true);
                foreignKey.setDataType("UUID");
                foreignKey.setSqlTable(childTable);
                childTable.setColumn(foreignKey);

                sqlRelationRepository.save(sqlRelation);
                sqlTableRepository.save(childTable);
            }
        });
    }

    private void analyzeDocument(Document doc,
                                 Map<String, Map<String, Integer>> subDocumentStructureOccurrences,
                                 Map<String, Map<String, Integer>> arrayStructureOccurrences,
                                 String parentKey) {
        doc.forEach((key, value) -> {
            // Construct a composite key to represent the field's full path
            String fullKey = (parentKey == null) ? key : parentKey + "." + key;

            if (value instanceof Document) {
                // Generate a hash for the top-level document fields only
                String structureHash = generateTopLevelHash((Document) value);
                if (structureHash != null) {
                    subDocumentStructureOccurrences.computeIfAbsent(fullKey, k -> new HashMap<>())
                            .merge(structureHash, 1, Integer::sum);
                }


                analyzeDocument((Document) value, subDocumentStructureOccurrences, arrayStructureOccurrences, fullKey);
            } else if (value instanceof List) {
                // Handle the list of items, considering only top-level items
                ((List<?>) value).stream().distinct().forEach(item -> {
                    // Generate a simple hash based on the item's toString, for immediate items only

                    if (!(item instanceof Document) && !(item instanceof List)) {
                        String structureHash = "Not M-N";
                        arrayStructureOccurrences.computeIfAbsent(fullKey, k -> new HashMap<>())
                                .merge(structureHash, 0, Integer::sum);
                        return;
                    }

                    if (item instanceof List) {
                        String structureHash = generateArrayHash((List<?>) value);

                        if (structureHash != null) {
                            /*Map<String, Integer> occurrencesMap = */arrayStructureOccurrences.computeIfAbsent(fullKey, k -> new HashMap<>())
                                    .merge(structureHash, 1, Integer::sum);
                        }
                    }

                    if (item instanceof Document) {
                        String structureHashObject = generateTopLevelHash((Document) item);


                        if(structureHashObject != null) {
                            arrayStructureOccurrences.computeIfAbsent(fullKey, k -> new HashMap<>())
                                    .merge(structureHashObject, 1, Integer::sum);
                            /*subDocumentStructureOccurrences.computeIfAbsent(fullKey, k -> new HashMap<>())
                                    .merge(structureHashObject, 1, Integer::sum);*/

                        }


                        analyzeDocument((Document) item, subDocumentStructureOccurrences, arrayStructureOccurrences, fullKey);
                    } /*else if (item instanceof List) {
                        // Handle a list within a list (nested array)
                        ((List<?>) item).forEach(nestedItem -> {
                            if (nestedItem instanceof Document) {
                                // If the nested item is a Document, analyze it
                                analyzeDocument((Document) nestedItem, subDocumentStructureOccurrences, arrayStructureOccurrences, fullKey + ".[]");
                            } *//*else {
                                // If the nested item is a List or other type, generate a hash and count it
                                String nestedItemHash = nestedItem.toString();
                                arrayStructureOccurrences.computeIfAbsent(fullKey + ".[]", k -> new HashMap<>())
                                        .merge(nestedItemHash, 1, Integer::sum);
                            }*//*
                        });
                    }*/
                });
            }
        });
    }

    private String generateTopLevelHash(Document document) {
        // This will create a concatenated string of the top-level fields and their values,
        // excluding values that are Documents, Lists (arrays), or null
        String ret = document.entrySet().stream()
                .filter(entry -> !(entry.getValue() instanceof Document)
                        && !(entry.getValue() instanceof List)
                        && entry.getValue() != null)
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));
        if(ret.trim().isEmpty()) {
            ret = null;
        }
        if(ret != null) {
            System.out.println(ret);
        }
        //System.out.println(ret);
        //System.out.println(ret.trim().length());
        return ret;
    }

    private String generateArrayHash(List<?> array) {
        // This will create a concatenated string of all non-null, non-Document elements in the array
        String ret = array.stream()
                .filter(item -> !(item instanceof Document)
                        && !(item instanceof List)
                        && item != null)
                .map(Object::toString)
                .collect(Collectors.joining(","));
        if(ret.trim().isEmpty()) {
            ret = null;
        }
        if(ret != null) {
            System.out.println(ret);
        }
        //System.out.println(ret.trim().length());

        return ret;
    }
}