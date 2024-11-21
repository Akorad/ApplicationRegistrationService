package ru.Darvin.DTO;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class TicketSummaryPageDTO {
    private List<TicketSummaryDTO> content;
    private int totalPages;
    private long totalElements;

    public TicketSummaryPageDTO(Page<TicketSummaryDTO> page) {
        this.content = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
    }
}
