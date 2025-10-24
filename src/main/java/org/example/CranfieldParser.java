package org.example;

import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class CranfieldParser {

    public static class Doc {
        public final String id, title, authors, bib, text;
        Doc(String id, String title, String authors, String bib, String text) {
            this.id = id;
            this.title = title;
            this.authors = authors;
            this.bib = bib;
            this.text = text;
        }
    }

    public static class Query {
        public final String id, text;
        Query(String id, String text) { this.id = id; this.text = text; }
    }

    public static class Qrel {
        public final String qid, docid;
        public final int grade;
        Qrel(String qid, String docid, int grade) {
            this.qid = qid.trim();
            this.docid = docid;
            this.grade = grade;
        }
    }

    // ✅ Parse documents (.I .T .A .B .W)
    public static List<Doc> parseDocs(Path p) throws Exception {
        String all = Files.readString(p);
        List<Doc> docs = new ArrayList<>();

        for (String block : all.split("\n\\.I\\s+")) {
            block = block.trim();
            if (block.isEmpty()) continue;
            String[] lines = block.split("\n");
            String raw = lines[0].trim();
            String id = raw.replaceFirst("^\\.I\\s*", ""); // remove ".I "
            String title = extract(block, "\\.T\\s*([\\s\\S]*?)(?=\\n\\.[ABW]|$)");
            String auth  = extract(block, "\\.A\\s*([\\s\\S]*?)(?=\\n\\.[TBW]|$)");
            String bib   = extract(block, "\\.B\\s*([\\s\\S]*?)(?=\\n\\.[TAW]|$)");
            String text  = extract(block, "\\.W\\s*([\\s\\S]*?)$");
            docs.add(new Doc(id, nz(title), nz(auth), nz(bib), nz(text)));
        }
        return docs;
    }

    // ✅ Parse queries with automatic renumbering (1..225)
    public static List<Query> parseQueries(Path p) throws Exception {
        String all = Files.readString(p);
        List<Query> qs = new ArrayList<>();

        for (String block : all.split("\n\\.I\\s+")) {
            block = block.trim();
            if (block.isEmpty()) continue;
            String text = extract(block, "\\.W\\s*([\\s\\S]*?)$");
            int newId = qs.size() + 1;
            qs.add(new Query(String.valueOf(newId), nz(text)));
        }
        return qs;
    }

    // ✅ Parse qrels (handles both 3-col and 4-col formats)
    public static List<Qrel> parseQrels(Path p) throws Exception {
        List<Qrel> res = new ArrayList<>();
        for (String line : Files.readAllLines(p)) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] t = line.split("\\s+");
            String qid, doc;
            int grade;
            if (t.length >= 4) { // TREC format: qid 0 docid grade
                qid = t[0];
                doc = t[2];
                grade = Integer.parseInt(t[3]);
            } else { // older format: qid docid grade
                qid = t[0];
                doc = t[1];
                grade = Integer.parseInt(t[2]);
            }
            res.add(new Qrel(qid, doc, grade));
        }
        return res;
    }

    private static String extract(String s, String regex) {
        Matcher m = Pattern.compile(regex, Pattern.DOTALL).matcher(s);
        return m.find() ? m.group(1).trim() : "";
    }

    private static String nz(String s) { return s == null ? "" : s.trim(); }
}
