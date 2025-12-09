package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.allPairings
import me.reckter.aoc.cords.d2.Coord2D
import me.reckter.aoc.cords.d2.getNeighbors
import me.reckter.aoc.cords.d2.minus
import me.reckter.aoc.cords.d2.plus
import me.reckter.aoc.parseWithRegex
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import kotlin.math.abs
import kotlin.math.sign
import kotlin.sequences.generateSequence
import kotlin.sequences.sortedByDescending

class Day9 : Day {
	override val day = 9

	enum class Tile {
		Empty,
		Red,
		Green
	}

	override fun solvePart1() {
		loadInput()
			.parseWithRegex("(\\d+),(\\d+)")
			.map { (x, y) -> Coord2D(x.toLong(), y.toLong()) }
			.allPairings(includeSelf = false, bothDirections = false)
			.maxOf { (abs(it.second.x - it.first.x) + 1) * (abs(it.second.y - it.first.y) + 1) }
			.solution(1)
	}

	fun Coord2D<Long>.isIn(a: Coord2D<Long>, b: Coord2D<Long>): Boolean {
		val inX = (a.x > this.x && this.x > b.x) || (a.x < this.x && this.x < b.x)
		val inY = (a.y > this.y && this.y > b.y) || (a.y < this.y && this.y < b.y)

		return inX && inY
	}

	fun Coord2D<Int>.line(to: Coord2D<Int>): Sequence<Coord2D<Int>> {
		val diff = to - this
		val normalized = Coord2D(diff.x.sign, diff.y.sign)

		return generateSequence(this) { it + normalized }
			.drop(1)
			.takeWhile { it != to }
	}

	fun floodFill(map: MutableMap<Coord2D<Int>, Tile>, start: Coord2D<Int>) {
		val queue = ArrayDeque<Coord2D<Int>>()
		queue.add(start)
		map[start] = Tile.Green
		while (queue.isNotEmpty()) {
			val next = queue.removeFirst()
			next.getNeighbors()
				.filter { !map.containsKey(it) }
				.forEach {
					map[it] = Tile.Green
					queue.add(it)
				}
		}
	}

	fun isInBounds(ranges: Map<Int, List<IntRange>>, coord: Coord2D<Int>): Boolean {
		val ranges = ranges[coord.x] ?: return false

		return ranges.any { coord.y in it }
	}

	fun rectangleBounds(a: Coord2D<Int>, d: Coord2D<Int>): Sequence<Coord2D<Int>> {
		val b = Coord2D(a.x, d.y)
		val c = Coord2D(d.x, a.y)
		// front load corners, for speedy check (we do then check them multiple times, because of this, but that shoudn't matter)
		return listOf(a, b, c, d).asSequence() + a.line(b) + b.line(d) + d.line(c) + c.line(a)
	}

	fun buildInBoundsRanges(map: MutableMap<Coord2D<Int>, Tile>): Map<Int, List<IntRange>> {
		val minX = map.keys.minOf { it.x }
		val maxX = map.keys.maxOf { it.x }

		return (minX..maxX)
			.toList()
			.stream()
			.parallel()
			.map { x ->
				val reducedMap = map.filter { it.key.x == x }
				val runs = reducedMap.keys
					.groupBy { start ->
						generateSequence(start.y) { it - 1 }
							.dropWhile { reducedMap.contains(Coord2D(start.x, it)) }
							.first()
					}
					.entries
					.sortedBy { it.key }
					.map { it.value }

				val minMaximums = runs
					.filter {
						if (it.size == 1) false
						else {
							val endpoints = it.filter { map[it] == Tile.Red }
							val firstIsLeft = map.contains(endpoints.first() + Coord2D(-1, 0))
							val secondIsLeft = map.contains(endpoints.last() + Coord2D(-1, 0))

							firstIsLeft == secondIsLeft
						}
					}
				val changes = runs - minMaximums

				val otherRanges =
					if (changes.size == 1) {
						val run = changes.first()
						listOf(run.minOf { it.y }..run.maxOf { it.y })
					} else
						changes
							.zipWithNext()
							.filterIndexed { index, _ -> index % 2 == 0 }
							.map { (start, end) ->
								start.minOf { it.y }..end.maxOf { it.y }
							}
				x to otherRanges + minMaximums.map {
					it.minOf { it.y }..it.maxOf { it.y }
				}
			}
			.toList()
			.toMap()
	}

	fun rectangleSize(a: Coord2D<Int>, b: Coord2D<Int>): Long {
		return (abs(b.x.toLong() - a.x.toLong()) + 1) * (abs(b.y.toLong() - a.y) + 1)
	}

	override fun solvePart2() {
		val points = loadInput()
			.parseWithRegex("(\\d+),(\\d+)")
			.map { (x, y) -> Coord2D(x.toInt(), y.toInt()) }

		val map = mutableMapOf<Coord2D<Int>, Tile>()
		points.forEach { (x, y) -> map[Coord2D(x, y)] = Tile.Red }

		(points + points.first())
			.zipWithNext()
			.forEach { (from, to) ->
				from.line(to)
					.filter { !map.containsKey(it) }
					.forEach { map[it] = Tile.Green }
			}
		val ranges = buildInBoundsRanges(map)

		points
			.allPairings(includeSelf = false, bothDirections = false)
			.sortedByDescending { rectangleSize(it.first, it.second) }
			.filter { pair ->
//				print("pair: $pair")
//				print(" size: ${rectangleSize(pair.first, pair.second)}")
				val ret = rectangleBounds(pair.first, pair.second)
					.all { isInBounds(ranges, it) }

//				println(" inside: $ret")

				ret
			}
//			.maxOf { (abs(it.second.x - it.first.x) + 1) * (abs(it.second.y - it.first.y) + 1) }
			.firstOrNull()
			?.let { rectangleSize(it.first, it.second) }
			?.solution(2)
			?: error("No solution found :o ")

	}
}

fun main() = solve<Day9>()
