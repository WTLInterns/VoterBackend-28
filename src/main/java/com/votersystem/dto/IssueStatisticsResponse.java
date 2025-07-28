package com.votersystem.dto;

public class IssueStatisticsResponse {
    private long totalIssues;
    private long openIssues;
    private long inProgressIssues;
    private long resolvedIssues;
    private long closedIssues;
    private long reopenedIssues;
    private long issuesToday;
    private long issuesThisWeek;
    private long issuesThisMonth;
    private long overdueIssues;
    
    // Constructors
    public IssueStatisticsResponse() {}
    
    public IssueStatisticsResponse(long totalIssues, long openIssues, long inProgressIssues, 
                                 long resolvedIssues, long closedIssues, long reopenedIssues,
                                 long issuesToday, long issuesThisWeek, long issuesThisMonth, 
                                 long overdueIssues) {
        this.totalIssues = totalIssues;
        this.openIssues = openIssues;
        this.inProgressIssues = inProgressIssues;
        this.resolvedIssues = resolvedIssues;
        this.closedIssues = closedIssues;
        this.reopenedIssues = reopenedIssues;
        this.issuesToday = issuesToday;
        this.issuesThisWeek = issuesThisWeek;
        this.issuesThisMonth = issuesThisMonth;
        this.overdueIssues = overdueIssues;
    }
    
    // Getters and Setters
    public long getTotalIssues() { return totalIssues; }
    public void setTotalIssues(long totalIssues) { this.totalIssues = totalIssues; }
    
    public long getOpenIssues() { return openIssues; }
    public void setOpenIssues(long openIssues) { this.openIssues = openIssues; }
    
    public long getInProgressIssues() { return inProgressIssues; }
    public void setInProgressIssues(long inProgressIssues) { this.inProgressIssues = inProgressIssues; }
    
    public long getResolvedIssues() { return resolvedIssues; }
    public void setResolvedIssues(long resolvedIssues) { this.resolvedIssues = resolvedIssues; }
    
    public long getClosedIssues() { return closedIssues; }
    public void setClosedIssues(long closedIssues) { this.closedIssues = closedIssues; }
    
    public long getReopenedIssues() { return reopenedIssues; }
    public void setReopenedIssues(long reopenedIssues) { this.reopenedIssues = reopenedIssues; }
    
    public long getIssuesToday() { return issuesToday; }
    public void setIssuesToday(long issuesToday) { this.issuesToday = issuesToday; }
    
    public long getIssuesThisWeek() { return issuesThisWeek; }
    public void setIssuesThisWeek(long issuesThisWeek) { this.issuesThisWeek = issuesThisWeek; }
    
    public long getIssuesThisMonth() { return issuesThisMonth; }
    public void setIssuesThisMonth(long issuesThisMonth) { this.issuesThisMonth = issuesThisMonth; }
    
    public long getOverdueIssues() { return overdueIssues; }
    public void setOverdueIssues(long overdueIssues) { this.overdueIssues = overdueIssues; }
}
