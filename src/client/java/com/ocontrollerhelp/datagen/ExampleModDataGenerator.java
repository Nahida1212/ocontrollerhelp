package com.ocontrollerhelp.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ExampleModDataGenerator implements DataGeneratorEntrypoint {


    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ExampleModEnglishLangProvider::new);
        pack.addProvider(ExampleModChineseLangProvider::new);



    }


    public class ExampleModEnglishLangProvider extends FabricLanguageProvider {
        protected ExampleModEnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
            // Specifying en_us is optional, as it's the default language code
            super(dataOutput, "en_us", registryLookup);
        }

        @Override
        public void generateTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
            translationBuilder.add("text.example-mod.greeting", "Hello there!");
        }
    }

    public class ExampleModChineseLangProvider extends FabricLanguageProvider {
        protected ExampleModChineseLangProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
            // Specifying en_us is optional, as it's the default language code
            super(dataOutput, "zh_cn", registryLookup);
        }

        @Override
        public void generateTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
            translationBuilder.add("text.example-mod.greeting", "Hello there!");
            translationBuilder.add("key.ocontrollerhelp-mod.lock","锁定");
            translationBuilder.add("key.category.ocontrollerhelp-mod.controller_aim","手柄设置");

        }
    }



}