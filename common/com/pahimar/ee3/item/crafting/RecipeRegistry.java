package com.pahimar.ee3.item.crafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.pahimar.ee3.item.WrappedStack;

public class RecipeRegistry {

    private static RecipeRegistry recipeRegistry = null;

    private Multimap<WrappedStack, List<WrappedStack>> recipeMap;
    private List<WrappedStack> discoveredStacks;

    public static Multimap<WrappedStack, List<WrappedStack>> getRecipeMappings() {

        lazyInit();
        return recipeRegistry.recipeMap;
    }

    public static List<WrappedStack> getDiscoveredStacks() {

        lazyInit();
        return Collections.unmodifiableList(recipeRegistry.discoveredStacks);
    }

    private static void lazyInit() {

        if (recipeRegistry == null) {
            recipeRegistry = new RecipeRegistry();
            recipeRegistry.init();
        }
    }

    private void init() {

        recipeMap = HashMultimap.create();

        // Add potion recipes
        recipeMap.putAll(RecipesPotions.getPotionRecipes());

        // Add recipes in the vanilla crafting manager
        recipeMap.putAll(RecipesVanilla.getVanillaRecipes());

        // Add recipes gathered via IMC
        recipeMap.putAll(RecipesIMC.getIMCRecipes());
        
        // Add fluid container recipes
        recipeMap.putAll(RecipesFluidContainers.getFluidContainerRecipes());

        // Discover all stacks that we can
        discoverStacks();
    }

    private void discoverStacks() {

        discoveredStacks = new ArrayList<WrappedStack>();

        // Scan stacks from known recipes
        for (WrappedStack recipeOutput : recipeMap.keySet()) {
            if (!discoveredStacks.contains(new WrappedStack(recipeOutput.getWrappedStack()))) {
                discoveredStacks.add(new WrappedStack(recipeOutput.getWrappedStack()));
            }
            
            for (List<WrappedStack> recipeInputList : recipeMap.get(recipeOutput)) {
                for (WrappedStack recipeInput : recipeInputList) {
                    if (!discoveredStacks.contains(new WrappedStack(recipeInput.getWrappedStack()))) {
                        discoveredStacks.add(new WrappedStack(recipeInput.getWrappedStack()));
                    }
                }
            }
        }

        // Scan stacks from vanilla item array
        for (int i = 0; i < Item.itemsList.length; i++) {
            if (Item.itemsList[i] != null) {
                if (Item.itemsList[i].getHasSubtypes()) {
                    for (int meta = 0; meta < 16; meta++) {
                        WrappedStack wrappedItemStack = new WrappedStack(new ItemStack(Item.itemsList[i].itemID, 1, meta));

                        if (!discoveredStacks.contains(wrappedItemStack)) {
                            discoveredStacks.add(wrappedItemStack);
                        }
                    }
                }
                else {
                    WrappedStack wrappedItemStack = new WrappedStack(Item.itemsList[i]);

                    if (!discoveredStacks.contains(wrappedItemStack)) {
                        discoveredStacks.add(wrappedItemStack);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        // Sort the keys for output to console
        SortedSet<WrappedStack> set = new TreeSet<WrappedStack>();
        set.addAll(recipeMap.keySet());

        for (WrappedStack key : set) {

            Collection<List<WrappedStack>> recipeMappings = recipeMap.get(key);

            for (List<WrappedStack> recipeList : recipeMappings) {
                stringBuilder.append(String.format("Recipe Output: %s, Recipe Input: %s\n", key.toString(), recipeList.toString()));
            }
        }

        return stringBuilder.toString();
    }
}
