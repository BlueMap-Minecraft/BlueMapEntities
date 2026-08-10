
function bme_test:kill

execute positioned ~ ~ ~0 run function bme_test:group/sheep
execute positioned ~ ~ ~4 run function bme_test:group/sheep_states
execute positioned ~ ~ ~8 run function bme_test:group/cow
execute positioned ~ ~ ~12 run function bme_test:group/pig
execute positioned ~ ~ ~16 run function bme_test:group/chicken
execute positioned ~ ~ ~20 run function bme_test:group/cat
execute positioned ~ ~ ~24 run function bme_test:group/ocelot
execute positioned ~ ~ ~28 run function bme_test:group/fox
execute positioned ~ ~ ~32 run function bme_test:group/frog
execute positioned ~ ~ ~36 run function bme_test:group/axolotl
execute positioned ~ ~ ~40 run function bme_test:group/bee
execute positioned ~ ~ ~44 run function bme_test:group/llama
execute positioned ~ ~ ~49 run function bme_test:group/trader_llama
execute positioned ~ ~ ~54 run function bme_test:group/horse_markings_0
execute positioned ~ ~ ~59 run function bme_test:group/horse_markings_1
execute positioned ~ ~ ~64 run function bme_test:group/horse_markings_2
execute positioned ~ ~ ~69 run function bme_test:group/horse_markings_3
execute positioned ~ ~ ~74 run function bme_test:group/horse_markings_4
execute positioned ~ ~ ~79 run function bme_test:group/horse_baby
execute positioned ~ ~ ~84 run function bme_test:group/chested_horse
execute positioned ~ ~ ~89 run function bme_test:group/undead_horse
execute positioned ~ ~ ~94 run function bme_test:group/zombie
execute positioned ~ ~ ~98 run function bme_test:group/skeleton
execute positioned ~ ~ ~102 run function bme_test:group/armadillo
execute positioned ~ ~ ~106 run function bme_test:group/camel
execute positioned ~ ~ ~111 run function bme_test:group/copper_golem
execute positioned ~ ~ ~115 run function bme_test:group/golem
execute positioned ~ ~ ~120 run function bme_test:group/aquatic
execute positioned ~ ~ ~124 run function bme_test:group/tropical_fish
execute positioned ~ ~ ~128 run function bme_test:group/ghast
execute positioned ~ ~ ~142 run function bme_test:group/misc

tellraw @s {"text":"[bme] spawned 214 test-entities in 31 groups","color":"green"}
