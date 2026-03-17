package com.recipebook.recipe_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class RecipeBackendApplication

fun main(args: Array<String>) {
	runApplication<RecipeBackendApplication>(*args)
}
