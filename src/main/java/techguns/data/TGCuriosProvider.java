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
 * Gives players the Curios slots Techguns' extra slot gear needs ({@code data/techguns/curios/entities/techguns.json}).
 * A plain JSON provider: no dependency on Curios classes.
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
        Path path = output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(Techguns.MODID).resolve("curios/entities/techguns.json");
        return DataProvider.saveStable(cache, json, path);
    }

    @Override
    public String getName() {
        return "Techguns Curios entities";
    }
}
