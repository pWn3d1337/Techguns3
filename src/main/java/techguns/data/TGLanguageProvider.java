package techguns.data;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import techguns.Techguns;

public class TGLanguageProvider extends LanguageProvider {
    public TGLanguageProvider(PackOutput output) {
        super(output, Techguns.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("techguns.configuration.title", "Techguns 3 Configs");
        add("techguns.configuration.section.techguns.common.toml", "Techguns 3 Common Config");
        add("techguns.configuration.section.techguns.common.toml.title", "Techguns 3 Common Config");
        add("techguns.configuration.debugLogging", "Debug Logging");
    }
}
