package com.home.lucene.indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.List;

/**
 * Simple Lucene indexer to index a list of documents to a specified path
 */
public class SimpleIndexer {

    /**
     * The path to which the indexes will be stored
     */
    private final String indexPath;

    public SimpleIndexer(String indexPath) throws IOException {
        this.indexPath = indexPath;
    }

    public void write(List<Map<String, Object>> values, Map<String, Field> fieldNameToTypeMap) throws IOException {
        if(values == null || values.size() ==0){
            return;
        }
        IndexWriter writer = getWriter();

        for(int i=0;i<values.size();i++){
            Map<String, Object> value = values.get(i);
            org.apache.lucene.document.Document ldoc = new org.apache.lucene.document.Document();

            for(String key:value.keySet()){
                Field field = fieldNameToTypeMap.get(key);
                if(field == null){
                    continue;
                }

                switch(field){
                    case StringField:ldoc.add(new StringField(key, (String)value.get(key), org.apache.lucene.document.Field.Store.YES));
                    break;
                    case TextField:ldoc.add(new TextField(key, (String)value.get(key), org.apache.lucene.document.Field.Store.YES));
                    break;

                }

            }
            writer.addDocument(ldoc);
        }
        writer.commit();
        writer.close();

    }

    private IndexWriter getWriter() throws IOException {

        Directory dir = FSDirectory.open(Paths.get(indexPath));

        //analyzer with the default stop words
        Analyzer analyzer = new StandardAnalyzer();

        //IndexWriter Configuration
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        //IndexWriter writes new index files to the directory
        IndexWriter writer = new IndexWriter(dir, iwc);
        return writer;
    }

}
