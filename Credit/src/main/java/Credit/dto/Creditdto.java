package Credit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Creditdto {
    private String creditCardNumber;
    private Double creditLine;
    private String idClient;
    private String documentNumber;
}
