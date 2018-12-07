package com.example.demo;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;



@Configuration
public class DBConnection {
	

	public @Bean Bucket loginBucket() {
		Cluster cluster = CouchbaseCluster.create("localhost");
        // cluster.authenticate("shubham", "password");
		return cluster.openBucket("travel-sample", "password");
	}
	
	
}
