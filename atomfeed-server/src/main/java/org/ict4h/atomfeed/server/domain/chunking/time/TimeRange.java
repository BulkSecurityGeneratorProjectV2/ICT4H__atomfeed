package org.ict4h.atomfeed.server.domain.chunking.time;


import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TimeRange {
    private Timestamp startTimestamp;
    private Timestamp endTimestamp;

    public TimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        this(Timestamp.valueOf(startTime),
                endTime == null ? null : Timestamp.valueOf(endTime));
    }

    public TimeRange(Timestamp startTimestamp, Timestamp endTimestamp){
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public Timestamp getStartTimestamp() {
        return startTimestamp;
    }

    public Timestamp getEndTimestamp() {
        return endTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeRange timeRange = (TimeRange) o;

        if (!startTimestamp.equals(timeRange.startTimestamp)) return false;
        if (endTimestamp != null ? !endTimestamp.equals(timeRange.endTimestamp) : timeRange.endTimestamp != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = startTimestamp.hashCode();
        result = 31 * result + (endTimestamp != null ? endTimestamp.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TimeRange{" +
                "startTime=" + startTimestamp +
                ", endTime=" + endTimestamp +
                '}';
    }
}