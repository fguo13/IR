package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;


import java.io.PrintWriter;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;

public class RunQueries {

    public static void main(String[] args) throws Exception {
        Path indexDir = Paths.get("index");  // folder created by Indexer
        Path qryFile = Paths.get("cran/cran.qry"); // your queries file

        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexDir))) {
            runWith(reader, "VSM", new ClassicSimilarity(), qryFile, Paths.get("runs/vsm.run"));
            runWith(reader, "BM25", new BM25Similarity(1.2f, 0.75f), qryFile, Paths.get("runs/bm25.run"));
        }
    }

    private static void runWith(DirectoryReader reader, String tag, Similarity similarity,
                                Path qryFile, Path outputFile) throws Exception {

        Files.createDirectories(outputFile.getParent());
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);

        Analyzer analyzer = new CustomAnalyser();
        QueryParser parser = new QueryParser("contents", analyzer);

        // parse your Cranfield queries
        List<CranfieldParser.Query> queries = CranfieldParser.parseQueries(qryFile);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile))) {
            for (CranfieldParser.Query q : queries) {
                Query luceneQuery = parser.parse(QueryParser.escape(q.text));
                TopDocs results = searcher.search(luceneQuery, 1000);

                int rank = 1;
                for (ScoreDoc sd : results.scoreDocs) {
                    Document d = searcher.storedFields().document(sd.doc);
                    String docno = d.get("docno");

                    // TREC format: qid Q0 docno rank score tag
                    writer.printf(Locale.US, "%s 0 %s %d %.6f %s%n",
                            q.id, docno, rank, sd.score, tag);
                    rank++;
                }
            }
        }

        System.out.println("✅ " + tag + " results written to " + outputFile.toAbsolutePath());
    }
}
