package Movement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankTransferetdto {
    private String numberAccountSource;
    private String idAccountasource;
    private String numberAccountdestination;
    private String idAccountaDestination;
    private Double amount;
}
