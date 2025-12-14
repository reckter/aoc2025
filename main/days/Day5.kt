package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.parseWithRegex
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.splitAtEmptyLine
import me.reckter.aoc.toLongs

class Day5 : Day {
	override val day = 5

	override fun solvePart1() {
		val input = loadInput(trim = false)
		val ranges = input
			.splitAtEmptyLine()
			.first()
			.toList()
			.parseWithRegex("(\\d+)-(\\d+)")
			.map { (min, max) -> min.toLong()..max.toLong() }

		input.splitAtEmptyLine()
			.drop(1)
			.first()
			.toList()
			.toLongs()
			.count { ranges.any { range -> range.contains(it) } }
			.solution(1)


	}

	fun hasAnyCollisions(ranges: List<LongRange>): Boolean {
		return ranges.sortedBy { it.start }
			.windowed(2, 1)
			.any { (range1, range2) ->
				!(range1.endInclusive < range2.start || range1.start > range2.endInclusive)
			}
	}

	fun mergeIfPossible(range1: LongRange, range2: LongRange): List<LongRange> {
		if (range1.endInclusive < range2.start || range1.start > range2.endInclusive) {
			// no overlapp, nothing to do
			return listOf(range1, range2)
		}

		if (range1.start <= range2.start && range1.endInclusive >= range2.endInclusive) {
			// range2 is in range1
			return listOf(range1)
		}

		if (range1.start >= range2.start && range1.endInclusive <= range2.endInclusive) {
			// range1 is in range2
			return listOf(range2)
		}

		if (range2.start >= range1.start && range2.start <= range1.endInclusive) {
			// -r1--]
			//  [---r2--

			return listOf(range1, (range1.endInclusive + 1)..range2.endInclusive)
		}

		if (range1.start >= range2.start && range1.start <= range2.endInclusive) {
			//   [-r1--
			// r2--]
			return listOf(range1, range2.start..(range1.start - 1))
		}

		error("not covered case! $range1 $range2")
	}

	fun combineOnce(arg: List<LongRange>): List<LongRange> {
		return arg.sortedBy { it.start }
			.chunked(2)
			.flatMap {
				if (it.size != 2) it
				else mergeIfPossible(it.first(), it.last())
			}
	}

	fun combineRound(arg: List<LongRange>): List<LongRange> {
		val first = combineOnce(arg)
		val second = combineOnce(first.drop(1))
		return listOf(first.first()) + second
	}

	fun combine(arg: List<LongRange>): List<LongRange> {
		return generateSequence(arg) {
			if (!hasAnyCollisions(it)) null
			else
				combineRound(it)
		}
			.last()
	}

	override fun solvePart2() {
		val input = loadInput(trim = false)
		val ranges = input
			.splitAtEmptyLine()
			.first()
			.toList()
			.parseWithRegex("(\\d+)-(\\d+)")
			.map { (min, max) -> min.toLong()..max.toLong() }

		combine(ranges)
			.sumOf { it.endInclusive - it.start + 1 }
			.solution(2)
	}
}

fun main() = solve<Day5>()
