package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.cords.d2.Coord2D
import me.reckter.aoc.cords.d2.minus
import me.reckter.aoc.cords.d2.plus
import me.reckter.aoc.parseMap
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day7 : Day {
	override val day = 7

	constructor()

	enum class Tile {
		Start,
		Empty,
		Splitter
	}

	fun findNextSplitter(map: Map<Coord2D<Int>, Tile>, coord: Coord2D<Int>): Coord2D<Int>? {
		var cur = coord
		while (map[cur] == Tile.Empty || map[cur] == Tile.Start) {
			cur += Coord2D(0, 1)
		}
		if (map[cur] == Tile.Splitter) {
			return cur
		}
		return null
	}

	val seen = mutableMapOf<Coord2D<Int>, Long>()

	override fun solvePart1() {
		val map = loadInput()
			.parseMap {
				when (it) {
					'S' -> Tile.Start
					'.' -> Tile.Empty
					'^' -> Tile.Splitter
					else -> error("Invalid input $it")
				}
			}

		val queue = ArrayDeque<Coord2D<Int>>()

		queue.add(map.entries.find { it.value == Tile.Start }?.key ?: error("no start!"))

		val usedSplitter = mutableSetOf<Coord2D<Int>>()
		while (queue.isNotEmpty()) {
			val current = queue.removeFirst()
			val splitter = findNextSplitter(map, current)

			if (splitter != null) {
				usedSplitter.add(splitter)
				val right = splitter + Coord2D(1, 0)
				if (right !in seen) {
					queue.add(right)
				}
				seen[right] = seen.getOrDefault(right, 0) + (seen[current] ?: 1L)

				val left = splitter + Coord2D(-1, 0)
				if (left !in seen) {
					queue.add(left)
				}

				seen[left] = seen.getOrDefault(left, 0) + (seen[current] ?: 1L)
			} else {
				val saveCords = Coord2D(current.x, -1)
				seen[saveCords] = seen.getOrDefault(saveCords, 0) + seen[current]!!
			}
			queue.sortBy { it.y }
		}
		usedSplitter.size.solution(1)
	}

	override fun solvePart2() {
		seen
			.entries
			.filter { it.key.y == -1 }
			.sumOf { it.value }
			.solution(2)
	}
}

fun main() = solve<Day7>()
