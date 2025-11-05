package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;

import java.nio.file.*;
import java.util.List;

public class Indexer {
    public static void main(String[] args) throws Exception {
        Path docsPath = Paths.get("cran/cran.all.1400");

        // Choose analyser
        String mode = args.length > 0 ? args[0].toLowerCase() : "english";
        Analyzer analyzer;
        Path indexDir;

        if (mode.equals("standard")) {
            analyzer = new StandardAnalyzer();
            indexDir = Paths.get("index-standard");
        } else {
            analyzer = new EnglishAnalyzer();
            indexDir = Paths.get("index-english");
        }

        Files.createDirectories(indexDir);
        List<CranfieldParser.Doc> docs = CranfieldParser.parseDocs(docsPath);

        IndexWriterConfig cfg = new IndexWriterConfig(analyzer);
        cfg.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        try (Directory dir = FSDirectory.open(indexDir);
             IndexWriter w = new IndexWriter(dir, cfg)) {
            for (CranfieldParser.Doc d : docs) {
                Document doc = new Document();
                doc.add(new StringField("docno", d.id, Field.Store.YES));
                doc.add(new TextField("title", d.title, Field.Store.YES));
                doc.add(new TextField("text", d.text, Field.Store.NO));
                doc.add(new TextField("contents", (d.title + " " + d.text).trim(), Field.Store.NO));
                w.addDocument(doc);
            }
            w.commit();
        }

        System.out.println("Indexed " + docs.size() + " docs with " + mode + " analyser into " + indexDir);
    }
}
