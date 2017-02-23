import java.util.stream.Collectors;

public class Output {
  public OutputCache[] caches = new OutputCache[Constants.C + 1];

  {
    for (int i = 0; i < caches.length; i++) {
      caches[i] = new OutputCache();
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    int length = 0;
    for (OutputCache cache : caches) {
      if (cache != null && !cache.videos.isEmpty()) {
        length++;
      }
    }

    sb.append(length).append('\n');
    for (int i = 0; i < caches.length; i++) {
      OutputCache cache = caches[i];
      if (cache == null || cache.videos.isEmpty()) {
        continue;
      }
      sb.append(i).append(' ');
      CharSequence videos = cache.videos.stream().map(v -> v.toString()).collect(Collectors.joining(" "));

      sb.append(videos).append('\n');
    }


    return sb.toString();
  }

  public static void main(String[] args) {
    Output o = new Output();

    o.caches[15] = new OutputCache();
    o.caches[15].videos.add(12);
    o.caches[25] = new OutputCache();
    o.caches[25].videos.add(15);

    System.out.println(o);


  }
}
