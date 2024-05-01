package info.preva1l.fadcs.shops;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PurchaseMode {
    BUY(0),
    SELL(1);
    private final int id;
}
