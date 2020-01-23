package controller.analysis.optimization.genetic

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utility.Utilities
import java.util.*
import kotlin.math.abs


class EvolutionManager(
    private val maxGenerations: Int = MaxGenerations,
    private val populationSize: Int = PopulationSize,
    private val chanceToMutateInPercent: Int = ChanceToMutateInPercent,
    private val geneLength: Int,
    private val geneAmount: Int,
    private val illegalValues: List<Int>? = null,
    private val fitnessFunction: (Specimen) -> Double
) {
    private val logger: Logger = LoggerFactory.getLogger(EvolutionManager::class.java)

    private val chromosomeLength: Int = geneLength * geneAmount
    private val randomizer: Random = Random()
    private var currentGeneration = 0
    private var generationsWithoutImprovement = 0
    private var mutationChance: Int = chanceToMutateInPercent
    private lateinit var population: ArrayList<Specimen>
    private lateinit var bestEverSpecimen: Specimen

    fun start(): Specimen {
        initializePopulation()

        while (currentGeneration <= maxGenerations) {
            if (generationsWithoutImprovement > (maxGenerations / 2)) break

            currentGeneration++

            select()
            crossover()
            mutate()

            evaluate()
        }

        return bestEverSpecimen
    }

    private fun evaluate() {
        sanitize()
        population.forEach { it.fitness = fitnessFunction(it) }
        updateBestSpecimen()

        logger.info("\nGeneration #$currentGeneration")
        logger.info("Best in current generation: ${population.maxBy { it.fitness!! }!!.fitness}")
        logger.info("Average fitness: ${population.map { it.fitness!! }.average()}")
        logger.info("Generations without improvement: $generationsWithoutImprovement")
        logger.info("Current mutation chance: $mutationChance%")
    }

    private fun select() {
        population = ArrayList(population.sortedByDescending { it.fitness!! }.toSet().take(((populationSize / 2) + 1)))
    }

    private fun crossover() {
        while (population.size < populationSize) {
            val parentPair: Pair<Specimen, Specimen> = retrieveParentPair()
            val offspring: Specimen = crossoverParents(parentPair)
            population.add(offspring)
        }
    }

    private fun mutate() {
        mutationChance = (chanceToMutateInPercent * generationsWithoutImprovement) / 2
        if (mutationChance > 50) mutationChance = 50

        for (specimen: Specimen in population) {
            for ((i: Int, gene: Int) in specimen.chromosome.withIndex()) {
                val geneShouldMutate: Boolean = randomizer.nextInt(100) <= chanceToMutateInPercent
                if (geneShouldMutate) specimen.chromosome[i] = flipBit(gene)
            }
        }
    }

    private fun sanitize() {
        if (illegalValues != null) population.removeIf { specimen -> convertBinaryChromosomeToIntegerList(specimen.chromosome, geneAmount).any { illegalValues.contains(it) } }
    }

    private fun retrieveParentPair(): Pair<Specimen, Specimen> {
        val parentA: Specimen = selectRandomSpecimenViaTournament()
        val parentB: Specimen = selectRandomSpecimenViaTournament()
        return Pair(parentA, parentB)
    }

    private fun selectRandomSpecimenViaTournament(): Specimen {
        val tournamentParticipants: List<Specimen> = population.shuffled().filter { it.fitness != null }.take((populationSize / SelectionPercentage) + 1)
        return tournamentParticipants.maxBy { it.fitness!! }!!
    }

    private fun crossoverParents(parentPair: Pair<Specimen, Specimen>): Specimen {
        val parentA: Specimen = parentPair.first
        val parentB: Specimen = parentPair.second

        val childChromosome: ArrayList<List<Int>> = arrayListOf()

        var currentGeneStartingPoint = 0
        for (_g: Int in 1..geneAmount) {
            val fiftyFiftyChance: Int = randomizer.nextInt(100)

            if (fiftyFiftyChance < 50) {
                val gene: List<Int> = parentA.chromosome.slice(currentGeneStartingPoint..(currentGeneStartingPoint + (geneLength - 1)))
                childChromosome.add(gene)
            } else {
                val gene: List<Int> = parentB.chromosome.slice(currentGeneStartingPoint..(currentGeneStartingPoint + (geneLength - 1)))
                childChromosome.add(gene)
            }

            currentGeneStartingPoint += (geneLength - 1)
        }

        return Specimen(childChromosome.flatten().toIntArray())
    }

    private fun updateBestSpecimen() {
        val currentBestSpecimen: Specimen = population.maxBy { it.fitness!! }!!

        if (currentGeneration == 0) bestEverSpecimen = currentBestSpecimen.clone()

        if (currentBestSpecimen.fitness!! > bestEverSpecimen.fitness!!) {
            bestEverSpecimen = currentBestSpecimen.clone()
            generationsWithoutImprovement = 0
        } else {
            generationsWithoutImprovement++
        }
    }

    private fun initializePopulation() {
        population = ArrayList((1..populationSize).map { generateRandomSpecimen() })
        evaluate()
    }

    private fun generateRandomSpecimen(): Specimen {
        val chromosome: IntArray = (1..chromosomeLength).map { randomBit() }.toIntArray()
        return Specimen(chromosome = chromosome)
    }

    private fun randomBit(): Int = abs(randomizer.nextInt()) % 2

    private fun flipBit(bit: Int) = bit.xor(1)

    companion object {
        private const val MaxGenerations = 100
        private const val PopulationSize = 100
        private const val SelectionPercentage = 10
        private const val ChanceToMutateInPercent = 1

        fun convertBinaryChromosomeToIntegerList(chromosome: IntArray, parameterAmount: Int): List<Int> {
            val parameters: ArrayList<Int> = arrayListOf()
            val parameterBitLength: Int = chromosome.size / parameterAmount

            var index = 0
            for (_i: Int in 1..parameterAmount) {
                var bits = ""

                for (_j: Int in 1..parameterBitLength) {
                    bits += chromosome[index].toString()
                    index += 1
                }

                parameters.add(Utilities.convertBinaryToDecimal(bits))
            }

            return parameters.toList()
        }
    }
}
