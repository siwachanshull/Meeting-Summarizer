package com.ai.SpringAiProject.Dto;

public record MeetingSummaryResponse(
        String title,
        String fileName,
        String transcript,
        String summary
) {
}
