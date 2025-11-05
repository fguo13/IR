package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Search {
    public static void main(String[] args) throws Exception {
        Path indexDir = Paths.get("index-english"); // whichever index you prefer
        Analyzer analyzer = new EnglishAnalyzer();

        DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexDir));
        IndexSearcher searcher = new IndexSearcher(reader);

        // Choose a model
        Similarity similarity;
        System.out.println("Select model: 1=VSM, 2=BM25, 3=LMJM");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String choice = br.readLine();

        switch (choice) {
            case "1":
                similarity = new ClassicSimilarity();
                System.out.println("Using VSM");
                break;
            case "2":
                similarity = new BM25Similarity(1.2f, 0.75f);
                System.out.println("Using BM25");
                break;
            case "3":
                similarity = new LMJelinekMercerSimilarity(0.7f);
                System.out.println("Using LM Jelinek–Mercer (λ=0.7)");
                break;
            default:
                System.out.println("Invalid, using BM25");
                similarity = new BM25Similarity();
        }
        searcher.setSimilarity(similarity);

        QueryParser parser = new QueryParser("contents", analyzer);

        while (true) {
            System.out.print("\nEnter query: ");
            String queryStr = br.readLine();
            if (queryStr == null || queryStr.equalsIgnoreCase("exit")) break;

            Query query = parser.parse(QueryParser.escape(queryStr));
            TopDocs results = searcher.search(query, 5);

            System.out.println("\nTop results for: \"" + queryStr + "\"\n");
            for (ScoreDoc sd : results.scoreDocs) {
                Document doc = searcher.storedFields().document(sd.doc);
                String title = doc.get("title");
                String id = doc.get("docno");
                System.out.printf("DocID=%s | Score=%.4f | %s%n", id, sd.score,
                        title != null ? title : "[No Title]");
            }
        }

        reader.close();
    }
}
