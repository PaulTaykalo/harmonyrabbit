import java.io.File
import java.util.*

fun main(args: Array<String>) {
  println(readInputData(File("input.in")))
}

fun readInputData(input: File): Input {
  val lines = input.readLines()

  val firstLine = lines[0].split(" ")
  val endpointCount = firstLine[1].toLong()
  val cachesCount = firstLine[3].toLong()
  val cachesSize = firstLine[4].toLong()

  val videos: List<Long> = lines[1].split(" ").map(String::toLong)

  val endpoints: MutableList<Endpoint> = ArrayList()

  var currentLine = 2;
  for (i in 0..(endpointCount - 1)) {
    val endpointInfo = lines[currentLine].split(" ")
    currentLine++

    val endpoint = Endpoint(i, endpointInfo[0].toLong())

    val endpointCachesCount = endpointInfo[1].toLong()
    for (j in 1..endpointCachesCount) {
      val cacheConnection = lines[currentLine].split(" ")
      endpoint.caches[cacheConnection[0].toLong()] = cacheConnection[1].toLong()
      currentLine++
    }

    endpoints.add(endpoint)
  }

  val requests = ArrayList<Request>()
  while (currentLine < lines.size) {
    val curLine = lines[currentLine].split(" ")
    currentLine++

    val video = curLine[0].toLong()
    val endpoint = curLine[1].toLong()
    val count = curLine[2].toLong()

    requests.add(Request(video, endpoint, count))
  }

  return Input(endpoints, videos, cachesCount, cachesSize, requests)
}

data class Endpoint(val id: Long, val dataCenterLatency: Long, val caches: MutableMap<Long, Long> = HashMap())
data class Request(val video: Long, val endpoint: Long, val count: Long)
data class Input(val endpoints: List<Endpoint>, val videos: List<Long>, val cacheCount: Long, val cacheSize: Long, val requests: List<Request>)