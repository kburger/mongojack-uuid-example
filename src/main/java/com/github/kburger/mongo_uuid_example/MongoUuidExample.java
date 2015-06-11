package com.github.kburger.mongo_uuid_example;

import java.util.UUID;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;

public class MongoUuidExample {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String name;
        private UUID tag;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public UUID getTag() {
            return tag;
        }
        
        public void setTag(UUID tag) {
            this.tag = tag;
        }
    }
    
    public static void main(String[] args) throws Exception {
        // common setup
        MongoClient client = new MongoClient();
        DB db = client.getDB("uuidtest");
        DBCollection coll = db.getCollection("items");
        
        UUID mongoUuid = UUID.fromString("8101b406-5ad7-4384-a7ed-847c917dadec");
        UUID mongojackUuid = UUID.fromString("74ef3a68-03f2-468e-a499-ebf403cd415f");
        
        // mongo-java-driver
        {
            DBObject item = BasicDBObjectBuilder.start("name", "mongo-java-driver example").add("tag", mongoUuid).get();
            coll.insert(item);
            // Storing it as
            //{
            //   "_id" : ObjectId("..."),
            //   "name" : "mongo-java-driver example",
            //   "tag" : BinData(3,"hEPXWga0AYHsrX2RfITtpw==")
            // }
            
            DBObject query = QueryBuilder.start("tag").is(mongoUuid).get();
            com.mongodb.DBCursor cursor = coll.find(query).limit(1);
            System.out.println("mongo-java-driver query for mongo-java-driver inserted document: " + cursor.hasNext());
        }
        
        // mongojack
        {
            Item item = new Item();
            item.setName("mongojack example");
            item.setTag(mongojackUuid);
            
            JacksonDBCollection<Item, Object> jacksonColl = JacksonDBCollection.wrap(coll, Item.class);
            jacksonColl.insert(item);
            // Storing it as
            // {
            //   "_id" : ObjectId("..."),
            //   "name" : "mongojack example",
            //   "tag" : "74ef3a68-03f2-468e-a499-ebf403cd415f"
            // }
            
            // mongo's serialized uuid query
            {
                DBCursor<Item> cursor = jacksonColl.find().is("tag", mongoUuid);
                System.out.println("mongojack query for mongo-java-driver inserted document: " + cursor.hasNext());
            }
            // mongojack's serialized uuid query
            {
                DBCursor<Item> cursor = jacksonColl.find().is("tag", mongojackUuid);
                System.out.println("mongojack query for mongojack inserted document: " + cursor.hasNext());
            }
        }
    }
}
