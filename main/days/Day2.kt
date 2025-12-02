package me.reckter.aoc.days

import me.reckter.aoc.Day
import me.reckter.aoc.parseWithRegex
import me.reckter.aoc.print
import me.reckter.aoc.solution
import me.reckter.aoc.solve

class Day2 : Day {
    override val day = 2


	fun checkValid(it: Long): Boolean {
		val str = it.toString()
		if(str.length % 2 != 0) return true

		val firstHalf = str.take(str.length / 2)
		val secondHalf = str.takeLast(str.length / 2)
		return firstHalf != secondHalf
	}

    override fun solvePart1() {
        loadInput()
	        .first()
	        .split(",")
	        .parseWithRegex("(\\d+)-(\\d+)")
	        .map { (from,to) -> from.toLong()..to.toLong()}
	        .flatten()
	        .filter { !checkValid(it) }
	        .print("")
	        .sum()
	        .solution(1)
    }

	fun checkValid2(it: Long): Boolean {
		val str = it.toString()

		return (1..(str.length / 2))
			.all { length ->
				if(str.length % length != 0) true
				else {
					val needle = str.take(length)

					str != needle.repeat(str.length / length)
				}
			}
	}

	override fun solvePart2() {
		loadInput()
			.first()
			.split(",")
			.parseWithRegex("(\\d+)-(\\d+)")
			.map { (from,to) -> from.toLong()..to.toLong()}
			.flatten()
			.filter { !checkValid2(it) }
			.print("")
			.sum()
			.solution(1)
    }
}

fun main() = solve<Day2>()
