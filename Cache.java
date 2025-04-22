import java.util.*;

class CacheBlock {
    boolean valid;
    int tag;
    int last_counter;

    CacheBlock() {
        valid = false;      // is the cache valid? (1 = valid, 0 = invalid)
        tag = -1;           // Tag bits associated with the memory address to check if a given memory address is a cache hit or miss
                            // -1 = nothing loaded
        last_counter = 0;   // for replacing last block when set is full
    }
}

public class Cache {
    private int cacheSize;
    private int wordsPerBlock;
    private String mappingPolicy;
    private int nWay;
    private CacheBlock[][] cache;
    private int blocks;
    private int sets;
    private int offsetBits;
    private int indexBits;
    private int tagBits;
    private final int wordSize = 4; // bytes per word

    public void setupCache(Scanner scanner) {
        System.out.print("Enter cache size (in Bytes): ");
        cacheSize = scanner.nextInt();
        System.out.print("Enter words per block (1, 2, 4, 8): ");
        wordsPerBlock = scanner.nextInt();
        System.out.print("Enter mapping (DM or SA): ");
        mappingPolicy = scanner.next().toUpperCase();

        if (mappingPolicy.equals("SA")) {
            System.out.print("Enter number of blocks per set (N-way associative): ");
            nWay = scanner.nextInt();
        } else {
            nWay = 1;
        }

        blocks = cacheSize / (wordsPerBlock * wordSize);
        sets = blocks / nWay;

        offsetBits = (int) (Math.log(wordsPerBlock) / Math.log(2));
        indexBits = sets > 1 ? (int) (Math.log(sets) / Math.log(2)) : 0;
        tagBits = 32 - indexBits - offsetBits;

        cache = new CacheBlock[sets][nWay];
        for (int i = 0; i < sets; i++)
            for (int j = 0; j < nWay; j++)
                cache[i][j] = new CacheBlock();

        System.out.println("\nCache Setup Complete:");
        System.out.println("Blocks: " + blocks);
        System.out.println("Sets: " + sets);
        System.out.println("Address Partition: [Tag: " + tagBits + " bits | Index: " + indexBits + " bits | Offset: " + offsetBits + " bits]");
        System.out.println("Real Cache Size: " + (blocks * wordsPerBlock * wordSize) + " Bytes\n");
    }

    
}
