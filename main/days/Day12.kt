package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.allPairings
import me.reckter.aoc.cords.d2.Coord2D
import me.reckter.aoc.parseMap
import me.reckter.aoc.parseWithRegex
import me.reckter.aoc.print
import me.reckter.aoc.repeatToSequence
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.splitAtEmptyLine
import me.reckter.aoc.toIntegers

class Day12 : Day {
	override val day = 12

	enum class Tile {
		Empty,
		Full,
	}

	val presentSize = 3
	enum class Flip(op: (it: Set<Coord2D<Int>>) -> Set<Coord2D<Int>>) {
		None({it}),
		Flip({it.map { Coord2D(-it.x + 2, it.y) }.toSet()}),
	}

	enum class Rotation(op: (it: Set<Coord2D<Int>>) -> Set<Coord2D<Int>>) {
		None({it}),
		Right({it.map { Coord2D(-it.y + 2, it.x) }.toSet()}),
		OneEighty({it.map { Coord2D(-it.x + 2, -it.y + 2) }.toSet()}),
		Left({it.map { Coord2D(it.y , -it.x + 2) }.toSet()}),
	}

	val allPermutations = Flip.entries
		.allPairings(Rotation.entries)
		.toList()

	fun checkTreeSize(size: Pair<Int, Int>, neededPresents: List<Int>, presents: Map<Int, Set<Coord2D<Int>>>): Boolean {
		val area = size.first * size.second
		val presentArea = neededPresents
			.mapIndexed { index, amount -> presents[index]!!.size * amount }
			.sum()
		return area >= presentArea
	}

	override fun solvePart1() {
		val input = loadInput(trim = false)
		val presents = input
			.splitAtEmptyLine()
			.toList()
			.dropLast(1)
			.associate {
				val index = it.first().split(":").first().toInt()
				val map = it.drop(1)
					.parseMap {
						when (it) {
							'#' -> Tile.Full
							'.' -> Tile.Empty
							else -> error("Invalid tile $it")
						}
					}
					.filter { it.value ==Tile.Full }
					.keys

				index to map
			}

		val trees = input
			.splitAtEmptyLine()
			.last()
			.toList()
			.parseWithRegex("(\\d+)x(\\d+): (.*)")
			.map { (widthStr, heightStr, indexes) ->
				val size = widthStr.toInt() to heightStr.toInt()
				val neededPresents = indexes.split(" ")
					.toIntegers()

				size to neededPresents
			}


		trees
			.count { checkTreeSize(it.first, it.second, presents)}
			.solution(1)

	}

	override fun solvePart2() {

	}
}

fun main() = solve<Day12>()
