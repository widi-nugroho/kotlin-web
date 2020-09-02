package db

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat
data class Person(val nama:String, val umur:Int, val pekerjaan:String)
object DB{
    init {
        val url = "jdbc:sqlite:/home/widi/sqlite/db/people3.db"
        val sql= """CREATE TABLE IF NOT EXISTS peoples (
            nama text PRIMARY KEY,
            usia integer,
            pekerjaan text NOT NULL
        );"""
        try{
            var conn=DriverManager.getConnection(url)
            var stmt=conn.createStatement()
            stmt.execute(sql)
        }catch (e:SQLException){
            println(e.message)
        }
    }
    private fun connect(): Connection? {
        val url = "jdbc:sqlite:/home/widi/sqlite/db/people3.db"
        var conn:Connection?=null
        try {
            conn=DriverManager.getConnection(url)
        }catch (e:SQLException){
            println(e.message)
        }
        return conn
    }
    fun insert(p:Person){
        var sql="INSERT INTO peoples(nama,usia,pekerjaan) VALUES(?,?,?)"
        try {
            var conn=this.connect()
            var pstmt= conn!!.prepareStatement(sql)
            pstmt.setString(1, p.nama.toLowerCase());
            pstmt.setInt(2,p.umur)
            pstmt.setString(3,p.pekerjaan)
            pstmt.executeUpdate();
            //conn.commit()
        }catch (e:SQLException){
            println(e.message)
        }
    }
    fun delete(nama:String){
        var sql="DELETE FROM peoples WHERE nama = ?"
        try {
            var conn=this.connect()
            var pstmt  = conn!!.prepareStatement(sql)
            pstmt.setString(1,nama)
            pstmt.executeUpdate();
        }catch (e:SQLException){
            println(e.message)
        }
    }
    fun update(nama:String,usia:Int,pekerjaan:String){
        var sql= ("UPDATE peoples SET usia = ? , "
                + "pekerjaan = ? "
                + "WHERE nama = ?")
        try {
            var conn=this.connect()
            var pstmt=conn!!.prepareStatement(sql)
            pstmt.setInt(1,usia)
            pstmt.setString(2,pekerjaan)
            pstmt.setString(3,nama)
            pstmt.executeUpdate()
        }catch (e:SQLException){
            println(e.message)
        }
    }
    fun allPerson(): MutableList<String> {
        var sql = "SELECT nama, usia, pekerjaan FROM peoples"
        var res1= mutableListOf<String>()
        try {
            var conn=this.connect()
            var stmt  = conn!!.createStatement()
            var rs    = stmt.executeQuery(sql)
            while (rs.next()){
                res1.add(rs.getString("nama") + "\t" +
                        rs.getInt("usia").toString() + "\t" +
                        rs.getString("pekerjaan"))
            }
        }catch (e:SQLException){
            println(e.message)
        }
        return res1
    }
}
fun main(){
    val server= embeddedServer(Netty,9000){
        install(ContentNegotiation) {
            gson {
                setDateFormat(DateFormat.LONG)
                setPrettyPrinting()
            }
        }
        routing{
            post("/addPerson") {
                val p=call.receive<Person>()
                DB.insert(p)
                call.respondText(p.nama+" "+p.umur+" "+p.pekerjaan)
            }
            get("/deletePerson"){
                val nama=call.request.queryParameters["nama"]!!
                DB.delete(nama)
                call.respondText(nama+" deleted")
            }
            get("/updatePerson"){
                val nama=call.request.queryParameters["nama"]!!
                val usia=call.request.queryParameters["usia"]!!.toInt()
                val pekerjaan=call.request.queryParameters["pekerjaan"]!!
                println(nama+usia.toString()+pekerjaan+"-----------")
                DB.update(nama,usia,pekerjaan)
                call.respondText(nama+" updated")
            }
            get("/allPerson"){
                DB.allPerson()
                call.respondText(DB.allPerson().toString())
            }
        }
    }
    server.start(wait=true)
}