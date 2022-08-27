package Movement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
@Document("movement")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movement {
    @Id
    private String idMovement;
    private Double balance;
    private Double movement;
    private String type;
    private Account account;
    private Credit credit;
    private LocalDateTime movementDate ;
}
