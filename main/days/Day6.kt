package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.flipDimensions
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve
import me.reckter.aoc.splitAt
import me.reckter.aoc.toLongs

class Day6 : Day {
    override val day = 6

    override fun solvePart1() {
	    loadInput()
		    .map { it.split(" ").filter(String::isNotEmpty) }
		    .flipDimensions()
			.sumOf {
			    if (it.last() == "*") {
				    it.dropLast(1).toLongs().reduce { a, b -> a * b }
			    } else {
				    it.dropLast(1).toLongs().reduce { a, b -> a + b }
			    }
		    }
		    .solution(1)
    }

    override fun solvePart2() {
		val lines = loadInput()

	    lines.first().indices.map { index ->
		    val slice = lines.map { it[index] }
		    if (slice.all { it == ' ' }) {
			    null to null
		    } else {
			    val number = slice.dropLast(1).joinToString("").trim().toLong()
			    val operation = slice.last()

			    operation to number
		    }
	    }
		    .splitAt { it.first == null && it.second == null }
			.sumOf {
			    if (it.first().first == '*') {
				    it.mapNotNull { it.second }.reduce { a, b -> a * b }
			    } else {
				    it.mapNotNull { it.second }.reduce { a, b -> a + b }
			    }

		    }
		    .solution(2)

    }
}

fun main() = solve<Day6>()
