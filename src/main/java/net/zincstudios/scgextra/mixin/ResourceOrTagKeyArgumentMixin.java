package net.zincstudios.scgextra.mixin;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

@Mixin(ResourceOrTagKeyArgument.class)
public class ResourceOrTagKeyArgumentMixin {

    @Redirect(
        method = "listSuggestions",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/commands/SharedSuggestionProvider;suggestRegistryElements(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/commands/SharedSuggestionProvider$ElementSuggestionType;Lcom/mojang/brigadier/suggestion/SuggestionsBuilder;Lcom/mojang/brigadier/context/CommandContext;)Ljava/util/concurrent/CompletableFuture;"
        )
    )
    private <S> CompletableFuture<Suggestions> filterLocateStructures(
        SharedSuggestionProvider provider,
        ResourceKey<? extends Registry<?>> registryKey,
        SharedSuggestionProvider.ElementSuggestionType type,
        SuggestionsBuilder builder,
        CommandContext<S> context
    ) {
        return provider.suggestRegistryElements(
            registryKey,
            type,
            builder,
            context
        ).thenApply(suggestions -> {
            return new Suggestions(
                suggestions.getRange(),
                suggestions.getList().stream()
                .filter(suggestion ->
                    !suggestion.getText().equals("scgextra:craters")
                )
                .toList()
            );
        });
    }
}