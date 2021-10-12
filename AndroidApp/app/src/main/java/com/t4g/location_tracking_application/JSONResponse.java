package com.t4g.location_tracking_application;
public class JSONResponse {
    private Results[] results;
    private Integer found;

    public Results[] getResults() {
        return results;
    }

    public void setResults(Results[] results) {
        this.results = results;
    }

    public Integer getFound() {
        return found;
    }

    public void setFound(Integer found) {
        this.found = found;
    }
}
