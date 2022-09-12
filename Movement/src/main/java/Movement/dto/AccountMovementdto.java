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
public class AccountMovementdto {

    private Double movement;
    private String typeMovement;
    private String idAccount;
    private String accountNumber;
    private String debitcardnumber;
    private LocalDateTime movementDate ;
    private String phoneNumber;
}
