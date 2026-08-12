package de.bluecolored.bluemap.entities;

import de.bluecolored.bluemap.core.map.hires.entity.EntityRendererType;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.mca.entity.EntityType;
import de.bluecolored.bluemap.core.world.mca.entity.MCAEntity;
import de.bluecolored.bluemap.entities.entity.*;
import de.bluecolored.bluemap.entities.renderer.*;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Addon implements Runnable {

    public static Logger LOGGER = Logger.getLogger("BlueMap Entities Addon");

    /** all baby/adult models where "Age" is a number */
    private static final String[] AGE_TYPES = {
            "goat", "hoglin", "panda", "sniffer", "turtle", "strider",
            "zoglin", "polar_bear", "nautilus", "villager"
    };

    /** all baby/dault models where "IsBaby" is a bool */
    private static final String[] BABY_TYPES = { "piglin", "zombified_piglin", "zombie_villager" };

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
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("parched"), Parched.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("dolphin"), AgeEntity.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("squid"), Squid.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("glow_squid"), GlowSquid.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("horse"), Horse.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("zombie_horse"), ZombieHorse.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("skeleton_horse"), SkeletonHorse.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("donkey"), Donkey.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("mule"), Mule.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("armadillo"), Armadillo.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("snow_golem"), SnowGolem.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("axolotl"), Axolotl.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("camel"), Camel.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("camel_husk"), CamelHusk.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("copper_golem"), CopperGolem.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("frog"), AgeVariantEntity.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("ghast"), MCAEntity.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("happy_ghast"), HappyGhast.class));
        for (String type : AGE_TYPES)
            EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft(type), AgeEntity.class));
        for (String type : BABY_TYPES)
            EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft(type), BabyEntity.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("mooshroom"), Mooshroom.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("rabbit"), Rabbit.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("parrot"), Parrot.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("wolf"), Wolf.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("salmon"), Salmon.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("armor_stand"), ArmorStand.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("pufferfish"), Pufferfish.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("slime"), CubeMob.class));
        EntityType.REGISTRY.register(new EntityType.Impl(Key.minecraft("magma_cube"), CubeMob.class));


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
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("parched"), SkeletonRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("dolphin"), DolphinRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("squid"), SquidRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("glow_squid"), SquidRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("horse"), HorseRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("zombie_horse"), HostileHorseRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("skeleton_horse"), HostileHorseRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("donkey"), ChestedHorseRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("mule"), ChestedHorseRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("armadillo"), ArmadilloRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("snow_golem"), SnowGolemRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("axolotl"), AxolotlRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("camel"), CamelRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("camel_husk"), CamelRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("copper_golem"), CopperGolemRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("frog"), FrogRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("ghast"), GhastRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("happy_ghast"), GhastRenderer::new));
        for (String type : AGE_TYPES)
            EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft(type), AgeRenderer::new));
        for (String type : BABY_TYPES)
            EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft(type), BabyRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("mooshroom"), VariantRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("rabbit"), VariantRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("parrot"), VariantRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("wolf"), WolfRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("salmon"), SalmonRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("armor_stand"), ArmorStandRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("pufferfish"), PufferfishRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("slime"), CubeRenderer::new));
        EntityRendererType.REGISTRY.register(new EntityRendererType.Impl(Key.minecraft("magma_cube"), CubeRenderer::new));
    }

}
