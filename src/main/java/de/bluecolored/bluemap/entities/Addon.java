package de.bluecolored.bluemap.entities;

import de.bluecolored.bluemap.core.map.hires.entity.EntityRendererType;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.mca.entity.EntityType;
import de.bluecolored.bluemap.entities.entity.*;
import de.bluecolored.bluemap.entities.renderer.*;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Addon implements Runnable {

    public static Logger LOGGER = Logger.getLogger("BlueMap Entities Addon");

    @Override
    public void run() {
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("llama"), Llama.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("trader_llama"), TraderLlama.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("bee"), Bee.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("cat"), Cat.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("ocelot"), Ocelot.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("chicken"), AgeVariantEntity.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("fox"), Fox.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("pig"), Pig.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("tropical_fish"), TropicalFish.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("zombie"), Zombie.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("husk"), Zombie.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("drowned"), Zombie.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("cow"), AgeVariantEntity.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("sheep"), Sheep.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("skeleton"), Skeleton.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("wither_skeleton"), WitherSkeleton.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("stray"), Stray.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("bogged"), Bogged.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("dolphin"), AgeEntity.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("squid"), Squid.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("glow_squid"), GlowSquid.class));


        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("llama"), LlamaRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("trader_llama"), LlamaRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("bee"), BeeRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("cat"), CatRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("ocelot"), OcelotRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("chicken"), ChickenRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("fox"), FoxRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("pig"), PigRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("tropical_fish"), TropicalFishRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("zombie"), ZombieRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("husk"), HuskRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("drowned"), DrownedRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("cow"), CowRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("sheep"), SheepRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("skeleton"), SkeletonRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("wither_skeleton"), SkeletonRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("stray"), SkeletonRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("bogged"), SkeletonRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("dolphin"), DolphinRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("squid"), SquidRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("glow_squid"), SquidRenderer::new));
    }

}
