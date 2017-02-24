import java.io.File
import java.util.*

fun main(args: Array<String>) {
  val file = "kittens"
  val input = readInputData(File("src/main/resources/$file.txt"))
  println(input)

  finfSolution(input)

  val output = Output()
  cacheServers.forEachIndexed { i, cacheServer ->
    output.caches[i].videos = cacheServer.videos
  }

  File("$file.2out").writeText(output.toString())
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

data class Endpoint(val id: Int, val dataCenterLatency: Int, val caches: MutableMap<Int, Int> = hashMapOf(), var bestLatency: MutableMap<Int, Int> = HashMap())
data class Request(val video: Int, val endpoint: Int, val count: Int)
data class Input(val endpoints: List<Endpoint>, val videos: List<Int>, val cacheCount: Int, val cacheSize: Int, val requests: List<Request>)


data class Win(val video: Int, val cache: Int, val win: Long)

data class CacheServer(val id:Int,
                       var cacheServerSize: Int,
                       var winByVideo:MutableMap<Int, Long> = mutableMapOf(),
                       val videos:MutableList<Int> = mutableListOf())

var requests = listOf<Request>()
var cacheServers = arrayListOf<CacheServer>()
var endpoints = listOf<Endpoint>()
var videosSize = listOf<Int>()
var videoRequests = mutableMapOf<Int, MutableList<Request>>()
var totalCacheSize: Long = 0
var cacheSizeLeft: Long = 0

fun putVideo(cache: Int, video: Int) {
  decreaseCache(cache, video)
  val cacheServer = cacheServers[cache]
  cacheServer.winByVideo.remove(video)
  cacheServer.videos.add(video)
  removeWinsWithVideoesBiggerThatCacheSizeFor(cache)

//  println("Video $video to $cache")
}

fun removeWinsWithVideoesBiggerThatCacheSizeFor(cache: Int) {
  val cacheServer = cacheServers[cache]
  val cacheSize = cacheServer.cacheServerSize
  val keys = cacheServer.winByVideo.filter { videosSize[it.key] > cacheSize }.keys
  for (key in keys) {
    cacheServer.winByVideo.remove(key)
  }
}

fun decreaseCache(cache: Int, video: Int) {
  val cacheServer = cacheServers[cache]
//  println("Cache $cache decreasing size by ${videosSize[video]}")
  cacheServer.cacheServerSize -=  videosSize[video]
  cacheSizeLeft -= videosSize[video]
  println("Size left $cacheSizeLeft (${cacheSizeLeft.toDouble()/totalCacheSize.toDouble()})")
}

fun recalculateForVideo(video: Int) {
  for (c in cacheServers) {
    c.winByVideo.remove(video)
  }
  val currentVideoRequests = videoRequests[video]

  if (currentVideoRequests != null) {
    for (r in currentVideoRequests) {
      val endpoint = endpoints[r.endpoint]
      for (c in endpoint.caches) {
        val server = cacheServers[c.key]
        if (!server.videos.contains(r.video)) {

          var currentWin = server.winByVideo[r.video] ?: 0
          val cacheLatency = c.value
          val latencyWin = cacheLatency - (endpoint.bestLatency[r.video] ?: 0)
          currentWin += r.count * latencyWin
          if (currentWin > 0) {
            server.winByVideo[r.video] = currentWin
          }
        }
      }
    }
  }
}

fun recalculateLatenciesForVideo(cache: Int, video: Int) {
  endpoints
      .filter { it.caches.containsKey(cache) }
      .forEach {
        val winForCache = it.caches[cache]!!
        val current = it.bestLatency[video] ?: 0
        if (current < winForCache) {
          it.bestLatency[video] = winForCache
        }
      }
}


fun findBestW(): Win? {
  var bestWin : Win? = null
  for (c in cacheServers) {
    for (w in c.winByVideo) {
      val latency = w.value
      val videoId = w.key
      val videoSize = videosSize[videoId]
      if (bestWin == null || latency > bestWin.win) {
        if (videoSize <= c.cacheServerSize) {
          bestWin = Win(videoId, c.id, latency)
        }
      }
    }
  }
  return bestWin
}



fun calculateWins() {
  while (true) {
    val theWin = findBestW() ?: break

    // make even better?
    val wins = makeBetterWin(theWin)
    for ((video, cache) in wins) {
      putVideo(cache, video)
      recalculateLatenciesForVideo(cache, video)
      recalculateForVideo(video)
    }
  }
}

fun  makeBetterWin(win: Win): List<Win> {
  // can we make the same win with smaller size on this cache server?
  val server = cacheServers[win.cache]
  if (server.winByVideo.size < 2) {
    return listOf(win)
  }

  val winVideoSize = videosSize[win.video]
  // grab pair of each two and check whether its better to put them here
  var bestItems = listOf(win)
  var bestWin = win.win
  val keys = server.winByVideo.filter { it.key != win.video }
  val keysKwys = keys.keys.toIntArray()
  for (i in 0..keysKwys.size - 2) {
    val id1 = keysKwys[i]
    val win1 = keys[id1]!!
    val size1 = videosSize[id1]
    if (size1 > winVideoSize) continue

    for (j in i + 1..keysKwys.size - 1) {
      val id2 = keysKwys[j]
      val win2 = keys[id2]!!

      val size2 = videosSize[id2]
      val totalSize = size1 + size2
      if (totalSize > server.cacheServerSize) continue
      if (totalSize > winVideoSize) continue
      val commwin = win1 + win2
      if (commwin < bestWin) continue
      bestWin = commwin
      bestItems = listOf(
          Win(id1, server.id, win1),
          Win(id2, server.id, win2)
      )

    }
  }

  if (bestItems.size == 1) {
    return bestItems
  }
  val id1 = bestItems[0].video
  val id2 = bestItems[1].video
  val commwin = bestItems[0].win + bestItems[1].win
  val size1 = videosSize[id1]
  val size2 = videosSize[id2]
  val totalSize = size1 + size2
  println("${keys.size}| $id1 && $id2 >> ${win.video} with total win of $commwin >> ${win.win}, $totalSize << $winVideoSize : Cache size still [${server.cacheServerSize}]")

  return bestItems
}


fun precalculateWinsForCacheServers() {
  for ((video, endpoint, count) in requests) {
    for ((key, latencyWin) in endpoints[endpoint].caches) {
      val server = cacheServers[key]
      val videoSize = videosSize[video]
      if (server.cacheServerSize >= videoSize) {
        var currentWin = server.winByVideo[video] ?: 0
        currentWin += count * latencyWin
        if (currentWin > 0) {
          server.winByVideo[video] = currentWin
        }
      }
    }
  }

  println(cacheServers)
}

fun finfSolution(input: Input) {
  requests = input.requests
  endpoints = input.endpoints
  totalCacheSize = (input.cacheCount * input.cacheSize).toLong()
  cacheSizeLeft = totalCacheSize
  for (i in 0..input.cacheCount) {
    cacheServers.add(CacheServer(i, input.cacheSize))
  }
  videosSize = input.videos
  requests.forEach { it ->
    var list = videoRequests[it.video]
    if (list == null) {
      videoRequests[it.video] = mutableListOf()
    }
    list?.add(it)
  }
  precalculateWinsForCacheServers()
  calculateWins()
}

