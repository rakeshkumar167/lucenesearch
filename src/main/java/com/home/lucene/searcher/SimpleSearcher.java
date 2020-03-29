package com.home.lucene.searcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Simple searcher , for searching through documents based on one field only.
 */
public class SimpleSearcher {
    /**
     * The path where the indexes are stored
     */
    private final String indexPath;

    public SimpleSearcher(String indexPath) throws IOException {
        this.indexPath = indexPath;
    }

    public JsonArray search(String searchColumn, String query, int maxResults) throws IOException, ParseException {
        Directory index = FSDirectory.open(Paths.get(indexPath));
        Query q = new QueryParser(searchColumn, new StandardAnalyzer()).parse(query);

        int hitsPerPage = maxResults;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        // 4. display results
        System.out.println("Found " + hits.length + " hits. TotalHits"+docs.totalHits);
        JsonArray resultArray = new JsonArray();
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            JsonObject obj = new JsonObject();
            for(IndexableField field:d.getFields()){
                obj.addProperty(field.name(), d.get(field.name()));
            }
            resultArray.add(obj);
        }
        return resultArray;
    }
}
