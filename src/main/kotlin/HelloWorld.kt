import java.io.File
import java.util.*

fun main(args: Array<String>) {
  val input = readInputData(File("input.in"))
  println(input)

  finfSolution(input)
}

fun readInputData(input: File): Input {
  val lines = input.readLines()

  val firstLine = lines[0].split(" ")
  val endpointCount = firstLine[1].toInt()
  val cachesCount = firstLine[3].toInt()
  val cachesSize = firstLine[4].toInt()

  val videos: List<Int> = lines[1].split(" ").map(String::toInt)

  val endpoints: MutableList<Endpoint> = ArrayList()

  var currentLine = 2;
  for (i in 0..(endpointCount - 1)) {
    val endpointInfo = lines[currentLine].split(" ")
    currentLine++

    val dataCenterLatency = endpointInfo[0].toInt()
    val endpoint = Endpoint(i, dataCenterLatency)

    val endpointCachesCount = endpointInfo[1].toInt()
    for (j in 1..endpointCachesCount) {
      val cacheConnection = lines[currentLine].split(" ")
      val cacheLatency = cacheConnection[1].toInt()
      val win = dataCenterLatency - cacheLatency
      endpoint.caches[cacheConnection[0].toInt()] = win
      currentLine++
    }

    endpoints.add(endpoint)
  }

  val requests = ArrayList<Request>()
  while (currentLine < lines.size) {
    val curLine = lines[currentLine].split(" ")
    currentLine++

    val video = curLine[0].toInt()
    val endpoint = curLine[1].toInt()
    val count = curLine[2].toInt()

    requests.add(Request(video, endpoint, count))
  }

  return Input(endpoints, videos, cachesCount, cachesSize, requests)
}

data class Endpoint(val id: Int, val dataCenterLatency: Int, val caches: MutableMap<Int, Int> = HashMap())
data class Request(val video: Int, val endpoint: Int, val count: Int)
data class Input(val endpoints: List<Endpoint>, val videos: List<Int>, val cacheCount: Int, val cacheSize: Int, val requests: List<Request>)


data class Win(val video: Int, val cache: Int, val win: Long)

data class CacheServer(val id:Int,
                       var cacheServerSize: Int,
                       val winByVideo:MutableMap<Int, Long> = mutableMapOf(),
                       val videos:MutableList<Int> = mutableListOf())

var requests = listOf<Request>()
var cacheServers = arrayListOf<CacheServer>()
var endpoints = listOf<Endpoint>()
var videosSize = listOf<Int>()

fun putVideo(cache: Int, video: Int) {
  decreaseCache(cache, video)
  val cacheServer = cacheServers[cache]
  cacheServer.winByVideo.remove(video)
  cacheServer.videos.add(video)
  removeWinsWithVideoesBiggerThatCacheSizeFor(cache)
}

fun removeWinsWithVideoesBiggerThatCacheSizeFor(cache: Int) {
//  throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
}

fun decreaseCache(cache: Int, video: Int) {
  cacheServers[cache].cacheServerSize = cacheServers[cache].cacheServerSize - videosSize[video]
}

fun recalculateForVideo(video: Int) {

  for (c in cacheServers) {
    c.winByVideo.remove(video)
  }

  for (r in requests.filter { it.video == video }) {
    for (c in endpoints[r.endpoint].caches) {
      val server = cacheServers[c.key]
      if (!server.videos.contains(r.video)) {
        var currentWin = server.winByVideo[r.video] ?: 0
        val latencyWin = c.value
        currentWin += r.count * latencyWin
        server.winByVideo[r.video] = currentWin
      }
    }
  }
  throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
}

fun findBestW(): Win? {
  var bestWin : Win? = null
  for (c in cacheServers) {
    for (w in c.winByVideo) {
      if (bestWin == null || w.value > bestWin.win) {
        bestWin = Win(w.key, c.id, w.value)
      }
    }
  }
  return bestWin
}

fun calculateWins() {
  while (true) {
    val win = findBestW() ?: break
    putVideo(win.cache, win.video)
    recalculateForVideo(win.video)
  }
}

fun precalculateWinsForCacheServers() {
  for (r in requests) {
    for (c in endpoints[r.endpoint].caches) {
      val server = cacheServers[c.key]
      var currentWin = server.winByVideo[r.video] ?: 0
      val latencyWin = c.value
      currentWin += r.count * latencyWin
      server.winByVideo[r.video] = currentWin
    }
  }

  print(cacheServers)
}

fun finfSolution(input: Input) {
  requests = input.requests
  endpoints = input.endpoints
  for (i in 0..input.cacheCount) {
    cacheServers.add(CacheServer(i, input.cacheSize))
  }
  precalculateWinsForCacheServers()
  calculateWins()
}

