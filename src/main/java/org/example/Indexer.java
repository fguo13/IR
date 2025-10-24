package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

import java.nio.file.*;
import java.util.List;

public class Indexer {
    public static void main(String[] args) throws Exception {
        Path docsPath = Paths.get("cran/cran.all.1400"); // your location
        Path indexDir = Paths.get("index");
        Files.createDirectories(indexDir);

        List<CranfieldParser.Doc> docs = CranfieldParser.parseDocs(docsPath);

        Analyzer analyzer = new CustomAnalyser();
        IndexWriterConfig cfg = new IndexWriterConfig(analyzer);
        cfg.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        try (Directory dir = FSDirectory.open(indexDir);
             IndexWriter w = new IndexWriter(dir, cfg)) {
            for (CranfieldParser.Doc d : docs) {
                Document doc = new Document();
                // Store a canonical docno that matches qrels (Cranfield uses numeric IDs as strings)
                doc.add(new StringField("docno", d.id, Field.Store.YES));
                // Index text fields
                doc.add(new TextField("title", d.title, Field.Store.NO));
                doc.add(new TextField("text", d.text, Field.Store.NO));
                // Convenience field
                doc.add(new TextField("contents", (d.title + " " + d.text).trim(), Field.Store.NO));
                w.addDocument(doc);
            }
            w.commit();
        }
        System.out.println("Indexed " + docs.size() + " docs into " + indexDir.toAbsolutePath());
    }
}
