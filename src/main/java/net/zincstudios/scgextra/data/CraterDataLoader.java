package net.zincstudios.scgextra.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.zincstudios.scgextra.SCGExtra;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

public class CraterDataLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(CraterData.class, new CratersDeserializer())
            .setPrettyPrinting()
            .create();
    public CraterData data = new CraterData(List.of());
    public static final CraterDataLoader INSTANCE = new CraterDataLoader();
    private static final String DIRECTORY = "craters";

    public CraterDataLoader() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        jsonMap.forEach((resLoc, jsonElement) -> {
            if (resLoc.getNamespace().equals(SCGExtra.MOD_ID))  {
                try {
                    INSTANCE.data = GSON.fromJson(jsonElement, CraterData.class);
                } catch (Exception e) {
                    SCGExtra.LOGGER.warn("Failed to load crater for some reason: " + resLoc);
                    SCGExtra.LOGGER.warn("Reason: " + e.getMessage());
                }
            }
        });
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    private static class CratersDeserializer implements JsonDeserializer<CraterData> {

        @Override
        public CraterData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject obj = jsonElement.getAsJsonObject();
            return new CraterData(parseCraters(obj.getAsJsonArray("craters")));
        }

        private List<CraterData.Crater> parseCraters(com.google.gson.JsonArray array) {
            List<CraterData.Crater> craters = new ArrayList<>();

            array.forEach(element -> {
                JsonObject obj = element.getAsJsonObject();
                String name = obj.has("name") ? obj.get("name").getAsString() : "";
                int gravity = obj.has("gravity") ? obj.get("gravity").getAsInt() : 0;
                craters.add(new CraterData.Crater(name, gravity));
            });

            return craters;
        }
    }
}