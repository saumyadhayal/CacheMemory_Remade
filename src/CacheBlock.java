public class CacheBlock {
    boolean valid;
    int tag;
    int last_counter;

    public CacheBlock() {
        valid = true;
        tag = -1;
        last_counter = 0;
    }
}
