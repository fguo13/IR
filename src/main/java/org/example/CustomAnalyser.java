package org.example;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.util.Arrays;

public class CustomAnalyser extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        // Tokenizer: splits text into individual terms
        Tokenizer source = new StandardTokenizer();

        // Build the token processing pipeline
        TokenStream filter = new LowerCaseFilter(source);           // lowercase
        filter = new StopFilter(filter, EnglishAnalyzer.getDefaultStopSet());                // remove stop words
        filter = new PorterStemFilter(filter);                      // stemming

        return new TokenStreamComponents(source, filter);
    }
}
