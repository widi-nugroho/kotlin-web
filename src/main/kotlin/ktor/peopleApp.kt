package ktor

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.text.DateFormat

data class Person(val nama:String, val umur:Int, val pekerjaan:String)
fun main(){
    var mapPeople= mutableMapOf<String,Person>()
    val server= embeddedServer(Netty,port=8080){
        install(ContentNegotiation) {
            gson {
                setDateFormat(DateFormat.LONG)
                setPrettyPrinting()
            }
        }
        routing {
            get("/"){
                call.respondText("Hello")
            }
            post("/addPerson"){
                val p=call.receive<Person>()
                println(p)
                call.respondText(p.nama+" "+p.umur+" "+p.pekerjaan)
                mapPeople[p.nama.toLowerCase()]=p
            }
            get("/getPerson"){
                val nama = call.request.queryParameters["nama"]
                call.respond(mapPeople[nama!!.toLowerCase()]!!)
            }
            get("/allPerson"){
                call.respond(mapPeople)
            }
            get("/updatePerson"){
                val nama=call.request.queryParameters["nama"]!!
                val umur=call.request.queryParameters["umur"]!!.toInt()
                val pekerjaan=call.request.queryParameters["pek"]!!
                val p=Person(nama, umur, pekerjaan)
                println(nama+umur.toString()+pekerjaan+"-----------")
                mapPeople[nama.toLowerCase()]=p
            }
            get("/deletePerson"){
                val nama=call.request.queryParameters["nama"]!!
                mapPeople.remove(nama)
            }
        }
    }
    server.start(wait=true)
}