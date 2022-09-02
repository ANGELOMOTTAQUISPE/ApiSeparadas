package Account.exception;
import java.time.LocalDateTime;

public class ExceptionResponse {

    private LocalDateTime fecha;
    private String mensaje;
    private String detalle;

    public ExceptionResponse(LocalDateTime fecha, String mensaje, String detalle) {
        this.fecha = fecha;
        this.mensaje = mensaje;
        this.detalle = detalle;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}
