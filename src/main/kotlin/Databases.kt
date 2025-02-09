package com.example

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )
    routing {

    }
}

fun Application.connectToMongoDB(): MongoDatabase {
//    val user = environment.config.tryGetString("db.mongo.user")
//    val password = System.getenv("db_password")
//    val host = environment.config.tryGetString("db.mongo.host") ?: "127.0.0.1"
//    val port = environment.config.tryGetString("db.mongo.port") ?: "27017"
//    val maxPoolSize = environment.config.tryGetString("db.mongo.maxPoolSize")?.toInt() ?: 20
//    val databaseName = environment.config.tryGetString("") ?: "myDatabase"

//    val credentials = user?.let { userVal -> password?.let { passwordVal -> "$userVal:$passwordVal@" } }.orEmpty()
//    val uri = "mongodb://$credentials$host:$port/?maxPoolSize=$maxPoolSize&w=majority"

    val dbName = "dove_drop"
    val dbUserName = "UMESHONE"
    val dbPassword = System.getenv("DBPASSWORD")

    val mongoClient = MongoClient.create("mongodb+srv://$dbUserName:$dbPassword@cluster0.1fost.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0")
    val database = mongoClient.getDatabase(dbName)

    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return database
}
