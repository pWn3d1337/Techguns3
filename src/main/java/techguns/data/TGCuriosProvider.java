package techguns.data;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import techguns.Techguns;
import techguns.slot.TGSlotType;

/**
 * Gives players the Curios slots Techguns' extra slot gear needs ({@code data/techguns/curios/entities/techguns.json})
 * and defines the one Curios doesn't have, the mouth slot of the Auto-Feeder ({@code curios/slots/mouth.json}, listed
 * right after the head slot, icon {@code techguns:slot/empty_mouth_slot} from {@code textures/slot}). A plain JSON
 * provider: no dependency on Curios classes.
 */
public class TGCuriosProvider implements DataProvider {
    private final PackOutput output;

    public TGCuriosProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        JsonObject json = new JsonObject();
        JsonArray entities = new JsonArray();
        entities.add("player");
        json.add("entities", entities);
        JsonArray slots = new JsonArray();
        for (TGSlotType slot : TGSlotType.values()) {
            slots.add(slot.curiosId());
        }
        json.add("slots", slots);
        Path data = output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(Techguns.MODID).resolve("curios");
        JsonObject mouth = new JsonObject();
        // Curios' head slot is 40
        mouth.addProperty("order", 45);
        mouth.addProperty("icon", Techguns.MODID + ":slot/empty_mouth_slot");
        JsonArray validators = new JsonArray();
        validators.add("curios:tag");
        mouth.add("validators", validators);
        return CompletableFuture.allOf(DataProvider.saveStable(cache, json, data.resolve("entities/techguns.json")),
                DataProvider.saveStable(cache, mouth, data.resolve("slots/" + TGSlotType.MOUTH.curiosId() + ".json")));
    }

    @Override
    public String getName() {
        return "Techguns Curios entities";
    }
}
