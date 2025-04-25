package CacheMemory;

import CacheMemory.CacheBlock;

import java.util.*;

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

    // New setupCache method that accepts parameters
    public void setupCache(int cacheSize, int wordsPerBlock, String mappingPolicy, int nWayInput) {
        this.cacheSize = cacheSize;
        this.wordsPerBlock = wordsPerBlock;
        this.mappingPolicy = mappingPolicy.toUpperCase();

        if (this.mappingPolicy.equals("SA")) {
            this.nWay = nWayInput;
        } else {
            this.nWay = 1;
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

    public int getNWay() {
        return nWay;
    }

    public int getSets() {
        return sets;
    }

    public CacheBlock getBlock(int i, int j) {
        return this.cache[i][j];
    }

}
