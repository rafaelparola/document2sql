package com.parola.document2sql.mapper.service;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
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
            //if (structureMap.size() == 1) {
            if (structureMap.values().stream().anyMatch(count -> count > 1)) {
                System.out.println("Sub-document field '" + field + "' has a 1-N relationship.");

            } else {
                System.out.println("Sub-document field '" + field + "' has a 1-1 relationship.");

            }
        });

        arrayStructureOccurrences.forEach((field, structureMap) -> {
            if (structureMap.values().stream().anyMatch(count -> count > 1)) {
                /*for(int i = 0; i < structureMap.size(); i++) {
                    System.out.println(structureMap.);
                }*/
                System.out.println(structureMap.values());
                System.out.println("Array field '" + field + "' appears to have an M-N relationship across documents. ou seja M-N");

            } else {
                System.out.println("Array field '" + field + "' does not appear to have an M-N relationship across documents. Ou seja 1-N");
            }
        });
    }

    /*private void analyzeDocument(Document doc,
                                 Map<String, Map<String, Integer>> subDocumentStructureOccurrences,
                                 Map<String, Map<String, Integer>> arrayStructureOccurrences,
                                 String parentKey) {
        doc.forEach((key, value) -> {
            // Construct a composite key to represent the field's full path
            String fullKey = (parentKey == null) ? key : parentKey + "." + key;

            if (value instanceof Document) {

                String structureHash = value.toString(); // Using the string representation as a hash
                subDocumentStructureOccurrences.computeIfAbsent(fullKey, k -> new HashMap<>())
                        .merge(structureHash, 1, Integer::sum);

                // Recursively analyze the nested document
                analyzeDocument((Document) value, subDocumentStructureOccurrences, arrayStructureOccurrences, fullKey);

            } else if (value instanceof List) {
                // Recursively analyze each item in the array if it's a document
                ((List<?>) value).stream().distinct().forEach(item -> {
                    // Use the item itself as the hash for occurrence tracking
                    Map<String, Integer> occurrencesMap = arrayStructureOccurrences.computeIfAbsent(fullKey, k -> new HashMap<>());
                    // Increment the count for this unique item
                    occurrencesMap.merge(item.toString(), 1, Integer::sum);
                    //System.out.println(item.toString());
                    if (item instanceof Document) {
                        analyzeDocument((Document) item, subDocumentStructureOccurrences, arrayStructureOccurrences, fullKey);
                    } else if (item instanceof List) {

                    } else {

                    }
                });
            }
        });
    }*/
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
                    String structureHash = generateArrayHash((List<?>) value);

                    if (structureHash != null) {
                        /*Map<String, Integer> occurrencesMap = */arrayStructureOccurrences.computeIfAbsent(fullKey, k -> new HashMap<>())
                                .merge(structureHash, 1, Integer::sum);
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
                    }
                });
            }
        });
    }

    /*private String generateTopLevelHash(Document document) {
        // This will create a simple concatenated string of the top-level fields and their values
        return document.entrySet().stream()
                .filter(entry -> !(entry.getValue() instanceof Document) && !(entry.getValue() instanceof List))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));

        String ret = document.entrySet().stream()
                .filter(entry -> !(entry.getValue() instanceof Document) && !(entry.getValue() instanceof List))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));
        System.out.println(ret);
        return ret;
    }*/

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

    // It words
    /*public void analyzeCollectionCardinality(String collectionName) {
        MongoDatabase database = mongoTemplate.getDb();
        MongoCollection<Document> collection = database.getCollection(collectionName);

        // For structures with identifiable unique keys
        Map<String, Set<Object>> uniqueObjectTracker = new HashMap<>();
        // For structures without identifiable unique keys, using string representation as a "structure hash"
        Map<String, Map<String, Integer>> subDocumentStructureOccurrences = new HashMap<>();
        // For arrays, tracking their "structure hash"
        Map<String, Map<String, Integer>> arrayStructureOccurrences = new HashMap<>();

        FindIterable<Document> documents = collection.find();
        for (Document document : documents) {
            analyzeDocument(document, uniqueObjectTracker, subDocumentStructureOccurrences, arrayStructureOccurrences, null);
        }

        // Determine cardinality based on occurrences
        uniqueObjectTracker.forEach((key, valueSet) -> {
            if (valueSet.size() == 1) {
                System.out.println("Field '" + key + "' with identifiable keys has a 1-1 relationship.");
            } else {
                System.out.println("Field '" + key + "' with identifiable keys has a 1-N relationship.");
            }
        });

        subDocumentStructureOccurrences.forEach((field, structureMap) -> {
            //if (structureMap.size() == 1) {
            if (structureMap.values().stream().anyMatch(count -> count > 1)) {
                System.out.println("Sub-document field '" + field + "' has a 1-N relationship.");

            } else {
                System.out.println("Sub-document field '" + field + "' has a 1-1 relationship.");

            }
        });

        arrayStructureOccurrences.forEach((field, structureMap) -> {
            if (structureMap.values().stream().anyMatch(count -> count > 1)) {
                System.out.println("Array field '" + field + "' appears to have an M-N relationship across documents. ou seja M-N");

            } else {
                System.out.println("Array field '" + field + "' does not appear to have an M-N relationship across documents. Ou seja 1-N");
            }
        });
    }

    private void analyzeDocument(Document doc,
                                 Map<String, Set<Object>> uniqueObjectTracker,
                                 Map<String, Map<String, Integer>> subDocumentStructureOccurrences,
                                 Map<String, Map<String, Integer>> arrayStructureOccurrences,
                                 String parentKey) {
        doc.forEach((key, value) -> {
            // Construct a composite key to represent the field's full path
            String fullKey = (parentKey == null) ? key : parentKey + "." + key;

            if (value instanceof Document) {
                // Check for identifiable unique keys, e.g., "_id"
                Object id = ((Document) value).get("_id");
                if (id != null) {
                    uniqueObjectTracker.computeIfAbsent(fullKey, k -> new HashSet<>()).add(id);
                } else {
                    String structureHash = value.toString(); // Using the string representation as a hash
                    subDocumentStructureOccurrences.computeIfAbsent(fullKey, k -> new HashMap<>())
                            .merge(structureHash, 1, Integer::sum);

                    // Recursively analyze the nested document
                    analyzeDocument((Document) value, uniqueObjectTracker, subDocumentStructureOccurrences, arrayStructureOccurrences, fullKey);
                }
            } else if (value instanceof List) {
                //String listHash = value.toString(); // Using the string representation as a hash for the entire list
                //arrayStructureOccurrences.computeIfAbsent(fullKey, k -> new HashMap<>())
                //        .merge(listHash, 1, Integer::sum);

                // Recursively analyze each item in the array if it's a document
                ((List<?>) value).stream().distinct().forEach(item -> {
                    // Use the item itself as the hash for occurrence tracking
                    Map<String, Integer> occurrencesMap = arrayStructureOccurrences.computeIfAbsent(fullKey, k -> new HashMap<>());
                    // Increment the count for this unique item
                    occurrencesMap.merge(item.toString(), 1, Integer::sum);
                    //System.out.println(item.toString());
                    if (item instanceof Document) {
                        analyzeDocument((Document) item, uniqueObjectTracker, subDocumentStructureOccurrences, arrayStructureOccurrences, fullKey);
                    }
                });
            }
        });
    }
}*/
