package com.example.demo;

import static org.assertj.core.api.Assertions.from;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.Statement;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.DesignDocument;
import com.couchbase.client.java.view.SpatialView;
import com.couchbase.client.java.view.SpatialViewQuery;
import com.couchbase.client.java.view.SpatialViewResult;
import com.couchbase.client.java.view.SpatialViewRow;
import com.couchbase.client.java.view.View;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;

@RestController
public class RestService {

	@Autowired
	public Bucket bucket;

	@RequestMapping("create")
	public String createView() {
		ArrayList<View> mapViewsList = new ArrayList<View>();

		mapViewsList.add(DefaultView.create("by_country",
				"function (doc, meta) { if (doc.type == 'landmark') { emit([doc.country, doc.city], null); } }"));
		mapViewsList.add(DefaultView.create("by_activity",
				"function (doc, meta) { if (doc.type == 'landmark') { emit(doc.activity, null); } }", "_count"));
				
		ArrayList<View> spatialViewsList = new ArrayList<View>();
		
		spatialViewsList.add(SpatialView.create("by_coordinates",
				"function (doc, meta) { if (doc.type == 'landmark') { emit([doc.geo.lon, doc.geo.lat], null); } }"));

		DesignDocument mapDesignDocument =  DesignDocument.create("travelview", mapViewsList);
		DesignDocument spatialDesignDocument = DesignDocument.create("spatialtravelview", spatialViewsList);
		
		

		// Insert design document into the bucket
		 bucket.bucketManager().insertDesignDocument(mapDesignDocument);
		 bucket.bucketManager().insertDesignDocument(spatialDesignDocument);
		 return "true";
	}

	@RequestMapping("call1")
	public String callTravelView() {
		// Statement query = Select.select("*").from("beer-sample");
		// N1qlQuery q1 = N1qlQuery.simple("select address from `beer-sample`;");

		ViewResult result = bucket.query(ViewQuery.from("travelview", "by_country").limit(5));
		for (ViewRow row : result) {
			System.out.println(row); // prints the row
			System.out.println(row.document().content()); // retrieves the doc and prints content
		}

		return "true";
	}

	@RequestMapping("call2")
	public String callSpatialView() {
		 SpatialViewQuery q = SpatialViewQuery.from("spatialtravelview", "by_coordinates").limit(5);
		 SpatialViewResult res = bucket.query(q);
		
		 for (SpatialViewRow row : res) {
		 System.out.println("Key:" + row.key());
		 System.out.println("Value:" + row.value());
		 System.out.println("Geometry:" + row.geometry());
		 }
		return "true";
	}
	@RequestMapping("close")
	public Boolean closeBucket() {
		return bucket.close();
	}
	
	@RequestMapping(" ")
	public void createIndex() {
		
		N1qlQuery index = N1qlQuery.simple("CREATE INDEX Index1_travel_country\r\n" + 
				"	    ON `travel-sample`(country)\r\n" + 
				"	    WHERE type=\"airline\" USING GSI");
	    
	
		N1qlQueryResult result = bucket.query(index);
		
	}

	public void updateView() {
		
		// Get design document to be updated
		DesignDocument designDoc = bucket.bucketManager().getDesignDocument("travelview");

		// Update the "by_country" view, adding a reduce
		designDoc.views().add(DefaultView.create("by_country", // reuse same name
				"function (doc, meta) { if (doc.type == 'landmark') { emit([doc.country, doc.city], null); } }", 
				"_count" // added reduce function
		));

		// Resend to server
		bucket.bucketManager().upsertDesignDocument(designDoc);
	}

	public void deleteDesignDoc(String desingDoc) {
		bucket.bucketManager().removeDesignDocument(desingDoc);
	}
}
