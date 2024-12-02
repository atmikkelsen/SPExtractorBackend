package com.example.SPExtractorBackend.response;
import com.example.SPExtractorBackend.dto.LargeFileDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GraphItemsResponse {
    private List<LargeFileDTO> value;

}
