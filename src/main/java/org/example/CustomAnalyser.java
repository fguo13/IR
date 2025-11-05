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
        Tokenizer source = new StandardTokenizer();
        TokenStream filter = new LowerCaseFilter(source);
        filter = new StopFilter(filter, EnglishAnalyzer.getDefaultStopSet());
        filter = new PorterStemFilter(filter);

        return new TokenStreamComponents(source, filter);
    }
}
