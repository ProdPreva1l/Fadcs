package info.preva1l.fadcs.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import info.preva1l.fadcs.utils.Serializers;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

public class Listings extends ArrayList<StockableItem> {

    public static Listings of(StockableItem... items) {
        Listings listing = new Listings();
        listing.addAll(Arrays.asList(items));
        return listing;
    }

    public static Listings newEmpty() {
        return new Listings();
    }

    public static Listings fromJsonString(String jsonString) {
        JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();

        Listings listings = new Listings();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            boolean isBuying = jsonObject.get("buying").getAsBoolean();
            boolean isSelling = jsonObject.get("selling").getAsBoolean();
            double buyPrice = jsonObject.get("buyPrice").getAsDouble();
            double sellPrice = jsonObject.get("sellPrice").getAsDouble();
            ItemStack itemStack = Serializers.stringToItems(jsonObject.get("itemStack").getAsString())[0];
            int buyStock = jsonObject.get("buyStock").getAsInt();

            listings.add(new StockableItem(isBuying, isSelling, buyPrice, sellPrice, itemStack, buyStock));
        }
        return listings;
    }

    public JsonArray getAsJson() {
        JsonArray jsonArray = new JsonArray();
        for (StockableItem item : this) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("buying", item.buying());
            jsonObject.addProperty("selling", item.selling());
            jsonObject.addProperty("buyPrice", item.buyPrice());
            jsonObject.addProperty("sellPrice", item.sellPrice());
            jsonObject.addProperty("itemStack", Serializers.itemsToString(item.itemStack()));
            jsonObject.addProperty("buyStock", item.buyStock());

            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
}
