package Movement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditMovementdto {
    private Double balance;
    private Double movement;
    private String typeMovement;
    private String idCredit;
    private String creditCardNumber;
    private LocalDateTime movementDate ;
}
