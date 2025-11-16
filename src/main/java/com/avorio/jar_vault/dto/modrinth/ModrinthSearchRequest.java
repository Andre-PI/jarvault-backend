package com.avorio.jar_vault.dto.modrinth;

import java.util.List;

public class ModrinthSearchRequest {
    private String query;
    private List<String> facets; // ["project_type:mod"], ["categories:fabric"], etc.
    private String index; // "relevance", "downloads", "follows", "newest", "updated"
    private Integer offset;
    private Integer limit;

    public ModrinthSearchRequest() {
    }

    public ModrinthSearchRequest(String query, List<String> facets, String index, Integer offset, Integer limit) {
        this.query = query;
        this.facets = facets;
        this.index = index;
        this.offset = offset;
        this.limit = limit;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getFacets() {
        return facets;
    }

    public void setFacets(List<String> facets) {
        this.facets = facets;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}

