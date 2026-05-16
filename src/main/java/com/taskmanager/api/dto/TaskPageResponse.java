package com.taskmanager.api.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Paginated task search response.")
public class TaskPageResponse {

    @Schema(description = "Tasks returned for the current page.")
    private List<TaskResponse> content;

    @Schema(description = "Current page number.", example = "0")
    private int number;

    @Schema(description = "Requested page size.", example = "10")
    private int size;

    @Schema(description = "Number of tasks in the current page.", example = "2")
    private int numberOfElements;

    @Schema(description = "Total number of matching tasks.", example = "42")
    private long totalElements;

    @Schema(description = "Total number of pages.", example = "5")
    private int totalPages;

    @Schema(description = "Whether this is the first page.", example = "true")
    private boolean first;

    @Schema(description = "Whether this is the last page.", example = "false")
    private boolean last;

    @Schema(description = "Whether the page contains no tasks.", example = "false")
    private boolean empty;
}
