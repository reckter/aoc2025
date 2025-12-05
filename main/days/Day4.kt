package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.cords.d2.Coord2D
import me.reckter.aoc.cords.d2.getNeighbors
import me.reckter.aoc.parseMap
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import kotlin.collections.component1
import kotlin.collections.component2

class Day4 : Day {
	override val day = 4

	enum class Tile {
		paper,
		empty
	}

	override fun solvePart1() {
		val map = loadInput()
			.parseMap {
				when (it) {
					'@' -> Tile.paper
					'.' -> Tile.empty
					else -> error("invalid tile: $it")
				}
			}

		map.entries
			.filter { it.value == Tile.paper }
			.count { (cords, _) ->
				cords.getNeighbors()
					.count { map[it] == Tile.paper } < 4
			}
			.solution(1)
	}

	fun Map<Coord2D<Int>, Tile>.removeAccessible(): Pair<Map<Coord2D<Int>, Tile>, Int> {
		val cords = this.entries
			.filter { it.value == Tile.paper }
			.filter { (cords, _) ->
				cords.getNeighbors()
					.count { this[it] == Tile.paper } < 4
			}
			.map { (cords, _) -> cords}

		return (this - cords.toSet()) to cords.size
	}

	override fun solvePart2() {
		val map = loadInput()
			.parseMap {
				when (it) {
					'@' -> Tile.paper
					'.' -> Tile.empty
					else -> error("invalid tile: $it")
				}
			}

		generateSequence(map to  0)  { cur ->
			val next = cur.first.removeAccessible()
			if(next.second == 0 && cur.second != 0)
				null
			else next
		}
			.map { it.second }
			.sum()
			.solution(2)
	}
}

fun main() = solve<Day4>()
