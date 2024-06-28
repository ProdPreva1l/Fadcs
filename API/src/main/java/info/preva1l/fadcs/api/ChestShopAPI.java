package info.preva1l.fadcs.api;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
public abstract class ChestShopAPI {
    @Getter private static ChestShopAPI instance = null;



    /**
     * Set the instance.
     * @throws IllegalStateException if the instance is already assigned
     */
    @ApiStatus.Internal
    public static void setInstance(ChestShopAPI newInstance) {
        if (instance != null) {
            throw new IllegalStateException("Instance has already been set");
        }
        instance = newInstance;
    }
}
