package modelo;

/** Resultado del cálculo de estado de un tipo de servicio de una moto (inmutable). */
public class EstadoServicio {

    private final TipoServicio tipo;
    private final EstadoMantenimiento estado;
    private final int proximoKm;
    private final int kmRestantes;

    public EstadoServicio(TipoServicio tipo, EstadoMantenimiento estado, int proximoKm, int kmRestantes) {
        this.tipo = tipo;
        this.estado = estado;
        this.proximoKm = proximoKm;
        this.kmRestantes = kmRestantes;
    }

    public TipoServicio getTipo() { return tipo; }
    public EstadoMantenimiento getEstado() { return estado; }
    public int getProximoKm() { return proximoKm; }
    public int getKmRestantes() { return kmRestantes; }

    @Override
    public String toString() {
        if (estado == EstadoMantenimiento.SIN_HISTORIAL) {
            return tipo + ": Sin historial";
        }
        if (kmRestantes < 0) {
            return tipo + ": " + estado.getEtiqueta() + " | tocaba a los " + proximoKm
                    + " km | excedido por " + (-kmRestantes) + " km";
        }
        return tipo + ": " + estado.getEtiqueta() + " | próximo a los " + proximoKm
                + " km | restan " + kmRestantes + " km";
    }
}
