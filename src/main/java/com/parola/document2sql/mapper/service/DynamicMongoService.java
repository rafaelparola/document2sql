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
                System.out.println("Array field '" + field + "' appears to have an M-N relationship across documents. ou seja M-N");

            } else {
                System.out.println("Array field '" + field + "' does not appear to have an M-N relationship across documents. Ou seja 1-N");
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
                // Check for identifiable unique keys, e.g., "_id"
                Object id = ((Document) value).get("_id");

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
                    }
                });
            }
        });
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
