package com.home.lucene.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SimpleHTTPServer {
    
    public static void main(String[] args) throws IOException, URISyntaxException {
        int port = Integer.parseInt(args[0]);//port
        String idx_dir = args[1];//directory where index is stored
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new Home());
        server.createContext("/search", new QueryHandler(idx_dir));
        server.setExecutor(null); // creates a default executor
        server.start();
    }


    static class Home implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream is = SimpleHTTPServer.class.getClassLoader().getResourceAsStream("index.html");
            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            IOUtils.copy(is, os);
            os.close();
        }
    }

    static class QueryHandler implements HttpHandler {
        private final String INDEX_DIR;
        public QueryHandler(String idx_dir){
            INDEX_DIR = idx_dir;
        }
        @Override
        public void handle(HttpExchange t) throws IOException {

            String query = queryToMap(t.getRequestURI().getQuery()).get("q");
            Directory index = FSDirectory.open(Paths.get(INDEX_DIR));
            Query q = null;
            try {
                q = new QueryParser("video_title", new StandardAnalyzer()).parse(query);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // 3. search
            int hitsPerPage = 500;
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(q, hitsPerPage);
            ScoreDoc[] hits = docs.scoreDocs;
            // 4. display results
            System.out.println("Found " + hits.length + " hits. TotalHits" + docs.totalHits);
            JsonArray resultArray = new JsonArray();
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                JsonObject obj = new JsonObject();
                for (IndexableField field : d.getFields()) {
                    obj.addProperty(field.name(), d.get(field.name()));
                }
                resultArray.add(obj);
            }

            t.sendResponseHeaders(200, 0);
            t.getResponseHeaders().set("Content-Type", "application/json");
            OutputStream os = t.getResponseBody();
            os.write(resultArray.toString().getBytes());
            os.close();
        }


        public Map<String, String> queryToMap(String query) {
            Map<String, String> result = new HashMap<>();
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], entry[1]);
                } else {
                    result.put(entry[0], "");
                }
            }
            return result;
        }
    }

}
