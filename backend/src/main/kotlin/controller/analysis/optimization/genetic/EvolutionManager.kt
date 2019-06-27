package controller.analysis.optimization.genetic

import utility.Utilities
import java.util.*
import kotlin.math.abs


class EvolutionManager(
        private val maxGenerations: Int = MaxGenerations,
        private val populationSize: Int = PopulationSize,
        private val chanceToMutateInPercent: Int = ChanceToMutateInPercent,
        private val geneLength: Int,
        private val geneAmount: Int,
        private val illegalValues: List<Number>? = null,
        private val fitnessFunction: (Specimen) -> Double
) {
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
            sanitize()

            evaluate()
        }

        return bestEverSpecimen
    }

    private fun evaluate() {
        population.forEach { it.fitness = fitnessFunction(it) }
        updateBestSpecimen()

        println("Generation #$currentGeneration")
        println("Best in current generation: ${population.maxBy { it.fitness!! }!!.fitness}")
        println("Average fitness: ${population.map { it.fitness!! }.average()}")
        println("Generations without improvement: $generationsWithoutImprovement")
        println("Current mutation chance: $mutationChance%")
        println()
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

//        val parentPairs: ArrayList<Pair<Specimen, Specimen>> = arrayListOf()
//
//        var index = 0
//        val sortedPopulation: List<Specimen> = population.sortedByDescending { it.fitness!! }
//        for (_specimen in sortedPopulation) {
//            if (index + 1 > sortedPopulation.size - 1) break
//            parentPairs.add(Pair(sortedPopulation[index], sortedPopulation[index + 1]))
//            index += 2
//        }
//
//        return parentPairs.toList()
    }

    private fun selectRandomSpecimenViaTournament(): Specimen {
        val tournamentParticipants: List<Specimen> = population.shuffled().filter { it.fitness != null }.take((populationSize / SelectionPercentage) + 1)
        return tournamentParticipants.maxBy { it.fitness!! }!!
    }

//    private fun selectRandomSpecimenProportionally(proportionalSpecimenList: List<Pair<Double, Specimen>>): Specimen {
//        val randomNumber: Int = randomizer.nextInt(100)
//
//        var totalPercentage = 0.0
//        var specimenIndex = 0
//        for (specimen: Pair<Double, Specimen> in proportionalSpecimenList) {
//            if (totalPercentage >= randomNumber) break
//            totalPercentage += specimen.first
//            specimenIndex++
//        }
//
//        if (specimenIndex >= proportionalSpecimenList.size) specimenIndex = proportionalSpecimenList.size - 1
//
//        return proportionalSpecimenList[specimenIndex].second
//    }

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

//    private fun buildProportionalSpecimenList(): List<Pair<Double, Specimen>> {
//        val accumulatedFitness: Double = population.sumByDouble { it.fitness!! }
//        return population.map { Pair(((it.fitness!! / accumulatedFitness) * 100), it) }.shuffled(randomizer)
//    }

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
        val initialPopulation: ArrayList<Specimen> = arrayListOf()

        for (n: Int in (1..populationSize)) {
            initialPopulation.add(generateRandomSpecimen())
        }

        population = initialPopulation
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
