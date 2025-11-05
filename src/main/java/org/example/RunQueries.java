package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;


import java.io.PrintWriter;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;

public class RunQueries {

    public static void main(String[] args) throws Exception {
        // arg0: analyser name -> english (default) | standard | custom
        String mode = args.length > 0 ? args[0].toLowerCase() : "english";
        Path indexDir = Paths.get(
                mode.equals("standard") ? "index-standard" :
                        mode.equals("custom")   ? "index-custom"   : "index-english"
        );

        Path qryFile = Paths.get("cran/cran.qry");

        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexDir))) {
            // tag includes analyser + similarity so files are self-describing
            runWith(reader, "VSM-"  + mode, new ClassicSimilarity(), qryFile,
                    Paths.get("runs/vsm-"  + mode + ".run"));
            runWith(reader, "BM25-" + mode, new BM25Similarity(1.2f, 0.75f), qryFile,
                    Paths.get("runs/bm25-" + mode + ".run"));
            runWith(reader, "Boolean"  + mode, new BooleanSimilarity(), qryFile,
                    Paths.get("runs/boolean-"  + mode + ".run"));
            runWith(reader, "LMJelinekMercer", new LMJelinekMercerSimilarity(0.7f),
                    qryFile, Paths.get("runs/lmjm0.7-" + mode + ".run"));
            runWith(reader, "LMJelinekMercer", new LMJelinekMercerSimilarity(0.5f),
                    qryFile, Paths.get("runs/lmjm0.5-" + mode + ".run"));
            runWith(reader, "LMJelinekMercer", new LMJelinekMercerSimilarity(0.3f),
                    qryFile, Paths.get("runs/lmjm0.3-" + mode +".run"));

        }
    }

    private static void runWith(DirectoryReader reader, String tag, Similarity similarity,
                                Path qryFile, Path outputFile) throws Exception {

        Files.createDirectories(outputFile.getParent());
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);

        Analyzer analyzer = new EnglishAnalyzer(); // only used to parse queries
        QueryParser parser = new QueryParser("contents", analyzer);

        List<CranfieldParser.Query> queries = CranfieldParser.parseQueries(qryFile);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputFile))) {
            for (CranfieldParser.Query q : queries) {
                Query luceneQuery = parser.parse(QueryParser.escape(q.text));
                TopDocs results = searcher.search(luceneQuery, 1000);

                int rank = 1;
                for (ScoreDoc sd : results.scoreDocs) {
                    Document d = searcher.storedFields().document(sd.doc); // Lucene 10.x way
                    String docno = d.get("docno");
                    writer.printf(Locale.US, "%s 0 %s %d %.6f %s%n",
                            q.id, docno, rank, sd.score, tag);
                    rank++;
                }
            }
        }

        System.out.println("✅ " + tag + " results → " + outputFile.toAbsolutePath());
    }
}
