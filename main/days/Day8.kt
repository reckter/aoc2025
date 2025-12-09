package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.allPairings
import me.reckter.aoc.cords.d2.Coord2D
import me.reckter.aoc.cords.d3.Coord3D
import me.reckter.aoc.cords.d3.euclidDistanceForSorting
import me.reckter.aoc.parseWithRegex
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day8 : Day {
    override val day = 8

	val boxes by lazy {
		loadInput()
			.parseWithRegex("(\\d+),(\\d+),(\\d+)")
			.map { (x, y, z) -> Coord3D(x.toInt(), y.toInt(), z.toInt()) }
	}
	val pairs by lazy {
		boxes.allPairings(includeSelf = false, bothDirections = false)
			.sortedBy { it.first.euclidDistanceForSorting(it.second) }
	}

    override fun solvePart1() {
	    val networks = mutableListOf<MutableSet<Coord3D<Int>>>()
	    pairs.
		    take(1000)
		    .forEach { pair ->
			val networkForA = networks.find { pair.first in it }
		    val networkForB = networks.find { pair.second in it }
		    when {
				networkForB == networkForA && networkForA != null-> {
					// no op
				}
			    networkForA != null && networkForB != null -> {
				    // merge!
				    networks.remove(networkForB)
				    networkForA.addAll(networkForB)
			    }
			    networkForA != null -> {
				    networkForA.add(pair.second)
			    }
			    networkForB != null -> {
					networkForB.add(pair.first)
			    }
			    else -> {
				    networks.add(mutableSetOf(pair.first, pair.second))
			    }
		    }
	    }

	    networks
		    .map { it.size }
		    .sortedDescending()
		    .take(3)
		    .reduce { a, b -> a * b }
		    .solution(1)
    }

	fun getLastConnectionNecessary(): Pair<Coord3D<Int>, Coord3D<Int>> {
		val networks = mutableListOf<MutableSet<Coord3D<Int>>>()

		for (pair in pairs) {
			val networkForA = networks.find { pair.first in it }
			val networkForB = networks.find { pair.second in it }
			when {
				networkForB == networkForA && networkForA != null-> {
					// no op
				}
				networkForA != null && networkForB != null -> {
					// merge!
					networks.remove(networkForB)
					networkForA.addAll(networkForB)
				}
				networkForA != null -> {
					networkForA.add(pair.second)
				}
				networkForB != null -> {
					networkForB.add(pair.first)
				}
				else -> {
					networks.add(mutableSetOf(pair.first, pair.second))
				}
			}
			if(networks.size == 1 && networks.first().size == boxes.size) {
				return pair
			}
		}
		error("No network found")
	}
    override fun solvePart2() {

		val lastConnectionNecessary = getLastConnectionNecessary()

	    (lastConnectionNecessary.first.x.toLong() * lastConnectionNecessary.second.x.toLong())
		    .solution(2)
    }
}

fun main() = solve<Day8>()
