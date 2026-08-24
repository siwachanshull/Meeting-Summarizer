package com.ai.SpringAiProject.Services;

import com.ai.SpringAiProject.Dto.MeetingSummaryResponse;
import com.ai.SpringAiProject.Exceptions.AudioProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MeetingService {

    private final TranscriptionService transcriptionService;
    private final SummaryService summaryService;

    public MeetingService(TranscriptionService transcriptionService, SummaryService summaryService) {
        this.transcriptionService = transcriptionService;
        this.summaryService = summaryService;
    }

    public MeetingSummaryResponse processAudioFile(MultipartFile audioFile, String title) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new AudioProcessingException("Please upload a valid audio file.");
        }

        String transcript = transcriptionService.transcribe(audioFile);
        String summary = summaryService.summarize(transcript);

        String finalTitle = (title == null || title.isBlank())
                ? "Meeting Summary"
                : title.trim();

        return new MeetingSummaryResponse(
                finalTitle,
                audioFile.getOriginalFilename(),
                transcript,
                summary
        );
    }
}
