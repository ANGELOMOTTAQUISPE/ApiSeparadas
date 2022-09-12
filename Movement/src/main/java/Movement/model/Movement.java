package Movement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Movement {
    @Id
    private String idMovement;
    private Double balance;
    private Double movement;
    private String typeMovement;
    private Account account;
    private Credit credit;
    private LocalDateTime movementDate;
    private Double commission;
    private String phoneNumber;
}
