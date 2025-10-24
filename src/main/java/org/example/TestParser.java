package org.example;

import java.nio.file.*;

public class TestParser {
    public static void main(String[] args) throws Exception {
        var docs = CranfieldParser.parseDocs(Paths.get("cran/cran.all.1400"));
        var queries = CranfieldParser.parseQueries(Paths.get("cran/cran.qry"));
        var qrels = CranfieldParser.parseQrels(Paths.get("cran/QRelsCorrectedforTRECeval"));

        System.out.println("Docs parsed: " + docs.size());
        System.out.println("Queries parsed: " + queries.size());
        System.out.println("Qrels parsed: " + qrels.size());

        System.out.println("\n--- First Document ---");
        System.out.println("ID: " + docs.get(0).id);
        System.out.println("Title: " + docs.get(0).title);

        System.out.println("\n--- First Query ---");
        System.out.println("ID: " + queries.get(0).id);
        System.out.println("Text: " + queries.get(0).text);

        System.out.println("\n--- First Qrel ---");
        System.out.println("Query ID: " + qrels.get(0).qid);
        System.out.println("Doc ID: " + qrels.get(0).docid);
        System.out.println("Grade: " + qrels.get(0).grade);
    }
}
