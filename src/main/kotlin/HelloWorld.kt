import java.io.File
import java.util.*

fun main(args: Array<String>) {
  println(readInputData(File("/Users/mih/prog/projects/harmonyrabbit/input.in")))
}

fun readInputData(input: File): Input {
  val lines = input.readLines()

  val firstLine = lines[0].split(" ")
//  val videosCount = firstLine[0].toInt()
  val endpointCount = firstLine[1].toLong()
//  val requestDescCount = firstLine[2].toLong()
  val cachesCount = firstLine[3].toLong()
  val cachesSize = firstLine[4].toLong()

  val videos: List<Long> = lines[1].split(" ").map(String::toLong)

  val endpoints: MutableList<Endpoint> = ArrayList()

  var currentLine = 2;
  for (i in 0..endpointCount) {
    val endpointInfo = lines[currentLine].split(" ")
    currentLine++

    val endpoint = Endpoint(i, endpointInfo[0].toLong())

    val endpointCachesCount = endpointInfo[1].toLong()
    for (j in 0..endpointCachesCount) {
      val cacheConnection = lines[currentLine].split(" ")
      endpoint.caches[cacheConnection[0].toLong()] = cacheConnection[1].toLong()
      currentLine++
    }

    endpoints.add(endpoint)
  }

  while (currentLine < lines.size) {
    val curLine = lines[currentLine].split(" ")

    val video = curLine[0].toLong()
    val endpoint = curLine[1].toLong()
    val count = curLine[2].toLong()

    endpoints[endpoint.toInt()].requests[video] = count
  }

  return Input(endpoints, videos, cachesCount, cachesSize)
}

data class Endpoint(val id: Long, val dataCenterLatency: Long, val caches: MutableMap<Long, Long> = HashMap(),
                    val requests: MutableMap<Long, Long> = HashMap())
data class Input(val endpoints: List<Endpoint>, val videos: List<Long>, val cacheCount: Long, val cacheSize: Long)