package com.example.model;

import java.util.List;

public class SearchContextResponse {
    private List<SearchResultItem> searchResults;
    private String llmSummary;

    public SearchContextResponse(List<SearchResultItem> searchResults, String llmSummary) {
        this.searchResults = searchResults;
        this.llmSummary = llmSummary;
    }

    public List<SearchResultItem> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<SearchResultItem> searchResults) {
        this.searchResults = searchResults;
    }

    public String getLlmSummary() {
        return llmSummary;
    }

    public void setLlmSummary(String llmSummary) {
        this.llmSummary = llmSummary;
    }
}
