class CacheBlock {
    boolean valid;
    int tag;
    int last_counter;
    boolean accessed;
    String content;

    CacheBlock() {
        valid = false;
        tag = -1;
        last_counter = 0;
        accessed = false;
        content = null;

    }
}
